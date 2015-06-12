package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

final class DataExportPropertyImpl implements DataExportProperty, PersistenceAware {

    private final DataModel dataModel;

    private String name;
    private String stringValue;
    private transient PropertySpec propertySpec;

    private Reference<IExportTask> task = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    DataExportPropertyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DataExportPropertyImpl init(IExportTask rule, PropertySpec propertySpec, Object value) {
        this.task.set(rule);
        this.name = propertySpec.getName();
        this.propertySpec = propertySpec;
        setValue(value);
        return this;
    }

    static DataExportPropertyImpl from(DataModel dataModel, IExportTask task, String name, Object value) {
        return dataModel.getInstance(DataExportPropertyImpl.class).init(task, task.getPropertySpec(name), value);
    }

    @Override
    public void postLoad() {
        propertySpec = task.get().getPropertySpec(name);
    }

    @Override
    public IExportTask getTask() {
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
        if (BigDecimal.class.equals(getPropertySpec().getValueFactory().getValueType())) {
            this.stringValue = toStringValue(new BigDecimal(value.toString()));
            return;
        }
        this.stringValue = toStringValue(value);
    }

    @Override
    public boolean useDefault() {
        return stringValue == null;
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
        dataModel.mapper(DataExportProperty.class).update(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataExportPropertyImpl that = (DataExportPropertyImpl) o;

        return getTask().getId() == that.getTask().getId() && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
