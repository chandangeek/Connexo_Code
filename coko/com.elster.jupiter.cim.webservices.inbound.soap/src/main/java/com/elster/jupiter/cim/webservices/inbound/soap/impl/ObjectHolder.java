/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.impl;

public class ObjectHolder<T> {

    private volatile T object;

    public void setObject(T object) {
        this.object = object;
    }

    public void unsetObject() {
        this.object = null;
    }

    public T getObject() {
        return object;
    }
}
