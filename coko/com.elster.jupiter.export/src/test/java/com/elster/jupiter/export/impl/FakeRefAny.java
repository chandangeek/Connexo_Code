/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.orm.associations.RefAny;

import java.util.Optional;

class FakeRefAny implements RefAny {

    private Object value;

    public FakeRefAny() {
    }

    public FakeRefAny(Object value) {
        this.value = value;
    }

    @Override
    public boolean isPresent() {
        return value != null;
    }

    @Override
    public Object get() {
        return value;
    }

    @Override
    public Optional<?> getOptional() {
        return Optional.ofNullable(value);
    }

    @Override
    public String getComponent() {
        return "DES";
    }

    @Override
    public String getTableName() {
        return "";
    }

    @Override
    public Object[] getPrimaryKey() {
        return new Object[0];
    }
}