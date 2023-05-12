package fr.tokazio.fluder.core;

import fr.tokazio.fluder.annotations.Buildable;

import java.util.List;

public interface FluderClass {
    String getQualifiedName();

    List<FluderElement> getEnclosedElements();

    String getSimpleName();

    Buildable buildable();

    FluderClass addCandidate(FluderCandidate candidate);

    List<FluderCandidate> getCandidates();

    String getPackageName();

    boolean noArgCtorIsNotPublic();

    FluderClass setNoArgCtorIsNotPublic(boolean b);

}
