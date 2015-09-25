package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.ObisCode;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeDuration;

import java.util.List;
import java.util.TimeZone;

/**
 * Provides services to build {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:54)
 */
@ProviderType
public interface PropertySpecService extends com.elster.jupiter.properties.PropertySpecService {

    void addFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider);

    /**
     * Todo: remove as part of COPL-1151
     * @deprecated Should be replaced by calls to {@link #basicPropertySpec(String, String, boolean, Class)}
     */
    @Deprecated
    PropertySpec basicPropertySpec (String name, boolean required, Class<? extends ValueFactory> valueFactoryClass);

    /**
     * Creates a PropertySpec, creating the required ValueFactory by asking the injector (DataModel) to provide an instance,
     * thereby enabling Injection on the ValueFactories.
     *
     * @param name The name of the PropertySpec
     * @param description The description of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param valueFactoryClass The class for which the DataModel (injector) will provide an instance
     * @return The PropertySpec
     */
    PropertySpec basicPropertySpec (String name, String description, boolean required, Class<? extends ValueFactory> valueFactoryClass);

    /**
     * Todo: remove as part of COPL-1151
     * @deprecated Replace by calls to {@link #timeDurationPropertySpec(String, String, boolean, TimeDuration)}
     */
    @Deprecated
    PropertySpec timeDurationPropertySpec(String name, boolean required, TimeDuration defaultValue);

    PropertySpec timeDurationPropertySpec(String name, String descrption, boolean required, TimeDuration defaultValue);

    /**
     * Todo: remove as part of COPL-1151
     * @deprecated Replace by calls to {@link #obisCodePropertySpecWithValues(String, String, boolean, ObisCode...)}
     */
    @Deprecated
    PropertySpec obisCodePropertySpecWithValues(String name, boolean required, ObisCode... values);

    /**
     * Creates a {@link PropertySpec} for an {@link ObisCode} value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param values The allowed values for the PropertySpec
     * @return The PropertySpec
     */
    PropertySpec obisCodePropertySpecWithValues(String name, String description, boolean required, ObisCode... values);

    /**
     * Todo: remove as part of COPL-1151
     * @deprecated Replace by calls to {@link #obisCodePropertySpecWithValuesExhaustive(String, String, boolean, ObisCode...)}
     */
    @Deprecated
    PropertySpec obisCodePropertySpecWithValuesExhaustive(String name, boolean required, ObisCode... values);

    PropertySpec obisCodePropertySpecWithValuesExhaustive(String name, String description, boolean required, ObisCode... values);

    /**
     * Todo: remove as part of COPL-1151
     * @deprecated Replace by calls to {@link #referencePropertySpec(String, String, boolean, FactoryIds)}
     */
    @Deprecated
    PropertySpec referencePropertySpec (String name, boolean required, FactoryIds factoryId);

    /**
     * Creates a {@link PropertySpec} that references objects provided by the
     * {@link com.elster.jupiter.properties.AbstractValueFactory} with the specified factoryId.
     *
     * @param name The PropertySpec name
     * @param description The description of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param factoryId The id of the AbstractValueFactory
     * @return The PropertySpec
     */
    public PropertySpec referencePropertySpec (String name, String description, boolean required, FactoryIds factoryId);

    /**
     * Creates a {@link PropertySpec} that references objects provided by the
     * {@link com.elster.jupiter.properties.AbstractValueFactory} with the specified factoryId.
     *
     * @param name The PropertySpec name
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param factoryId The id of the AbstractValueFactory
     * @param possibleValues The exhaustive list of possible values
     * @return The PropertySpec
     */
    public PropertySpec referencePropertySpec(String name, boolean required, FactoryIds factoryId, List<? extends Object> possibleValues);

    /**
     * Todo: remove as part of COPL-1151
     * @deprecated Replace by calls to {@link #booleanPropertySpec(String, String, boolean, Boolean)}
     */
    @Deprecated
    PropertySpec booleanPropertySpec(String name, boolean required, Boolean defaultValue);

    PropertySpec booleanPropertySpec(String name, String description, boolean required, Boolean defaultValue);

    /**
     * Todo: remove as part of COPL-1151
     * @deprecated Replace by calls to {@link #timeZonePropertySpec(String, String, boolean, TimeZone)}
     */
    @Deprecated
    PropertySpec timeZonePropertySpec(String name, boolean required, TimeZone defaultValue);

    /**
     * Creates a PropertySPec that references a TimeZone object.
     *<br/>
     * For now the list of possible values will only contain:
     * <ul>
     *     <li>GMT</li>
     *     <li>Europe/Brussels</li>
     *     <li>EST</li>
     *     <li>Europe/Moscow</li>
     *     </ul>
     *
     * @param name The PropertySpec name
     * @param description The description for the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param defaultValue The default value
     * @return the newly created propertyspec
     */
    PropertySpec timeZonePropertySpec(String name, String description, boolean required, TimeZone defaultValue);

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of values that are managed by the
     * specified {@link ValueFactory}. An instance of the ValueFactory is created by the injector (DataModel), thereby enabling
     * injection on the ValueFactory in casu
     *
     * @param valueFactoryClass Injector will create a instance of this ValueFactory-class
     * @return The PropertySpecBuilder
     */
    PropertySpecBuilder newPropertySpecBuilder(Class<? extends ValueFactory> valueFactoryClass);

    ValueFactory getValueFactory(Class<? extends ValueFactory> valueFactoryClassName);

}