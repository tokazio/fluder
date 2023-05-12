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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FluderProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FluderProcessor.class);

    private Messager messager;

    private final List<ValidationAnnotation> validationAnnotations = new LinkedList<>();

    private static boolean isNonPublic(Element e) {
        return !e.getModifiers().contains(Modifier.PUBLIC);
    }

    private void initValidationAnnotations() {
        if (validationAnnotations.isEmpty()) {
            validationAnnotations.add(new ValidationAnnotation(Nonnull.class) {
                @Override
                public void javaCode(final StringBuilder sb, final FluderCandidate candidate) {
                    sb.append("\t\tif(in==null){\n")
                            .append("\t\t\tthrow new IllegalArgumentException(\"set").append(candidate.setterName()).append(" can't be called with a null parameter. The target field is marked as @Nonnull\");\n")
                            .append("\t\t}\n");
                }
            });
            validationAnnotations.add(new ValidationAnnotation(NotNull.class) {
                @Override
                public void javaCode(final StringBuilder sb, final FluderCandidate candidate) {
                    sb.append("\t\tif(in==null){\n")
                            .append("\t\t\tthrow new IllegalArgumentException(\"set").append(candidate.setterName()).append(" can't be called with a null parameter. The target field is marked as @NotNull\");\n")
                            .append("\t\t}\n");
                }
            });
            validationAnnotations.add(new ValidationAnnotation(NotEmpty.class) {
                @Override
                public void javaCode(final StringBuilder sb, final FluderCandidate candidate) {
                    sb.append("\t\tif(in.isEmpty()){\n")
                            .append("\t\t\tthrow new IllegalArgumentException(\"set").append(candidate.setterName()).append(" can't be called with an empty parameter. The target field is marked as @Empty\");\n")
                            .append("\t\t}\n");
                }
            });
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        initValidationAnnotations();
        messager = processingEnv.getMessager();
        for (TypeElement annotation : annotations) {
            processAnnotation(roundEnv, annotation);
        }
        return true;
    }

    private void processAnnotation(final RoundEnvironment roundEnv, final TypeElement annotation) {
        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
        note("FluderProcessor is processing @Buildable annotations...");
        for (Element el : annotatedElements) {
//            this is actually ensured by the @Buildable @Target(ElementType.TYPE)
//            if (el.getKind() != ElementKind.CLASS) {
//                error("@Buildable can only be applied to class.");
//                return true;
//            }
            if (el instanceof TypeElement) {
                final Buildable buildable = el.getAnnotation(Buildable.class);
                if (buildable == null) {
                    continue;
                }
                processBuildableClass((TypeElement) el, buildable);
            }
        }
    }

    private void processBuildableClass(final TypeElement tl, final Buildable buildable) {
        note("@Buildable processing class " + tl.getQualifiedName().toString() + "...");
        final Fluder fluder = new Fluder();
        final List<Element> ordered = new LinkedList<>();
        final List<Element> unordered = new LinkedList<>();
        final String packageName = tl.getQualifiedName().toString().substring(0, tl.getQualifiedName().toString().lastIndexOf('.'));
        final List<FluderCandidate> candidates = new LinkedList<>();
        boolean noArgCtorIsNotPublic = false;
        for (Element inCl : tl.getEnclosedElements()) {
            note("* Found element '" + inCl.getSimpleName().toString() + "' in " + tl.getQualifiedName().toString());
            if(inCl instanceof ExecutableElement){
                try {
                    processExecutableElement((ExecutableElement)inCl,tl.getQualifiedName().toString());
                }catch (CtorIsNotPublicException ex){
                    warn(ex.getMessage());
                    noArgCtorIsNotPublic = true;
                }catch (NoNoArgCtorException ex){
                    error(ex.getMessage());
                }
            }else if (inCl instanceof VariableElement) {
                try {
                    processVariableElement((VariableElement) inCl,tl.getSimpleName().toString(),buildable, ordered, unordered, candidates);
                }catch (IgnoreElementException ex){
                    note(ex.getMessage());
                }
            } else {
                note("\t" + inCl.getSimpleName().toString() + " is not a field.");
                note("\tFluderProcessor can't handle it at this time.");
            }
        }

        //all or nothing required fields ordered
        if (!ordered.isEmpty() && !unordered.isEmpty()) {
            error("It seems you've started to use @Order in " + tl.getQualifiedName().toString() + ". You must use @Order on each: non transient / @Ignore / non @Optional fields.");
            for (Element e : unordered) {
                error("Please put @Order on '" + tl.getQualifiedName() + "::" + e.getSimpleName() + "'");
            }
        }

        final String simpleClassName = tl.getSimpleName().toString();
        List<FluderFile> files = fluder.generate(buildable, packageName, simpleClassName, noArgCtorIsNotPublic, candidates);
        generateFiles(tl, packageName, files);
    }

    private void processVariableElement(final VariableElement ve, final String buildableClassName, Buildable buildable, final List<Element> ordered, final List<Element> unordered, final List<FluderCandidate> candidates) throws IgnoreElementException {
            if (isTransient(ve)) {
                throw new IgnoreElementException("\t'" + ve.getSimpleName().toString() + "' is 'transient', FluderProcessor has ignored it");
            }
            if (isFinal(ve)) {
                throw new IgnoreElementException("\t'" + ve.getSimpleName().toString() + "' is 'final', FluderProcessor has ignored it");
            }
            Ignore ignore = ve.getAnnotation(Ignore.class);
            if (ignore != null) {
                throw new IgnoreElementException("\t'" + ve.getSimpleName().toString() + "' is annotated @Ignore, FluderProcessor has ignored it");
            }

            Group group = ve.getAnnotation(Group.class);

            //TODO implementation
            if (group != null) {
                warn("@Group not already supported");
            }

            fr.tokazio.fluder.annotations.Name name = ve.getAnnotation(Name.class);
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
                }
            } else {
                ordered.add(ve);
            }

            final List<Validation> resultValidation = processValidationAnnotations(ve);

            final FluderCandidate candidate = new FluderCandidate(
                    buildable,
                    buildableClassName,
                    new FluderField() {
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
                    },
                    name != null ? name.value() : "",
                    opt != null,
                    opt != null ? opt.value() : "",
                    order != null ? order.value() : -1,
                    resultValidation
            );

            note("\tA fluent builder will be generated for " + candidate);
            candidates.add(candidate);

    }

    private void processExecutableElement(final ExecutableElement xe, final String buildableClassName) throws NoNoArgCtorException,CtorIsNotPublicException  {
        if (isCtor(xe)) {
            if (!isNoArg(xe)) {
                throw new NoNoArgCtorException("\t'" + buildableClassName + "' needs a no arg constructor in order to FluderProcessor be able to generate a fluent builder");
            }
            if (isNonPublic(xe)) {
                throw new CtorIsNotPublicException("\t'" + buildableClassName + "' constructor is private, it will be accessed by reflection");
            }
        }
    }

    private static boolean isCtor(final ExecutableElement element) {
        return "<init>".equals(element.getSimpleName().toString());
    }

    private static boolean isNoArg(final ExecutableElement xe){
        return xe.getParameters().isEmpty();
    }

    private void generateFiles(final TypeElement tl, final String packageName, final List<FluderFile> files) {
        note("FluderProcessor generation report for " + tl.getQualifiedName().toString() + ":");
        for (FluderFile file : files) {
            try {
                final JavaFileObject javaFile = writeClassFile(packageName + ".", file.name(), file.javaCode());
                note("\t* " + packageName + "." + file.name() + " generated (" + javaFile.getName() + ")");
            } catch (IOException e) {
                error(e.getClass().getName() + "::" + e.getMessage());
            }
        }
    }

    private List<Validation> processValidationAnnotations(Element el) {
        final List<Validation> out = new LinkedList<>();
        //TODO handle others javax.validation.constraints (beanValidation) via spi
        for (ValidationAnnotation va : validationAnnotations) {
            Annotation a = va.getAnnotationFrom(el);
            if (a != null) {
                out.add(new Validation(a, va));
            }
        }
        return out;
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
        return SourceVersion.RELEASE_8;
    }

    private JavaFileObject writeClassFile(final String path, final String className, final String javaCode) throws IOException {
        final JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(path + className);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print(javaCode);
        }
        return builderFile;
    }

}
