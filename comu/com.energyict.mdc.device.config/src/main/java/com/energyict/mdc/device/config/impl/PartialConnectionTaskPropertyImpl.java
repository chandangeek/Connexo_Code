package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialConnectionTaskProperty;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;

import javax.inject.Inject;

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

    private String name;
    private String value;
    private transient Object objectValue;

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
        PropertySpec propertySpec = getPartialConnectionTask().getConnectionType().getPropertySpec(name);
        if (propertySpec != null) {
            ValueFactory valueFactory = propertySpec.getValueFactory();
            return valueFactory.fromStringValue(propertyStringValue);
        }
        return null;
    }

    private String asStringValue(Object value) {
        PropertySpec propertySpec = getPartialConnectionTask().getConnectionType().getPropertySpec(name);
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
            objectValue = getValueObjectFromStringValue(value);
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

    @Override
    public void save() {
        dataModel.mapper(PartialConnectionTaskPropertyImpl.class).update(this);
    }
}
