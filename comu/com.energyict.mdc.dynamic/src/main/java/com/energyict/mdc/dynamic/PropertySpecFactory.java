package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeOfDay;
import java.math.BigDecimal;
import java.util.List;

/**
 * Models the behavior of a component that will provide
 * factory services for {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-01 (11:35)
 */
public interface PropertySpecFactory {

    /**
     * Creates a {@link PropertySpec} for a String value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec stringPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a String value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec stringPropertySpec (String name, String defaultValue);

    /**
     * Creates a {@link PropertySpec} for a String value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param values The allowed values for the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec stringPropertySpecWithValues (String name, String... values);

    /**
     * Creates a {@link PropertySpec} for a "large" String value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec largeStringPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a {@link HexString} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec hexStringPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec bigDecimalPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec bigDecimalPropertySpec (String name, BigDecimal defaultValue);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param values The allowed values for the PropertySpec
     * @return the PropertySpec
     */
    public PropertySpec bigDecimalPropertySpecWithValues (String name, BigDecimal... values);

    /**
     * Creates a {@link PropertySpec} for a Boolean value.
     * This property can have <i>three</i> values:
     * <ul>
     * <li>False</li>
     * <li>True</li>
     * <li>Unknown</li>
     * </ul>
     * The UI will be able to represent the three values.
     * Choosing Unknown will result in an empty property.<br/>
     * If this is not your desired behavior, consider using {@link #notNullableBooleanPropertySpec(String)}
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec booleanPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a Boolean value <b>which cannot be set to null</b>.
     * This means that this property will always be configured (by default false).<br/>
     * If you don't want this behavior for your property,
     * consider using {@link #booleanPropertySpec(String)}.
     * This one allows to set the value to "Unknown",
     * which will result in a <code>null</code> property value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec notNullableBooleanPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a Date value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec datePropertySpec(String name);

    /**
     * Creates a {@link PropertySpec} for a {@link TimeOfDay} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec timeOfDayPropertySpec(String name);

    /**
     * Creates a {@link PropertySpec} for a Date value with time resolution.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec dateTimePropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a {@link TimeDuration} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec timeDurationPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a {@link TimeDuration} value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec timeDurationPropertySpec (String name, TimeDuration defaultValue);

    /**
     * Creates a {@link PropertySpec} for an {@link ObisCode} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec obisCodePropertySpec(String name);

    /**
     * Converts a list with string keys to the new List<PropertySpec> format.
     *
     * @param keys The list of keys
     * @return The list of PropertySpecs
     */
    public List<PropertySpec> toPropertySpecs(List<String> keys);

    public PropertySpec obisCodePropertySpecWithValuesExhaustive(String name, ObisCode... values);

}