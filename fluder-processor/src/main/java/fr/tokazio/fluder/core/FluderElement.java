package fr.tokazio.fluder.core;

import java.lang.annotation.Annotation;

public interface FluderElement extends FluderField {

    boolean isMethod();

    boolean isField();

    boolean isCtor();

    boolean hasNoArgs();

    boolean isTransient();

    boolean isFinal();

    <A extends Annotation> A getAnnotation(Class<A> anAnnotationClass);

}
