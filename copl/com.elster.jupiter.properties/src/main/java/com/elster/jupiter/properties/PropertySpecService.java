package com.elster.jupiter.properties;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.time.RelativePeriod;

import java.math.BigDecimal;

/**
 * Provides services to build {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:54)
 */
@ProviderType
public interface PropertySpecService {

    public PropertySpec basicPropertySpec (String name, boolean required, ValueFactory valueFactory);

    /**
     * Creates a {@link PropertySpec} for a String value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param values The allowed values for the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec stringPropertySpecWithValues (String name, boolean required, String... values);

    /**
     * Creates a {@link PropertySpec} for a String value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec stringPropertySpec (String name, boolean required, String defaultValue);

    /**
     * Creates a {@link PropertySpec} for a String value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param defaultValue The default value in case the property is not specified
     * @param values The allowed values for the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec stringPropertySpecWithValuesAndDefaultValue (String name, boolean required, String defaultValue, String... values);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param values The allowed values for the PropertySpec
     * @return the PropertySpec
     */
    public PropertySpec bigDecimalPropertySpecWithValues (String name, boolean required, BigDecimal... values);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec bigDecimalPropertySpec (String name, boolean required, BigDecimal defaultValue);

    //PropertySpec newPropertySpec(ValueFactory valueFactory, String name, boolean required, Object defaultObject);

    /**
     * Creates a {@link PropertySpec} for positive BigDecimal values.
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @return The PropertySpec
     */
    public PropertySpec positiveDecimalPropertySpec (String name, boolean required);

    /**
     * Creates a {@link PropertySpec} for BigDecimal values that are limited between the lowerLimit and the upperLimit (inclusive).
     *
     * @param name The name of the PropertySpec
     * @param required A flag that indicates if the PropertySpec should be required or not
     * @param lowerLimit The lowest value allowed
     * @param upperLimit The largest value allowed
     * @return The PropertySpec
     */
    public PropertySpec boundedDecimalPropertySpec (String name, boolean required, BigDecimal lowerLimit, BigDecimal upperLimit);

    /**
     * Creates a {@link PropertySpec} for ListValue values that can have single or multiple values at the same time.
     * @param name The name of the PropertySpec
     * @param required The flag that indicates if the PropertySpec should be required or not
     * @param finder The finder values by key
     * @param values The list of possible values
     * @return The PropertySpec
     */
    public <T extends HasIdAndName> PropertySpec listValuePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T... values);

    /**
     * Creates a new {@link PropertySpecBuilder} for building a custom
     * {@link PropertySpec} of values that are managed by the
     * specified {@link ValueFactory}.
     *
     * @param valueFactory The ValueFactory
     * @return The PropertySpecBuilder
     */
    public PropertySpecBuilder newPropertySpecBuilder (ValueFactory valueFactory);

    public PropertySpec relativePeriodPropertySpec(String name, boolean required, RelativePeriod defaultPeriod);

    public PropertySpec longPropertySpec(String name, boolean required, Long defaultValue);

    public PropertySpec longPropertySpecWithValues (String name, boolean required, Long... values);

    public PropertySpec positiveLongPropertySpec (String name, boolean required);

    public PropertySpec boundedLongPropertySpec (String name, boolean required, Long lowerLimit, Long upperLimit);

    public <T extends HasIdAndName> PropertySpec stringReferencePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T... values);
}