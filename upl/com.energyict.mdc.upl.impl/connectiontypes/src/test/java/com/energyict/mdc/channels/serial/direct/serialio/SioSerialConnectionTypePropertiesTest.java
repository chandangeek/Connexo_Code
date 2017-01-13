package com.energyict.mdc.channels.serial.direct.serialio;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests the properties of the {@link SioSerialConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (13:58)
 */
@RunWith(MockitoJUnitRunner.class)
public class SioSerialConnectionTypePropertiesTest {

    @Mock
    private PropertySpecService propertySpecService;

    @Test
    public void testAllOptionalPropertiesAreReturnedByGetPropertySpec () {
        SioSerialConnectionType connectionType = new SioSerialConnectionType(this.propertySpecService);
        for (PropertySpec optionalPropertySpec : connectionType.getUPLPropertySpecs()) {
            assertThat(connectionType.getUPLPropertySpec(optionalPropertySpec.getName())).
                    as("Property " + optionalPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getUPLPropertySpec(optionalPropertySpec.getName())).isEqualTo(optionalPropertySpec);
        }
    }

}