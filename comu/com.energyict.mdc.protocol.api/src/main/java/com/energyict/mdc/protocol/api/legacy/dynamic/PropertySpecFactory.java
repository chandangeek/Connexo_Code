package com.energyict.mdc.protocol.api.legacy.dynamic;


import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;

import java.util.List;
import java.util.stream.Collectors;

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
     * @param propertySpecService The PropertySpecService
     * @return The PropertySpec
     */
    public static PropertySpec stringPropertySpec(String name, PropertySpecService propertySpecService) {
        return simplePropertySpec(propertySpecService, name, new StringFactory());
    }

    private static PropertySpec simplePropertySpec(PropertySpecService propertySpecService, String name, com.elster.jupiter.properties.ValueFactory valueFactory) {
        return propertySpecService.basicPropertySpec(name, false, valueFactory);
    }

    public static List<PropertySpec> toPropertySpecs(List<String> keys, PropertySpecService propertySpecService) {
        return keys.stream().map(key -> stringPropertySpec(key, propertySpecService)).collect(Collectors.toList());
    }

    // Hide utility class constructor
    private PropertySpecFactory() {
    }

}