package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;

/**
 * Provides services to build {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:54)
 */
public interface PropertySpecService {

    public <T> PropertySpec<T> basicPropertySpec (String name, boolean required, ValueFactory<T> valueFactory);

    public <T extends IdBusinessObject> PropertySpec<T> referencePropertySpec (String name, boolean required, IdBusinessObjectFactory<T> valueFactory);

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of values that are managed by the
     * specified {@link ValueFactory}.
     *
     * @param valueFactory The ValueFactory
     * @param <T> The Type of values for the PropertySpec
     * @return The PropertySpecBuilder
     */
    public <T> PropertySpecBuilder<T> newPropertySpecBuilder (ValueFactory<T> valueFactory);

}