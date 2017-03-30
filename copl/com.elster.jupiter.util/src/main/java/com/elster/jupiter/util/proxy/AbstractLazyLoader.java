/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.proxy;

public abstract class AbstractLazyLoader<T> implements LazyLoader<T> {
    private final Class<T> implementedInterface;

    public AbstractLazyLoader(Class<T> implementedInterface) {
        this.implementedInterface = implementedInterface;
    }


    @Override
    public Class<T> getImplementedInterface() {
        return implementedInterface;
    }
}
