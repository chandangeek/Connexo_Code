package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.dynamic.JupiterReferenceFactory;

import com.elster.jupiter.properties.PropertySpecPossibleValuesImpl;

import java.util.List;

/**
 * Provides an implementation for the {@link com.elster.jupiter.properties.PropertySpec}
 * interface that models a reference to another object.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-15 (17:24)
 */
public class JupiterReferencePropertySpec<T extends HasId> extends BasicPropertySpec {

    public JupiterReferencePropertySpec(String name, boolean required, CanFindByLongPrimaryKey<T> factory) {
        super(name, required, new JupiterReferenceFactory<>(factory));
    }

    public JupiterReferencePropertySpec(String name, boolean required, CanFindByLongPrimaryKey<T> factory, List<T> possibleValues) {
        this(name, required, factory);
        this.setPossibleValues(new PropertySpecPossibleValuesImpl(true, possibleValues));
    }

    @Override
    public boolean isReference () {
        return true;
    }

}