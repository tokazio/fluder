package fr.tokazio.fluder.processor;

public class FluderCandidate {

    private final FluderField field;
    private final String className;
    private final String packageName;
    private final boolean optional;
    private final String defaultValue;
    private final int order;

    public FluderCandidate(String className, FluderField field, boolean optional, String defaultValue, int order) {
        final int index = className.lastIndexOf('.');
        this.packageName = className.substring(0, index);
        this.className = className.substring(index + 1);
        this.field = field;
        this.optional = optional;
        this.defaultValue = defaultValue;
        this.order = order;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public boolean isOptional() {
        return optional;
    }


    public String intfName() {
        return "FluentBuilder" + className + firstUpper(field.getName());
    }

    private String firstUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public String setterType() {
        return field.getTypeName();
    }


    public String setterName() {
        return firstUpper(field.getName());
    }

    public String pckName() {
        return packageName;
    }

    public String fieldSignature() {
        return field.getTypeName() + " " + field.getName();
    }

    public String fieldName() {
        return field.getName();
    }

    public boolean isPrivate() {
        return field.isPrivate();
    }

    public int order() {
        return order;
    }
}
