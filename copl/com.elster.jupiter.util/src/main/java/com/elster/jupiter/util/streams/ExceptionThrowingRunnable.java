/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

@FunctionalInterface
public interface ExceptionThrowingRunnable<E extends Throwable> {
    void run() throws E;
}
