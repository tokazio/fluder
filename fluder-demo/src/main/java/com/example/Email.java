package com.example;

import fr.tokazio.fluder.annotations.*;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Buildable
public class Email {

    @Nonnull
    @Order(0)
    String to;
    @NotNull
    @Order(2)
    String subject;
    @Order(3)
    @Name("body")
    String content;
    @Optional("\"bcc-default\"")
    String bcc;
    @Optional
    String cc;
    @Order(1)
    private String from;

    @Ignore
    private Date date;

    transient Object notInBuilder;
}
