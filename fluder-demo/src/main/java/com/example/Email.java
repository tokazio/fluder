package com.example;

import fr.tokazio.fluder.processor.Buildable;

@Buildable
public class Email {

    String to;
    String subject;
    String content;
    //@Optional
    String bcc;
    //@Optional
    String cc;
    transient Object notInBuilder;
    private String from;
}
