package com.example;

import fr.tokazio.fluder.annotations.Buildable;

@Buildable(builderName = "Buildme", creatorName = "Gen", intermediatePrefix = "Chain", buildMethodName = "go", instanceMethodName = "createNew")
public class RenameAll {

    String test;
}
