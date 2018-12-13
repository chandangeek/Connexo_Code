/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

public abstract class VoidTransaction implements Transaction<Void> {

    @Override
    public Void perform() {
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
