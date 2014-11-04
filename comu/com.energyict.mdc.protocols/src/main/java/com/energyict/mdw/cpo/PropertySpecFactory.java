package com.energyict.mdw.cpo;

import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory;

import com.energyict.mdw.dynamicattributes.StringFactory;

import java.util.ArrayList;
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