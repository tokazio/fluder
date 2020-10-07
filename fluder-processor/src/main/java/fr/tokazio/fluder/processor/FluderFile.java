package fr.tokazio.fluder.processor;

public class FluderFile {

    private final String name;
    private final String javaCode;

    public FluderFile(String name, String javaCode) {
        this.name = name;
        this.javaCode = javaCode;
    }

    public String name() {
        return name;
    }

    public String javaCode() {
        return javaCode;
    }
}
