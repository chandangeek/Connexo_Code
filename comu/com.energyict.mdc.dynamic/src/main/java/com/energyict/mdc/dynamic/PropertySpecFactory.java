package com.energyict.mdc.dynamic;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import java.math.BigDecimal;
import java.util.List;

/**
 * Models the behavior of a component that will provide
 * factory services for {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-01 (11:35)
 */
@ProviderType
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
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public PropertySpec booleanPropertySpec (String name);

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
     * Converts a list with string keys to the new List<PropertySpec> format.
     *
     * @param keys The list of keys
     * @return The list of PropertySpecs
     */
    public List<PropertySpec> toPropertySpecs(List<String> keys);

}