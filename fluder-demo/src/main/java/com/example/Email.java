package com.example;

import fr.tokazio.fluder.annotations.Buildable;
import fr.tokazio.fluder.annotations.Optional;
import fr.tokazio.fluder.annotations.Order;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

@Buildable
public class Email {


    @Nonnull
    @Order(0)
    String to;
    @NotNull
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
