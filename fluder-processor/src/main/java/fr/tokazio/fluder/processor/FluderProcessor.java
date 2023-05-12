package fr.tokazio.fluder.processor;

import fr.tokazio.fluder.annotations.Buildable;
import fr.tokazio.fluder.core.FluderClassProcessor;
import fr.tokazio.fluder.core.FluderFileWriter;
import fr.tokazio.fluder.core.FluderLogger;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

public class FluderProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            processAnnotation(roundEnv, annotation);
        }
        return true;
    }

    private void processAnnotation(final RoundEnvironment roundEnv, final TypeElement annotation) {
        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
        final FluderLogger fluderLogger = new FluderLoggerImpl(processingEnv.getMessager(),getClass());
        fluderLogger.note("FluderProcessor is processing @Buildable annotations...");
        final FluderFileWriter fluderFileWriter = new FluderFileWriterImpl(processingEnv.getFiler());
        final FluderClassProcessor fluderProcessor = new FluderClassProcessor(fluderLogger, fluderFileWriter);
        for (Element el : annotatedElements) {
            if (el instanceof TypeElement) {final Buildable buildable = el.getAnnotation(Buildable.class);
                if (buildable == null) {
                    continue;
                }
                fluderProcessor.generate(new FluderClassImpl((TypeElement)el,buildable));
            }
        }
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

}
