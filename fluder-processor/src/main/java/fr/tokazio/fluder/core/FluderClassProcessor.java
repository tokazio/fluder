package fr.tokazio.fluder.core;

import fr.tokazio.fluder.annotations.Group;
import fr.tokazio.fluder.annotations.Ignore;
import fr.tokazio.fluder.annotations.Optional;
import fr.tokazio.fluder.annotations.Order;
import fr.tokazio.fluder.core.exceptions.CtorIsNotPublicException;
import fr.tokazio.fluder.core.exceptions.IgnoreElementException;
import fr.tokazio.fluder.core.exceptions.NoNoArgCtorException;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

public class FluderClassProcessor {

    private final List<ValidationAnnotation> validationAnnotations = new LinkedList<>();
    private final FluderLogger fluderLogger;
    private final FluderFileWriter fluderFileWriter;

    public FluderClassProcessor(final FluderLogger fluderLogger,final FluderFileWriter fluderFileWriter) {
        this.fluderLogger = fluderLogger;
        this.fluderFileWriter = fluderFileWriter;
        initValidationAnnotations();
    }

    public void generate(final FluderClass fluderClass) {
        processBuildableClass(fluderClass);
        generateFile(fluderClass);
    }

    private void generateFile(final FluderClass fluderClass){
        final FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        List<FluderFile> files = fluderJavaGenerator.generate(fluderClass);
        generateFiles(fluderClass, files);
    }

    private void processBuildableClass(final FluderClass fluderClass) {
        fluderLogger.note("@Buildable processing class " + fluderClass.getQualifiedName() + "...");
        final List<FluderElement> ordered = new LinkedList<>();
        final List<FluderElement> unordered = new LinkedList<>();
        for (FluderElement enclosedElement : fluderClass.getEnclosedElements()) {
            processEnclosedElement(fluderClass, ordered, unordered, enclosedElement);
        }
        //all or nothing required fields ordered
        if (!ordered.isEmpty() && !unordered.isEmpty()) {
            fluderLogger.error("It seems you've started to use @Order in " + fluderClass.getQualifiedName() + ". You must use @Order on each: non transient / @Ignore / non @Optional fields.");
            for (FluderElement e : unordered) {
                fluderLogger.error("Please put @Order on '" + fluderClass.getQualifiedName() + "::" + e.getSimpleName() + "'");
            }
        }
    }

    private void processEnclosedElement(final FluderClass fluderClass, final List<FluderElement> ordered, final List<FluderElement> unordered, final FluderElement enclosedElement) {
        fluderLogger.note("* Found element '" + enclosedElement.getSimpleName() + "' in " + fluderClass.getQualifiedName());
        if (enclosedElement.isMethod()) {
            try {
                processExecutableElement(enclosedElement, fluderClass.getQualifiedName());
            } catch (CtorIsNotPublicException ex) {
                fluderLogger.warn(ex.getMessage());
                fluderClass.setNoArgCtorIsNotPublic(true);
            } catch (NoNoArgCtorException ex) {
                fluderLogger.error(ex.getMessage());
            }
        } else if (enclosedElement.isField()) {
            try {
                processVariableElement(enclosedElement, fluderClass, ordered, unordered);
            } catch (IgnoreElementException ex) {
                fluderLogger.note(ex.getMessage());
            }
        } else {
            fluderLogger.note("\t" + enclosedElement.getSimpleName() + " is not a field.");
            fluderLogger.note("\tFluderProcessor can't handle it at this time.");
        }
    }

    private void processVariableElement(final FluderElement ve, final FluderClass fluderClass, final List<FluderElement> ordered, final List<FluderElement> unordered) throws IgnoreElementException {
        if (ve.isTransient()) {
            throw new IgnoreElementException("\t'" + ve.getSimpleName() + "' is 'transient', FluderProcessor has ignored it");
        }
        if (ve.isFinal()) {
            throw new IgnoreElementException("\t'" + ve.getSimpleName() + "' is 'final', FluderProcessor has ignored it");
        }
        Ignore ignore = ve.getAnnotation(Ignore.class);
        if (ignore != null) {
            throw new IgnoreElementException("\t'" + ve.getSimpleName() + "' is annotated @Ignore, FluderProcessor has ignored it");
        }

        Group group = ve.getAnnotation(Group.class);
        //TODO implementation
        if (group != null) {
            fluderLogger.warn("@Group not already supported");
        }

        fr.tokazio.fluder.annotations.Name name = ve.getAnnotation(fr.tokazio.fluder.annotations.Name.class);
        Optional opt = ve.getAnnotation(Optional.class);
        Order order = ve.getAnnotation(Order.class);
        if (order == null) {//Pas ordered
            //dans ce cas si il n'est:
            //pas en option
            //pas ignoré
            //pas transient
            //-> il devrait être ordered si il y a au moin un autre ordered
            if (opt == null) {
                unordered.add(ve);
            }
        } else {
            ordered.add(ve);
        }

        final List<Validation> resultValidation = processValidationAnnotations(ve);

        final FluderCandidate candidate = new FluderCandidate(
                fluderClass,
                ve,
                name != null ? name.value() : "",
                opt != null,
                opt != null ? opt.value() : "",
                order != null ? order.value() : -1,
                resultValidation
        );

        fluderLogger.note("\tA fluent builder will be generated for " + candidate);
        fluderClass.addCandidate(candidate);
    }

    private void processExecutableElement(final FluderElement xe, final String buildableClassName) throws NoNoArgCtorException, CtorIsNotPublicException {
        if (xe.isCtor()) {
            if (!xe.hasNoArgs()) {
                throw new NoNoArgCtorException("\t'" + buildableClassName + "' needs a no arg constructor in order to FluderProcessor be able to generate a fluent builder");
            }
            if (xe.isNonPublic()) {
                throw new CtorIsNotPublicException("\t'" + buildableClassName + "' constructor is private, it will be accessed by reflection");
            }
        }
    }

    private void generateFiles(final FluderClass fluderClass, final List<FluderFile> files) {
        fluderLogger.note("FluderProcessor generation report for " + fluderClass.getQualifiedName() + ":");
        for (FluderFile file : files) {
            try {
                final FluderFile javaFile = fluderFileWriter.writeClassFile(fluderClass.getPackageName() + ".", file);
                fluderLogger.note("\t* " + fluderClass.getPackageName() + "." + file.getName() + " generated (" + javaFile.getName() + ")");
            } catch (IOException e) {
                fluderLogger.error(e.getClass().getName() + "::" + e.getMessage());
            }
        }
    }

    private List<Validation> processValidationAnnotations(final FluderElement el) {
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
}
