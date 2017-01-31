/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.concurrent;

@FunctionalInterface
public interface RegistrationHandler {

    void handle(Runnable registration);

    default void ready() {
    }
}