package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialConnectionTaskProperty;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Provides an implementation for the {@link PartialConnectionTaskProperty} interface.
 *
 * @author sva
 * @since 22/01/13 - 9:05
 */
@PartialConnectionTaskPropertyMustHaveSpec(groups = {Save.Create.class, Save.Update.class})
@PartialConnectionTaskPropertyValueHasCorrectType(groups = {Save.Create.class, Save.Update.class})
class PartialConnectionTaskPropertyImpl implements PartialConnectionTaskProperty {

    private final DataModel dataModel;

    private Reference<PartialConnectionTask> partialConnectionTask = ValueReference.absent();
    @Size(max = 255, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @Size(max = 4000, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String value;
    private transient Object objectValue;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    PartialConnectionTaskPropertyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static PartialConnectionTaskPropertyImpl from(DataModel dataModel, PartialConnectionTask partialConnectionTask, String name, Object value) {
        PartialConnectionTaskPropertyImpl partialConnectionTaskProperty = new PartialConnectionTaskPropertyImpl(dataModel);
        partialConnectionTaskProperty.partialConnectionTask.set(partialConnectionTask);
        partialConnectionTaskProperty.name = name;
        partialConnectionTaskProperty.setValue(value);
        return partialConnectionTaskProperty;
    }

    private Object getValueObjectFromStringValue(String propertyStringValue) {
        PropertySpec propertySpec = this.getPropertySpec();
        if (propertySpec != null) {
            ValueFactory valueFactory = propertySpec.getValueFactory();
            return valueFactory.fromStringValue(propertyStringValue);
        }
        return null;
    }

    private String asStringValue(Object value) {
        PropertySpec propertySpec = this.getPropertySpec();
        if (propertySpec != null) {
            ValueFactory valueFactory = propertySpec.getValueFactory();
            if (valueFactory.getValueType().isInstance(value)) {
                return valueFactory.toStringValue(value);
            }
            // else there is a value mismatch. Rather than fail fast on this value, we opt to delay until save/update, which will report on all invalid values at once
        }
        return null;

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        if (objectValue == null) {
            objectValue = this.getValueObjectFromStringValue(value);
        }
        return objectValue;
    }

    @Override
    public PartialConnectionTask getPartialConnectionTask() {
        return partialConnectionTask.get();
    }

    void setValue(Object value) {
        objectValue = value;
        this.value = asStringValue(value);
    }

    boolean isRequired() {
        return this.getPropertySpec().isRequired();
    }

    private PropertySpec getPropertySpec() {
        return this.getPartialConnectionTask().getConnectionType().getPropertySpec(name);
    }

    @Override
    public void save() {
        dataModel.mapper(PartialConnectionTaskPropertyImpl.class).update(this);
    }

}