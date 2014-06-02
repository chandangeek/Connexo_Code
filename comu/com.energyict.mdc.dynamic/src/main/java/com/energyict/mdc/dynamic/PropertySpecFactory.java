package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.common.coordinates.SpatialCoordinates;
import com.energyict.mdc.common.ean.Ean13;
import com.energyict.mdc.common.ean.Ean18;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
    public PropertySpec<String> stringPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a String value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec<String> stringPropertySpec (String name, String defaultValue);

    /**
     * Creates a {@link PropertySpec} for a String value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param values The allowed values for the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<String> stringPropertySpecWithValues (String name, String... values);

    /**
     * Creates a {@link PropertySpec} for a "large" String value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<String> largeStringPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a {@link HexString} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<HexString> hexStringPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a {@link Password} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<Password> passwordPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<BigDecimal> bigDecimalPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec<BigDecimal> bigDecimalPropertySpec (String name, BigDecimal defaultValue);

    /**
     * Creates a {@link PropertySpec} for a BigDecimal value which only allows the given values.
     *
     * @param name The name of the PropertySpec
     * @param values The allowed values for the PropertySpec
     * @return the PropertySpec
     */
    public PropertySpec<BigDecimal> bigDecimalPropertySpecWithValues (String name, BigDecimal... values);

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
    public PropertySpec<Boolean> booleanPropertySpec (String name);

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
    public PropertySpec<Boolean> notNullableBooleanPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a Date value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<Date> datePropertySpec(String name);

    /**
     * Creates a {@link PropertySpec} for a {@link TimeOfDay} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<TimeOfDay> timeOfDayPropertySpec(String name);

    /**
     * Creates a {@link PropertySpec} for a Date value with time resolution.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<Date> dateTimePropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a {@link TimeDuration} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<TimeDuration> timeDurationPropertySpec (String name);

    /**
     * Creates a {@link PropertySpec} for a {@link TimeDuration} value with a default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public PropertySpec<TimeDuration> timeDurationPropertySpec (String name, TimeDuration defaultValue);

    /**
     * Creates a {@link PropertySpec} for an {@link ObisCode} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec<ObisCode> obisCodePropertySpec(String name);

    /**
     * Converts a list with string keys to the new List<PropertySpec> format.
     *
     * @param keys The list of keys
     * @return The list of PropertySpecs
     */
    public List<PropertySpec> toPropertySpecs(List<String> keys);

    public PropertySpec<ObisCode> obisCodePropertySpecWithValuesExhaustive(String name, ObisCode... values);

}