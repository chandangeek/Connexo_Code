package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;

import java.util.Optional;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (08:42)
 */
public abstract class AbstractPropertySpec implements PropertySpec {

    private final String name;
    private final boolean required;
    private final String description;
    private final String displayName;

    protected AbstractPropertySpec(String name, boolean required, String displayName, String description) {
        this.name = name;
        this.description = description;
        this.displayName = displayName;
        this.required = required;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean isRequired() {
        return this.required;
    }

    @Override
    public PropertySpecPossibleValues getPossibleValues() {
        return new PropertySpecPossibleValuesImpl();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<?> getDefaultValue() {
        return Optional.ofNullable(this.getPossibleValues().getDefault());
    }

    @Override
    public boolean supportsMultiValues() {
        return false;
    }

}