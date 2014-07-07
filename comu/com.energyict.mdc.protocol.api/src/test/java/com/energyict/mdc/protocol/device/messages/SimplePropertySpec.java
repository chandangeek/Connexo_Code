package com.energyict.mdc.protocol.device.messages;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.ValueFactory;

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
    public boolean isReference () {
        return false;
    }

    @Override
    public boolean validateValue (T value) throws InvalidValueException {
        return true;
    }

    @Override
    public boolean validateValueIgnoreRequired(T value) throws InvalidValueException {
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