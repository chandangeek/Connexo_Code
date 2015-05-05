package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DisableValidation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-04 (17:12)
 */
@RunWith(MockitoJUnitRunner.class)
public class DisableValidationTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;

    @Test
    public void testGetPropertySpecs() {
        DisableValidation disableValidation = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = disableValidation.getPropertySpecs(this.propertySpecService);

        // Asserts
        assertThat(propertySpecs).isEmpty();
    }

    @Test
    public void executeDisablesValidation() {
        DisableValidation disableValidation = this.getTestInstance();
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(this.device.forValidation()).thenReturn(deviceValidation);

        // Business method
        disableValidation.execute(this.device, Collections.emptyList());

        // Asserts
        verify(deviceValidation).deactivateValidation();
    }

    public DisableValidation getTestInstance() {
        return new DisableValidation();
    }

}