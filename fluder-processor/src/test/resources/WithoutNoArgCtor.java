package com.example;

import fr.tokazio.fluder.annotations.Buildable;

@Buildable
public class WithoutNoArgCtor {

    String aField;

    public WithoutNoArgCtor(String shouldNotBeBuildable){
        this.aField = shouldNotBeBuildable;
    }
}
