package fr.tokazio.fluder.processor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FluderProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FluderProcessor.class);
    private final Fluder fluder = new Fluder();
    private Messager messager;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        int ordered = 0;
        int unordered = 0;
        messager = processingEnv.getMessager();
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            note("FluderProcessor is processing @Buildable annotations...");
            for (Element el : annotatedElements) {
                if (el.getKind() != ElementKind.CLASS) {
                    error("@Buildable can only be applied to class.");
                    return true;
                }
                if (el instanceof TypeElement) {
                    TypeElement tl = (TypeElement) el;
                    note("@Buildable processing class " + tl.getQualifiedName().toString() + "...");
                    final String packageName = tl.getQualifiedName().toString().substring(0, tl.getQualifiedName().toString().lastIndexOf('.'));
                    final List<FluderCandidate> candidates = new LinkedList<>();
                    for (Element inCl : tl.getEnclosedElements()) {
                        note("* Found element '" + inCl.getSimpleName().toString() + "' in " + tl.getQualifiedName().toString());
                        if (inCl instanceof VariableElement) {
                            final VariableElement ve = (VariableElement) inCl;
                            if (ve.getModifiers().contains(Modifier.TRANSIENT)) {
                                note("\t'" + ve.getSimpleName().toString() + "' is 'transient', FluderProcessor has ignored it");
                                continue;
                            }
                            if (ve.getModifiers().contains(Modifier.FINAL)) {
                                note("\t'" + ve.getSimpleName().toString() + "' is 'final', FluderProcessor has ignored it");
                                continue;
                            }
                            Optional opt = ve.getAnnotation(Optional.class);
                            Order order = ve.getAnnotation(Order.class);
                            note("\t'" + ve.getSimpleName().toString() + "' is a '" + ve.asType().toString() + "' " + (ve.getModifiers().contains(Modifier.PRIVATE) ? "private" : "") + " field " + (opt != null ? "@Optional" : "") + " " + (order != null ? "@Order(" + order.value() + ")" : ""));
                            if (order != null) {
                                ordered++;
                            } else {
                                if (opt == null) {
                                    unordered++;
                                }
                            }
                            FluderCandidate candidate = new FluderCandidate(tl.getSimpleName().toString(), new FluderField() {
                                @Override
                                public String getTypeName() {
                                    return ve.asType().toString();
                                }

                                @Override
                                public String getName() {
                                    return ve.getSimpleName().toString();
                                }

                                @Override
                                public boolean isPrivate() {
                                    return ve.getModifiers().contains(Modifier.PRIVATE);
                                }
                            }, opt != null, opt != null ? opt.value() : "", order != null ? order.value() : -1);

                            note("\tA fluent builder will be generated for " + candidate);
                            candidates.add(candidate);
                        } else {
                            note("\t" + inCl.getSimpleName().toString() + " is not a field.");
                            note("\tFluderProcessor can't handle it at this time.");
                        }
                    }

                    if (ordered > 0 && unordered > 0) {
                        error("It seems you've started to use @Order in " + tl.getQualifiedName().toString() + ". You must use @Order on each non transient and non @Optional fields.");
                    }

                    final String simpleClassName = tl.getSimpleName().toString();
                    List<FluderFile> files = fluder.generate(packageName, simpleClassName, candidates);
                    note("FluderProcessor generation report for " + tl.getQualifiedName().toString() + ":");
                    for (FluderFile file : files) {
                        try {
                            writeClassFile(packageName + ".", file.name(), file.javaCode());
                            note("\t* " + packageName + "." + file.name() + " generated");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return true;
    }

    private void error(String str) {
        messager.printMessage(Diagnostic.Kind.ERROR, str);
        LOGGER.error(str);
    }

    private void note(String str) {
        messager.printMessage(Diagnostic.Kind.NOTE, str);
        LOGGER.debug(str);
    }

    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> set = new HashSet<>();
        set.add(Buildable.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void writeClassFile(String path, String className, String javaCode) throws IOException {
        final JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(path + className);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print(javaCode);
        }
    }

}
