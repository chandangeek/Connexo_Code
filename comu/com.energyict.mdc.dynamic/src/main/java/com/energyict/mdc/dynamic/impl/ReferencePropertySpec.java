package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.LegacyReferenceFactory;

/**
 * Provides an implementation for the {@link PropertySpec}
 * interface that models a reference to another object.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-05 (13:59)
 */
public class ReferencePropertySpec<T extends IdBusinessObject> extends BasicPropertySpec<T> {

    public ReferencePropertySpec (String name, boolean required, IdBusinessObjectFactory<T> factory) {
        super(name, required, new LegacyReferenceFactory<>(factory));
    }

    @Override
    public boolean isReference () {
        return true;
    }

}