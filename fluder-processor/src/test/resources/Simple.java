package com.example;

import fr.tokazio.fluder.annotations.Buildable;
import fr.tokazio.fluder.annotations.Group;
import fr.tokazio.fluder.annotations.Ignore;

@Buildable
public class Simple {

    @Group("notImplemented")
    String aField;

    transient int doNotBuildTransientFields;

    @Ignore
    boolean notBuildableBecauseIgnored;

    final float finalFieldShouldBeIgnored = 0;
}
