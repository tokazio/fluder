package com.example;

import fr.tokazio.fluder.annotations.Buildable;
import fr.tokazio.fluder.annotations.Order;

@Buildable
public class PartialOrder {


    String noMarkedAsOrdered;

    @Order(1)
    String doNotMind;
}
