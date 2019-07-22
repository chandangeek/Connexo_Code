/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

@FunctionalInterface
public interface Transaction<T> extends ExceptionThrowingSupplier<T, RuntimeException> {

    T perform();

    @Override
    default T get() {
        return perform();
    }
}
