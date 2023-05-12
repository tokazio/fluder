package fr.tokazio.fluder.core;

public class FluderUtils {

    private FluderUtils(){
        super();
    }

    public static String firstUpper(final String str) {
        return str == null || str.isEmpty() ? "" : str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String firstLower(final String str) {
        return str == null || str.isEmpty() ? "" : str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}
