package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.ValueFactory;

/**
 * Provides an implementation for the {@link PartialConnectionTaskProperty} interface.
 *
 * @author sva
 * @since 22/01/13 - 9:05
 */
public class PartialConnectionTaskPropertyImpl implements PartialConnectionTaskProperty {

    private Reference<PartialConnectionTask> partialConnectionTask;

    private String name;
    private String value;
    private transient Object objectValue;

    public PartialConnectionTaskPropertyImpl(PartialConnectionTask partialConnectionTask) {
        super();
        this.partialConnectionTask.set(partialConnectionTask);
    }

    private Object getValueObjectFromStringValue(String propertyName, String propertyStringValue) {
        PropertySpec propertySpec = getPartialConnectionTask().getConnectionType().getPropertySpec(propertyName);
        if (propertySpec != null) {
            ValueFactory valueFactory = propertySpec.getValueFactory();
            return valueFactory.fromStringValue(propertyStringValue);
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
            objectValue = getValueObjectFromStringValue(name, value);
        }
        return value;
    }

    @Override
    public PartialConnectionTask getPartialConnectionTask() {
        return partialConnectionTask.get();
    }

}
