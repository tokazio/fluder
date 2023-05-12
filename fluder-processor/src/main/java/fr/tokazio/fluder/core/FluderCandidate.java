package fr.tokazio.fluder.core;

import java.util.LinkedList;
import java.util.List;

public class FluderCandidate {

    public static final String PREPEND_INTERFACE_NAME = "";

    private final FluderField field;
    private final boolean optional;
    private final String defaultValue;
    private final int order;
    private final FluderClass fluderClass;
    private final String name;
    private final List<Validation> validations = new LinkedList<>();

    public FluderCandidate(
            final FluderClass fluderClass,
            final FluderField field,
            final String name,
            final boolean optional,
            final String defaultValue,
            final int order,
            final List<Validation> validations
    ) {
        this.fluderClass = fluderClass;
        this.field = field;
        this.name = name;
        this.optional = optional;
        this.defaultValue = defaultValue;
        this.order = order;
        this.validations.addAll(validations);
    }



    public String defaultValue() {
        return defaultValue;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return fluderClass.getSimpleName() + "::" + field.getSimpleName() +
                " " + (isPrivate() ? "(non public) " : "") +
                (isOptional() ? "@Optional" + defaultVal() : "") +
                (order >= 0 ? "@Order(" + order + ") " : " ");
    }

    private String defaultVal() {
        return !defaultValue.equals("\0") ? "(" + defaultValue + ") " : " ";
    }

    public String intfName() {
        return prefix() + fluderClass.getSimpleName() + FluderUtils.firstUpper(field.getSimpleName());
    }

    private String prefix() {
        return FluderUtils.firstUpper(fluderClass.buildable().intermediatePrefix());
    }

    public String setterType() {
        return field.getTypeName();
    }

    public String setterName() {
        return FluderUtils.firstUpper(name.isEmpty() ? field.getSimpleName() : name);
    }

    public String fieldSignature() {
        return field.getTypeName() + " " + field.getSimpleName();
    }

    public String fieldName() {
        return field.getSimpleName();
    }

    public boolean isPrivate() {
        return field.isNonPublic();
    }

    public int order() {
        return order;
    }

    public List<Validation> validations() {
        return validations;
    }
}
