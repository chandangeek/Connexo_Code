package com.energyict.mdc.dynamic;

import java.util.concurrent.atomic.AtomicReference;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.ObisCode;

/**
 * Provides services to build {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:54)
 */
public interface PropertySpecService extends com.elster.jupiter.properties.PropertySpecService {

    public AtomicReference<PropertySpecService> INSTANCE = new AtomicReference<>();
    
    /**
     * Creates a {@link PropertySpec} for an {@link ObisCode} value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param values The allowed values for the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<ObisCode> obisCodePropertySpecWithValues(String name, boolean required, ObisCode... values);

    public <T extends IdBusinessObject> PropertySpec<T> referencePropertySpec (String name, boolean required, IdBusinessObjectFactory<T> valueFactory);

    /**
     * Creates a {@link PropertySpec} that references objects provided by the
     * {@link AbstractValueFactory} with the specified factoryId.
     *
     * @param name The PropertySpec name
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param factoryId The id of the AbstractValueFactory
     * @return The PropertySpec
     */
    public PropertySpec referencePropertySpec (String name, boolean required, FactoryIds factoryId);

    void addFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider);
}