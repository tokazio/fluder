package fr.tokazio.fluder.processor;

import fr.tokazio.fluder.core.FluderElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;

public class FluderElementImpl implements FluderElement {

    private final Element el;

    public FluderElementImpl(final Element el) {
        this.el = el;
    }

    @Override
    public String getSimpleName() {
        return el.getSimpleName().toString();
    }

    @Override
    public boolean isMethod() {
        return el instanceof ExecutableElement;
    }

    @Override
    public boolean isField() {
        return el instanceof VariableElement;
    }

    @Override
    public boolean isCtor() {
        return isMethod() && "<init>".equals(asExecutable().getSimpleName().toString());
    }

    @Override
    public boolean hasNoArgs() {
        return isMethod() && asExecutable().getParameters().isEmpty();
    }

    @Override
    public boolean isNonPublic() {
        return !el.getModifiers().contains(Modifier.PUBLIC);
    }

    @Override
    public boolean isTransient() {
        return el.getModifiers().contains(Modifier.TRANSIENT);
    }

    @Override
    public boolean isFinal() {
        return el.getModifiers().contains(Modifier.FINAL);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> anAnnotationClass) {
        return el.getAnnotation(anAnnotationClass);
    }

    @Override
    public String getTypeName() {
        return el.asType().toString();
    }

    private ExecutableElement asExecutable() {
        return (ExecutableElement) el;
    }


}
