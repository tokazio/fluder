package fr.tokazio.fluder.processor;

public class CtorIsNotPublicException extends FluderProcessorException {
    public CtorIsNotPublicException(String msg) {
        super(msg);
    }
}
