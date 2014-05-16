package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.ObisCode;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides services to build {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:54)
 */
public interface PropertySpecService {

    public AtomicReference<PropertySpecService> INSTANCE = new AtomicReference<>();

    public <T> PropertySpec<T> basicPropertySpec (String name, boolean required, ValueFactory<T> valueFactory);

    /**
     * Creates a {@link PropertySpec} for a String value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param values The allowed values for the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<String> stringPropertySpecWithValues (String name, boolean required, String... values);

    /**
     * Creates a {@link PropertySpec} for a String value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec<String> stringPropertySpec (String name, boolean required, String defaultValue);

    /**
     * Creates a {@link PropertySpec} for a String value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param defaultValue The default value in case the property is not specified
     * @param values The allowed values for the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<String> stringPropertySpecWithValuesAndDefaultValue (String name, boolean required, String defaultValue, String... values);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param values The allowed values for the PropertySpec
     * @return the PropertySpec
     */
    public PropertySpec<BigDecimal> bigDecimalPropertySpecWithValues (String name, boolean required, BigDecimal... values);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec<BigDecimal> bigDecimalPropertySpec (String name, boolean required, BigDecimal defaultValue);

    /**
     * Creates a {@link PropertySpec} for positive BigDecimal values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @return The PropertySpec
     */
    public PropertySpec<BigDecimal> positiveDecimalPropertySpec (String name, boolean required);

    /**
     * Creates a {@link PropertySpec} for BigDecimal values that are limited between the lowerLimit and the upperLimit (inclusive).
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param lowerLimit The lowest value allowed
     * @param upperLimit The largest value allowed
     * @return The PropertySpec
     */
    public PropertySpec<BigDecimal> boundedDecimalPropertySpec (String name, boolean required, BigDecimal lowerLimit, BigDecimal upperLimit);

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