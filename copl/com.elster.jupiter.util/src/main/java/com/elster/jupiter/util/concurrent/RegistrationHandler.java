package com.elster.jupiter.util.concurrent;

@FunctionalInterface
public interface RegistrationHandler {

    void handle(Runnable registration);

    default void ready() {
    }
}