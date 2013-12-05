package com.energyict.mdc.protocol.device.messages;

import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.dynamic.PropertySpecPossibleValues;
import com.energyict.mdc.protocol.api.dynamic.ValueFactory;

/**
 * Serves as the root for all {@link PropertySpec} test implementations.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-05 (15:03)
 */
public class SimplePropertySpec<T> implements PropertySpec<T> {

    private String name;
    private boolean required;

    public SimplePropertySpec (String name) {
        this(name, false);
    }

    public SimplePropertySpec (String name, boolean required) {
        super();
        this.name = name;
        this.required = required;
    }

    @Override
    public String getName () {
        return this.name;
    }

    @Override
    public boolean isRequired () {
        return this.required;
    }

    @Override
    public boolean validateValue (T value) throws InvalidValueException {
        return true;
    }

    @Override
    public PropertySpecPossibleValues<T> getPossibleValues () {
        return null;
    }

    @Override
    public ValueFactory<T> getValueFactory () {
        // Trying to get away with null for now, may need a fix later
        return null;
    }

}