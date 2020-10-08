package fr.tokazio.fluder.processor;

import fr.tokazio.fluder.annotations.Buildable;

public class FluderCandidate {

    public static final String PREPEND_INTERFACE_NAME = "";

    private final FluderField field;
    private final String simpleClassName;
    private final boolean optional;
    private final String defaultValue;
    private final int order;
    private final boolean isNonnull;
    private final Buildable buildable;
    private final String name;

    public FluderCandidate(final Buildable buildable, final String simpleClassName, final FluderField field, final String name, final boolean optional, final String defaultValue, final int order, final boolean isNonnull) {
        this.buildable = buildable;
        this.simpleClassName = simpleClassName;
        this.field = field;
        this.name = name;
        this.optional = optional;
        this.defaultValue = defaultValue;
        this.order = order;
        this.isNonnull = isNonnull;
    }

    public boolean isNonnull() {
        return isNonnull;
    }

    public static String firstUpper(final String str) {
        return str == null | str.isEmpty() ? "" : str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String firstLower(final String str) {
        return str == null || str.isEmpty() ? "" : str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public String defaultValue() {
        return defaultValue;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(simpleClassName).append("::").append(field.getName())
                .append(" ").append(isPrivate() ? "(non public) " : "")
                .append(isOptional() ? "@Optional" + (!defaultValue.equals("\0") ? "(" + defaultValue + ") " : " ") : "")
                .append(order >= 0 ? "@Order(" + order + ") " : " ");
        return sb.toString();
    }

    public String intfName() {
        return prefix() + simpleClassName + firstUpper(field.getName());
    }

    private String prefix() {
        return firstUpper(buildable.intermediatePrefix());
    }

    public String setterType() {
        return field.getTypeName();
    }


    public String setterName() {
        return firstUpper(name.isEmpty() ? field.getName() : name);
    }


    public String fieldSignature() {
        return field.getTypeName() + " " + field.getName();
    }

    public String fieldName() {
        return field.getName();
    }

    public boolean isPrivate() {
        return field.isNonPublic();
    }

    public int order() {
        return order;
    }
}
