/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.customtask.CustomTaskProperty;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

final class CustomTaskPropertyImpl implements CustomTaskProperty, PersistenceAware {

    private final DataModel dataModel;

    private String name;
    private String stringValue;
    private transient PropertySpec propertySpec;

    private Reference<ICustomTask> task = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    CustomTaskPropertyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    CustomTaskPropertyImpl init(ICustomTask rule, PropertySpec propertySpec, Object value) {
        this.task.set(rule);
        this.name = propertySpec.getName();
        this.propertySpec = propertySpec;
        setValue(value);
        return this;
    }

    static CustomTaskPropertyImpl from(DataModel dataModel, ICustomTask task, String name, Object value) {
        return dataModel.getInstance(CustomTaskPropertyImpl.class).init(task, task.getPropertySpec(name), value);
    }

    @Override
    public void postLoad() {
        propertySpec = task.get().getPropertySpec(name);
    }

    @Override
    public ICustomTask getTask() {
        return task.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return getTask().getDisplayName(name);
    }

    @Override
    public Object getValue() {
        return getPropertySpec().getValueFactory().fromStringValue(stringValue);
    }

    private PropertySpec getPropertySpec() {
        if (propertySpec == null) {
            postLoad();
        }
        return propertySpec;
    }

    @Override
    public boolean instanceOfSpec(PropertySpec spec) {
        return  getPropertySpec().equals(spec);
    }

    @Override
    public void setValue(Object value) {
        if (value != null && !(value instanceof String && Checks.is((String) value)
                .emptyOrOnlyWhiteSpace())) {
            this.stringValue = value.toString();
            return;
        }

        this.stringValue = toStringValue(value);
    }

    @SuppressWarnings("unchecked")
    private String toStringValue(Object object) {
        return getPropertySpec().getValueFactory().toStringValue(object);
    }

    @Override
    public String toString() {
        return getName() + ": " + getValue();
    }

    @Override
    public void save() {
        dataModel.mapper(CustomTaskProperty.class).update(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomTaskPropertyImpl that = (CustomTaskPropertyImpl) o;

        return getTask().getId() == that.getTask().getId() && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
