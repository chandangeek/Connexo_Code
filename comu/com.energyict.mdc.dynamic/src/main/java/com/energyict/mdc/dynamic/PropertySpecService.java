package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.ObisCode;

/**
 * Provides services to build {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:54)
 */
public interface PropertySpecService extends com.elster.jupiter.properties.PropertySpecService {

    /**
     * Creates a PropertySpec, creating the required ValueFactory by asking the injector (DataModel) to provide an instance,
     * thereby enabling Injection on the ValueFactories
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param valueFactoryClass The class for which the DataModel (injector) will provide an instance
     * @return The PropertySpec
     */
    public <T> PropertySpec<T> basicPropertySpec (String name, boolean required, Class<? extends ValueFactory<T>> valueFactoryClass);

    public PropertySpec<TimeDuration> timeDurationPropertySpec(String name, boolean required, TimeDuration defaultValue);

    /**
     * Creates a {@link PropertySpec} for an {@link ObisCode} value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param values The allowed values for the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<ObisCode> obisCodePropertySpecWithValues(String name, boolean required, ObisCode... values);

    public PropertySpec<ObisCode> obisCodePropertySpecWithValuesExhaustive(String name, boolean required, ObisCode... values);

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

    public void addFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider);

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of values that are managed by the
     * specified {@link ValueFactory}. An instance of the ValueFactory is created by the injector (DataModel), thereby enabling
     * injection on the ValueFactory in casu
     *
     * @param valueFactoryClass Injector will create a instance of this ValueFactory-class
     * @param <T> The Type of values for the PropertySpec
     * @return The PropertySpecBuilder
     */
    public <T> PropertySpecBuilder<T> newPropertySpecBuilder(Class<? extends ValueFactory<T>> valueFactoryClass);

    public <T> ValueFactory<T> getValueFactory(Class<? extends ValueFactory<T>> valueFactoryClassName);
}