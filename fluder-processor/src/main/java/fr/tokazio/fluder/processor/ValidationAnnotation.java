package fr.tokazio.fluder.processor;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

public abstract class ValidationAnnotation {

    private final Class<? extends java.lang.annotation.Annotation> clazz;
    private java.lang.annotation.Annotation annotation;

    protected ValidationAnnotation(final Class<? extends java.lang.annotation.Annotation> clazz) {
        this.clazz = clazz;
    }

    public Annotation getAnnotationFrom(final Element el) {
        return el.getAnnotation(clazz);
    }

    public abstract void javaCode(final StringBuilder sb, final FluderCandidate candidate);

}
