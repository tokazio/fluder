package com.example;

import fr.tokazio.fluder.processor.Buildable;
import fr.tokazio.fluder.processor.Optional;
import fr.tokazio.fluder.processor.Order;

import javax.annotation.Nonnull;

@Buildable
public class Email {


    @Nonnull
    @Order(0)
    String to;
    @Order(2)
    String subject;
    @Order(3)
    String content;
    @Optional("\"bcc-default\"")
    String bcc;
    @Optional
    String cc;
    @Order(1)
    private String from;

    transient Object notInBuilder;
}
