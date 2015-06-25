package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.properties.*;
import com.energyict.mdc.device.data.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link EnableValidation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-04 (17:12)
 */
@RunWith(MockitoJUnitRunner.class)
public class EnableEstimationTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        EnableEstimation enableEstimation = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = enableEstimation.getPropertySpecs(this.propertySpecService);

        // Asserts
        assertThat(propertySpecs).isEmpty();
    }

    @Test
    public void executeEnablesValidation() {
        Instant now = Instant.ofEpochSecond(97L);
        EnableEstimation enableEstimation = this.getTestInstance();
        DeviceEstimation deviceEstimation = mock(DeviceEstimation.class);
        when(this.device.forEstimation()).thenReturn(deviceEstimation);

        // Business method
        enableEstimation.execute(this.device, Instant.now(), Collections.emptyList());

        // Asserts
        verify(deviceEstimation).activateEstimation();
    }

    public EnableEstimation getTestInstance() {
        return new EnableEstimation();
    }

}