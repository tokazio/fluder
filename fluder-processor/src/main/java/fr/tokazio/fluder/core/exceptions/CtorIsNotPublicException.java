package fr.tokazio.fluder.core.exceptions;

public class CtorIsNotPublicException extends FluderProcessorException {
    public CtorIsNotPublicException(String msg) {
        super(msg);
    }
}
