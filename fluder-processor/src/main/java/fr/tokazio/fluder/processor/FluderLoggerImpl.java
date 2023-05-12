package fr.tokazio.fluder.processor;

import fr.tokazio.fluder.core.FluderLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public class FluderLoggerImpl implements FluderLogger {

    private final Logger LOGGER;

    private Messager messager;

    public FluderLoggerImpl(final Messager messager, final Class<?> clazz){
        this.messager = messager;
        this.LOGGER = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void error(final String str) {
        messager.printMessage(Diagnostic.Kind.ERROR, str);
        LOGGER.error(str);
    }

    @Override
    public void note(final String str) {
        messager.printMessage(Diagnostic.Kind.NOTE, str);
        LOGGER.debug(str);
    }

    @Override
    public void warn(final String str) {
        messager.printMessage(Diagnostic.Kind.WARNING, str);
        LOGGER.warn(str);
    }
}
