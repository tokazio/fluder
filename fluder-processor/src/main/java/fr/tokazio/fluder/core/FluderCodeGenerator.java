package fr.tokazio.fluder.core;

import java.util.List;

public interface FluderCodeGenerator {
    List<FluderFile> generate(FluderClass fluderClass);
}
