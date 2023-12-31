/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations.impl;


import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.PrimaryKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.KeyValue;

import java.util.Objects;
import java.util.Optional;

public class PersistentReference<T> implements Reference<T> {

    private DataMapperImpl<T> dataMapper;
    private Class<?>[] eagers;
    private KeyValue primaryKey;
    private Optional<T> value;
    private ForeignKeyConstraint foreignKeyConstraint;

    public PersistentReference() {

    }

    public PersistentReference(KeyValue primaryKey, DataMapperImpl<T> dataMapper, Class<?>[] eagers, ForeignKeyConstraint foreignKeyConstraint) {
        this.primaryKey = primaryKey;
        this.dataMapper = dataMapper;
        this.eagers = eagers;
        this.foreignKeyConstraint = foreignKeyConstraint;
    }

    @Override
    public void set(T value) {
        this.value = Optional.ofNullable(value);
        primaryKey = dataMapper.getTable().getPrimaryKey(value);
        if (value != null && !isPresent()) {
            throw new IllegalArgumentException("Object " + value + " does not have a valid primary key");
        }
    }

    @Override
    public Optional<T> getOptional() {
        /* Here we check that object that contains reference is cached.*/
        if (foreignKeyConstraint.getTable().isCached() && isPresent()) {
            return dataMapper.getOptional(primaryKey.getKey());
        }
        if (value == null) {
            if (isPresent()) {
                if (eagers.length == 0 || dataMapper.getTable().isCached()) {
                    value = dataMapper.getOptional(primaryKey.getKey());
                } else {
                    value = dataMapper.query(eagers).getOptional(primaryKey.getKey());
                }
            } else {
                value = Optional.empty();
            }
        }
        return value;
    }

    @Override
    public boolean isPresent() {
        if (primaryKey == null) {
            return false;
        } else {
            if (getPrimaryKeyConstraint().allowZero()) {
                return !primaryKey.isNullAllowZero();
            } else {
                return !primaryKey.isNull();
            }
        }
    }

    private PrimaryKeyConstraint getPrimaryKeyConstraint() {
        Table<? super T> table = dataMapper.getTable();
        return table.getPrimaryKeyConstraint()
                .orElseThrow(() -> new IllegalStateException("Table " + table.getName() + " has no primary key."));
    }

    public Object getKeyPart(int index) {
        return primaryKey.get(index);
    }

    public KeyValue getKey() {
        return primaryKey;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Reference)) {
            return false;
        }
        Reference<?> other = (Reference<?>) o;
        return Objects.equals(this.orNull(), other.orNull());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orNull());
    }

    @Override
    public void setNull() {
        set(null);
    }
}
