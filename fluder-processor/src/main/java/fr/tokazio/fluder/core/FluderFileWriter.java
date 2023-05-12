package fr.tokazio.fluder.core;

import java.io.IOException;

public interface FluderFileWriter {
    FluderFile writeClassFile(String path, FluderFile fluderFile) throws IOException;
}
