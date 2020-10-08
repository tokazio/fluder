package fr.tokazio.fluder.processor;

import fr.tokazio.fluder.annotations.Name;
import fr.tokazio.fluder.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FluderProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FluderProcessor.class);

    private Messager messager;

    private static boolean isNonPublic(Element e) {
        return !e.getModifiers().contains(Modifier.PUBLIC);
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        messager = processingEnv.getMessager();
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            note("FluderProcessor is processing @Buildable annotations...");
            for (Element el : annotatedElements) {
                final Fluder fluder = new Fluder();
                final List<Element> ordered = new LinkedList<>();
                final List<Element> unordered = new LinkedList<>();
                Buildable buildable = el.getAnnotation(Buildable.class);
                if (buildable == null) {
                    continue;
                }
                if (el.getKind() != ElementKind.CLASS) {
                    error("@Buildable can only be applied to class.");
                    return true;
                }
                if (el instanceof TypeElement) {
                    TypeElement tl = (TypeElement) el;
                    note("@Buildable processing class " + tl.getQualifiedName().toString() + "...");
                    final String packageName = tl.getQualifiedName().toString().substring(0, tl.getQualifiedName().toString().lastIndexOf('.'));
                    final List<FluderCandidate> candidates = new LinkedList<>();
                    boolean noArgCtorIsNotPublic = false;
                    for (Element inCl : tl.getEnclosedElements()) {
                        note("* Found element '" + inCl.getSimpleName().toString() + "' in " + tl.getQualifiedName().toString());
                        if (inCl instanceof ExecutableElement) {
                            final ExecutableElement xe = (ExecutableElement) inCl;
                            if ("<init>".equals(xe.getSimpleName().toString())) {
                                if (!xe.getParameters().isEmpty()) {
                                    error("\t'" + tl.getQualifiedName().toString() + "' needs a no arg constructor in order to FluderProcessor be able to generate a fluent builder");
                                    return true;
                                }
                                if (isNonPublic(xe)) {
                                    noArgCtorIsNotPublic = true;
                                    note("\t'" + tl.getQualifiedName().toString() + "' constructor is private");
                                    continue;
                                }
                            }
                        }
                        if (inCl instanceof VariableElement) {
                            final VariableElement ve = (VariableElement) inCl;
                            if (isTransient(ve)) {
                                note("\t'" + ve.getSimpleName().toString() + "' is 'transient', FluderProcessor has ignored it");
                                continue;
                            }
                            if (isFinal(ve)) {
                                note("\t'" + ve.getSimpleName().toString() + "' is 'final', FluderProcessor has ignored it");
                                continue;
                            }
                            Ignore ignore = ve.getAnnotation(Ignore.class);
                            if (ignore != null) {
                                note("\t'" + ve.getSimpleName().toString() + "' is annotated @Ignore, FluderProcessor has ignored it");
                                continue;
                            }

                            Group group = ve.getAnnotation(Group.class);

                            //TODO implementation
                            if (group != null) {
                                warn("@Group nor allready supported");
                            }

                            Name name = ve.getAnnotation(Name.class);
                            Optional opt = ve.getAnnotation(Optional.class);
                            Order order = ve.getAnnotation(Order.class);
                            if (order == null) {//Pas ordered
                                //dans ce cas si il n'est:
                                //pas en option
                                //pas ignoré
                                //pas transient
                                //-> il devrait être ordered si il y a au moin un autre ordered
                                if (opt == null || isTransient(ve)) {
                                    unordered.add(ve);
                                    note(ve.getSimpleName() + " is unordered");
                                }
                            } else {
                                ordered.add(ve);
                                note(ve.getSimpleName() + " is @Order");
                            }
                            Nonnull nonnull = ve.getAnnotation(Nonnull.class);
                            NotNull notnull = ve.getAnnotation(NotNull.class);

                            //TODO
                            //handle javax.validation.constraints (beanValidation)

                            note("\t'" + ve.getSimpleName().toString() + "' is a '" + ve.asType().toString() + "' " + (isNonPublic(ve) ? "non public" : "") + " field " + (opt != null ? "@Optional" : "") + " " + (order != null ? "@Order(" + order.value() + ")" : "") + " " + (nonnull != null ? "@Nonnull" : "") + " " + (notnull != null ? "@NotNull" : ""));
                            FluderCandidate candidate = new FluderCandidate(buildable, tl.getSimpleName().toString(), new FluderField() {
                                @Override
                                public String getTypeName() {
                                    return ve.asType().toString();
                                }

                                @Override
                                public String getName() {
                                    return ve.getSimpleName().toString();
                                }

                                @Override
                                public boolean isNonPublic() {
                                    return FluderProcessor.isNonPublic(ve);
                                }
                            }, name != null ? name.value() : "", opt != null, opt != null ? opt.value() : "", order != null ? order.value() : -1, nonnull != null || notnull != null);

                            note("\tA fluent builder will be generated for " + candidate);
                            candidates.add(candidate);
                        } else {
                            note("\t" + inCl.getSimpleName().toString() + " is not a field.");
                            note("\tFluderProcessor can't handle it at this time.");
                        }
                    }

                    note(ordered.size() + " " + unordered.size());

                    if (!ordered.isEmpty() && !unordered.isEmpty()) {
                        error("It seems you've started to use @Order in " + tl.getQualifiedName().toString() + ". You must use @Order on each: non transient / @Ignore / non @Optional fields.");
                        for (Element e : unordered) {
                            error("Please put @Order on '" + tl.getQualifiedName() + "::" + e.getSimpleName() + "'");
                        }
                    }

                    final String simpleClassName = tl.getSimpleName().toString();
                    List<FluderFile> files = fluder.generate(buildable, packageName, simpleClassName, noArgCtorIsNotPublic, candidates);
                    note("FluderProcessor generation report for " + tl.getQualifiedName().toString() + ":");
                    for (FluderFile file : files) {
                        try {
                            final JavaFileObject javaFile = writeClassFile(packageName + ".", file.name(), file.javaCode());
                            note("\t* " + packageName + "." + file.name() + " generated (" + javaFile.getName() + ")");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isFinal(Element e) {
        return e.getModifiers().contains(Modifier.FINAL);
    }

    private boolean isTransient(Element e) {
        return e.getModifiers().contains(Modifier.TRANSIENT);
    }

    private void error(final String str) {
        messager.printMessage(Diagnostic.Kind.ERROR, str);
        LOGGER.error(str);
    }

    private void note(final String str) {
        messager.printMessage(Diagnostic.Kind.NOTE, str);
        LOGGER.debug(str);
    }

    private void warn(final String str) {
        messager.printMessage(Diagnostic.Kind.WARNING, str);
        LOGGER.warn(str);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> set = new HashSet<>();
        set.add(Buildable.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    private JavaFileObject writeClassFile(final String path, final String className, final String javaCode) throws IOException {
        final JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(path + className);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print(javaCode);
        }
        return builderFile;
    }

}
