package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DisableValidation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-04 (17:12)
 */
@RunWith(MockitoJUnitRunner.class)
public class DisableEstimationTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;

    @Test
    public void testGetPropertySpecs() {
        DisableEstimation disableEstimation = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = disableEstimation.getPropertySpecs(this.propertySpecService);

        // Asserts
        assertThat(propertySpecs).isEmpty();
    }

    @Test
    public void executeDisablesValidation() {
        DisableEstimation disableEstimation = this.getTestInstance();
        DeviceEstimation deviceEstimation = mock(DeviceEstimation.class);
        when(this.device.forEstimation()).thenReturn(deviceEstimation);

        // Business method
        disableEstimation.execute(this.device, Instant.now(), Collections.emptyList());

        // Asserts
        verify(deviceEstimation).deactivateEstimation();
    }

    public DisableEstimation getTestInstance() {
        return new DisableEstimation();
    }

}