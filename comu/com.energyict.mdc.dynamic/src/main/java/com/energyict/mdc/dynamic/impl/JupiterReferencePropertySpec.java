package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.AbstractValueFactory;
import com.energyict.mdc.dynamic.JupiterReferenceFactory;
import com.energyict.mdc.dynamic.LegacyReferenceFactory;
import com.energyict.mdc.dynamic.PropertySpec;

/**
 * Provides an implementation for the {@link PropertySpec}
 * interface that models a reference to another object.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-15 (17:24)
 */
public class JupiterReferencePropertySpec<T extends HasId> extends BasicPropertySpec<T> {

    public JupiterReferencePropertySpec(String name, boolean required, CanFindByLongPrimaryKey<T> factory) {
        super(name, required, new JupiterReferenceFactory<T>(factory));
    }

    @Override
    public boolean isReference () {
        return true;
    }

}