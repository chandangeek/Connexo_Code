package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.PropertySpec;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (08:42)
 */
public abstract class AbstractPropertySpec implements PropertySpec {

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

}