package fr.tokazio.fluder.processor;

import fr.tokazio.fluder.core.FluderFile;
import fr.tokazio.fluder.core.FluderFileWriter;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;

public class FluderFileWriterImpl implements FluderFileWriter {
    private final Filer filer;

    public FluderFileWriterImpl(final Filer filer) {
        this.filer = filer;
    }

    @Override
    public FluderFile writeClassFile(final String path, final FluderFile file) throws IOException {
        final JavaFileObject builderFile = filer.createSourceFile(path + file.getName());
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print(file.getJavaCode());
        }
        return new FluderFile(builderFile.getName(),file.getJavaCode());
    }
}
