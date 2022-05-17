package com.xyzcorp.resilience4j;

public class Returnable {

    public <T> T andReturn(T t) {
        return t;
    }
}
