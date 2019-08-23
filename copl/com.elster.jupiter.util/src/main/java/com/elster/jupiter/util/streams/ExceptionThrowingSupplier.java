/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

@FunctionalInterface
public interface ExceptionThrowingSupplier<T, E extends Throwable> {
    T get() throws E;
}
