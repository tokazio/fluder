package fr.tokazio.fluder.core;

import java.lang.annotation.Annotation;

public abstract class ValidationAnnotation {

    private final Class<? extends java.lang.annotation.Annotation> clazz;

    protected ValidationAnnotation(final Class<? extends java.lang.annotation.Annotation> clazz) {
        this.clazz = clazz;
    }

    public Annotation getAnnotationFrom(final FluderElement el) {
        return el.getAnnotation(clazz);
    }

    public abstract void javaCode(final StringBuilder sb, final FluderCandidate candidate);

}
