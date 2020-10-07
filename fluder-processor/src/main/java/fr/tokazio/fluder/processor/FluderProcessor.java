package fr.tokazio.fluder.processor;

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

    private final Fluder fluder = new Fluder();

    private int ordered;
    private int unordered;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ordered = 0;
        unordered = 0;
        final Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "Processing @Buildable annotation");
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element el : annotatedElements) {
                if (el.getKind() != ElementKind.CLASS) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class only.");
                    System.out.println("Can be applied to class only.");
                    return true;
                }
                if (el instanceof TypeElement) {
                    TypeElement tl = (TypeElement) el;
                    messager.printMessage(Diagnostic.Kind.NOTE, tl.getQualifiedName().toString());
                    System.out.println("@Buildable processing " + tl.getQualifiedName().toString() + "...");
                    final String packagename = tl.getQualifiedName().toString().substring(0, tl.getQualifiedName().toString().lastIndexOf('.'));
                    final List<FluderCandidate> candidates = new LinkedList<>();
                    for (Element inCl : tl.getEnclosedElements()) {
                        System.out.println("\tElement " + inCl.getSimpleName());
                        if (inCl instanceof VariableElement) {
                            final VariableElement ve = (VariableElement) inCl;
                            if (!ve.getModifiers().contains(Modifier.TRANSIENT) && !ve.getModifiers().contains(Modifier.FINAL)) {
                                System.out.println("\t\t'" + ve.getSimpleName().toString() + "' is a '" + ve.asType().toString() + "' " + (ve.getModifiers().contains(Modifier.PRIVATE) ? "private" : "") + " variable");

                                Optional opt = ve.getAnnotation(Optional.class);
                                Order order = ve.getAnnotation(Order.class);

                                if (order != null) {
                                    ordered++;
                                } else {
                                    if (opt == null) {
                                        unordered++;
                                    }
                                }

                                candidates.add(new FluderCandidate(tl.getQualifiedName().toString(), new FluderField() {
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
                                }, opt != null, opt != null ? opt.value() : "", order != null ? order.value() : -1));
                            }
                        }
                    }

                    if (ordered > 0 && unordered > 0) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "It seems you've started to use @Order in " + tl.getQualifiedName().toString() + ". You must use @Order on each fields.");
                    }

                    System.out.println("Adding files for " + tl.getSimpleName().toString() + "...");
                    List<FluderFile> files = fluder.generate(tl.getSimpleName().toString(), candidates);
                    for (FluderFile file : files) {
                        try {
                            writeClassFile(packagename + ".", file.name(), file.javaCode());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return true;
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
