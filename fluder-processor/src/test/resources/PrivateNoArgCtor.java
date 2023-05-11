package com.example;

import fr.tokazio.fluder.annotations.Buildable;

@Buildable
public class PrivateNoArgCtor {

    String aField;

    private PrivateNoArgCtor(){
        super();
    }
}
