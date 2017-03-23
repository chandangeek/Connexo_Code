/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.propertyspec.MockPropertySpecService;
import org.junit.BeforeClass;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractConnectionTypePropertiesTest {

    protected static PropertySpecService propertySpecService;

    @BeforeClass
    public static void initializeMocksAndFactories() {
        propertySpecService = new MockPropertySpecService();
    }

    protected void assertPropertySpecsEqual(PropertySpec optionalPropertySpec, Optional<PropertySpec> uplPropertySpec) {
        assertTrue(uplPropertySpec.isPresent());
        PropertySpec propertySpec = uplPropertySpec.get();
        assertThat(propertySpec.isRequired()).isEqualTo(optionalPropertySpec.isRequired());
        assertThat(propertySpec.getDescription()).isEqualTo(optionalPropertySpec.getDescription());
        assertThat(propertySpec.getDisplayName()).isEqualTo(optionalPropertySpec.getDisplayName());
        assertThat(propertySpec.getName()).isEqualTo(optionalPropertySpec.getName());
        assertThat(propertySpec.getValueFactory()).isEqualTo(optionalPropertySpec.getValueFactory());
        assertThat(propertySpec.supportsMultiValues()).isEqualTo(optionalPropertySpec.supportsMultiValues());
    }


}
