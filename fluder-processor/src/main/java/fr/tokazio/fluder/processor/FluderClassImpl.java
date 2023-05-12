package fr.tokazio.fluder.processor;

import fr.tokazio.fluder.annotations.Buildable;
import fr.tokazio.fluder.core.FluderCandidate;
import fr.tokazio.fluder.core.FluderClass;
import fr.tokazio.fluder.core.FluderElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FluderClassImpl implements FluderClass {

    private final List<FluderCandidate> candidates = new LinkedList<>();

    private final TypeElement el;
    private final Buildable buildable;

    private List<FluderElement> cachedElements;
    private boolean noArgCtorIsNotPublic = false;

    public FluderClassImpl(final TypeElement el, final Buildable buildable){
        this.el = el;
        this.buildable = buildable;
    }

    @Override
    public String getQualifiedName() {
        return el.getQualifiedName().toString();
    }

    @Override
    public List<FluderElement> getEnclosedElements() {
        if(cachedElements==null){
            cachedElements = new LinkedList<>();
            for(Element e : el.getEnclosedElements()){
                cachedElements.add(new FluderElementImpl(e));
            }
        }
        return cachedElements;
    }

    @Override
    public String getSimpleName() {
        return el.getSimpleName().toString();
    }

    @Override
    public Buildable buildable() {
        return buildable;
    }

    @Override
    public FluderClass addCandidate(final FluderCandidate candidate) {
        if(candidate!=null) {
            candidates.add(candidate);
        }
        return this;
    }

    @Override
    public List<FluderCandidate> getCandidates() {
        return Collections.unmodifiableList(candidates);
    }

    @Override
    public String getPackageName() {
        return getQualifiedName().substring(0, getQualifiedName().lastIndexOf('.'));
    }

    @Override
    public boolean noArgCtorIsNotPublic() {
        return noArgCtorIsNotPublic;
    }

    @Override
    public FluderClass setNoArgCtorIsNotPublic(final boolean b) {
        this.noArgCtorIsNotPublic = b;
        return this;
    }
}
