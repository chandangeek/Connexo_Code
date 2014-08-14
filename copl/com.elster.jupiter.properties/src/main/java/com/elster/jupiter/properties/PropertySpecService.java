package com.elster.jupiter.properties;

import java.math.BigDecimal;

/**
 * Provides services to build {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:54)
 */
public interface PropertySpecService {

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
     * Creates a {@link PropertySpec} for ListValue values that can have single or multiple values at the same time. 
     * @param name The name of the PropertySpec
     * @param required The flag that indicates if the PropertySpec should be required or not
     * @param finder The finder values by key
     * @param values The list of possible values
     * @return The PropertySpec
     */
    public <T extends ListValueEntry> PropertySpec<ListValue<T>> listValuePropertySpec(String name, boolean required, FindById<T> finder, T... values);
    
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