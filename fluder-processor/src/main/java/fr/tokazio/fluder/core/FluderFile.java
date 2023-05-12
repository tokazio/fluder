package fr.tokazio.fluder.core;

public class FluderFile {

    private final String name;
    private final String javaCode;

    public FluderFile(String name, String javaCode) {
        this.name = name;
        this.javaCode = javaCode;
    }

    public String getName() {
        return name;
    }

    public String getJavaCode() {
        return javaCode;
    }
}
