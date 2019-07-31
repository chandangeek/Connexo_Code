/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import com.elster.jupiter.util.streams.ExceptionThrowingRunnable;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

/**
 * @deprecated Please directly use methods taking {@link ExceptionThrowingRunnable} instead of wrapping {@link Runnable} to {@link VoidTransaction}.
 */
@Deprecated
public abstract class VoidTransaction implements ExceptionThrowingSupplier<Void, RuntimeException> {

    @Override
    public Void get() {
        doPerform();
        return null;
    }

    protected abstract void doPerform();

    public static VoidTransaction of(Runnable runnable) {
        return new VoidTransaction() {
            @Override
            protected void doPerform() {
                runnable.run();
            }
        };
    }
}
