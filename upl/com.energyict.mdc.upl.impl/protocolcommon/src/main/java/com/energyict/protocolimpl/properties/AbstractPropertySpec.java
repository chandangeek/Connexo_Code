package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (08:42)
 */
public abstract class AbstractPropertySpec<T> implements PropertySpec<T> {

    private final String name;
    private final boolean required;

    protected AbstractPropertySpec(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.getName();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean isRequired() {
        return this.required;
    }

    @Override
    public List<T> getPossibleValues() {
        return Collections.emptyList();
    }

    @Override
    public Optional<T> getDefaultValue() {
        return Optional.empty();
    }
}