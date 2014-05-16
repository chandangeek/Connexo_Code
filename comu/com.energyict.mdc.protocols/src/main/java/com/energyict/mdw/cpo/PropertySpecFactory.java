package com.energyict.mdw.cpo;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory;

import com.energyict.mdw.dynamicattributes.BigDecimalFactory;
import com.energyict.mdw.dynamicattributes.BooleanFactory;
import com.energyict.mdw.dynamicattributes.DateAndTimeFactory;
import com.energyict.mdw.dynamicattributes.HexStringFactory;
import com.energyict.mdw.dynamicattributes.StringFactory;
import com.energyict.mdw.dynamicattributes.ThreeStateFactory;
import com.energyict.mdw.dynamicattributes.TimeDurationValueFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides factory services for {@link PropertySpec}s.
 * <p/>
 * User: jbr
 * Date: 7/05/12
 * Time: 10:56
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-02 (14:19)
 */
public class PropertySpecFactory {

    /**
     * Creates a PropertySpec for a String value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<String> stringPropertySpec(String name) {
        return simplePropertySpec(name, String.class, new StringFactory());
    }

    /**
     * Creates a PropertySpec for a String value, taking into account its default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public static PropertySpec<String> stringPropertySpec(String name, String defaultValue) {
        return PropertySpecBuilder.
                forClass(String.class, new StringFactory()).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    /**
     * Creates a PropertySpec for a HexString value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<HexString> hexStringPropertySpec(String name) {
        return simplePropertySpec(name, HexString.class, new HexStringFactory());
    }

    /**
     * Creates a PropertySpec for a BigDecimal value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<BigDecimal> bigDecimalPropertySpec(String name) {
        return simplePropertySpec(name, BigDecimal.class, new BigDecimalFactory());
    }

    /**
     * Creates a PropertySpec for a BigDecimal value, taking into account its default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public static PropertySpec<BigDecimal> bigDecimalPropertySpec(String name, BigDecimal defaultValue) {
        return PropertySpecBuilder.
                forClass(BigDecimal.class, new BigDecimalFactory()).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    /**
     * Creates a PropertySpec for a Boolean value.
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
    public static PropertySpec<Boolean> booleanPropertySpec(String name) {
        return simplePropertySpec(name, Boolean.class, new ThreeStateFactory());
    }

    /**
     * Creates a PropertySpec for a Boolean value <b>which cannot be set to null</b>.
     * This means that this property will always be configured (by default false).<br/>
     * If you don't want this behavior for your property,
     * consider using {@link #booleanPropertySpec(String)}.
     * This one allows to set the value to "Unknown",
     * which will result in a <code>null</code> property value.
     *
     * @param name the name of the PropertySpec
     * @return the PropertySpec
     */
    public static PropertySpec<Boolean> notNullableBooleanPropertySpec (String name) {
        return simplePropertySpec(name, Boolean.class, new BooleanFactory());
    }

    /**
     * Creates a PropertySpec for a Date value with time resolution.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<Date> dateTimePropertySpec(String name) {
        return simplePropertySpec(name, Date.class, new DateAndTimeFactory());
    }

    /**
     * Creates a PropertySpec for a {@link TimeDuration} value.
     *
     * @param name The name of the PropertySpec
     * @return The PropertySpec
     */
    public static PropertySpec<TimeDuration> timeDurationPropertySpec(String name) {
        return simplePropertySpec(name, TimeDuration.class, new TimeDurationValueFactory());
    }

    /**
     * Creates a PropertySpec for a {@link TimeDuration} value, taking into account its default value.
     *
     * @param name The name of the PropertySpec
     * @param defaultValue The default value in case the property is not specified
     * @return The PropertySpec
     */
    public static PropertySpec<TimeDuration> timeDurationPropertySpec(String name, TimeDuration defaultValue) {
        return PropertySpecBuilder.
                forClass(TimeDuration.class, new TimeDurationValueFactory()).
                name(name).
                setDefaultValue(defaultValue).
                finish();
    }

    private static <T> PropertySpec<T> simplePropertySpec(String name, Class<T> domainClass, ValueFactory<T> valueFactory) {
        return PropertySpecBuilder.
                forClass(domainClass, valueFactory).
                name(name).
                finish();
    }

    // for legacy conversion

    /**
     * Converts a list with string keys to the new List<PropertySpec> format.
     *
     * @param keys The list of keys
     * @return The list of PropertySpecs
     */
    public static List<PropertySpec> toPropertySpecs(List<String> keys) {
        List<PropertySpec> result = new ArrayList<>();
        for (String key : keys) {
            result.add(PropertySpecFactory.stringPropertySpec(key));
        }
        return result;
    }

    // Hide utility class constructor
    private PropertySpecFactory() {
    }

}