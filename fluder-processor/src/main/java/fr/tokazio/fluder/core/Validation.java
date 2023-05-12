package fr.tokazio.fluder.core;

import java.lang.annotation.Annotation;

public class Validation {


    private final Annotation a;
    private final ValidationAnnotation va;

    public Validation(Annotation a, ValidationAnnotation va) {
        this.a = a;
        this.va = va;
    }

    public void validate(StringBuilder sb, FluderCandidate candidate) {
        if (a != null) {
            va.javaCode(sb, candidate);
        }
    }
}
