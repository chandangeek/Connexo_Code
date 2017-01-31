/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy.dynamic;


import com.elster.jupiter.properties.PropertySpec;
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

    public static List<PropertySpec> toPropertySpecs(List<String> keys, PropertySpecService propertySpecService) {
        return keys
                .stream()
                .map(key -> propertySpecService
                        .stringSpec()
                        .named(key, key).describedAs(key)
                        .finish())
                .collect(Collectors.toList());
    }

    // Hide utility class constructor
    private PropertySpecFactory() {
    }

}