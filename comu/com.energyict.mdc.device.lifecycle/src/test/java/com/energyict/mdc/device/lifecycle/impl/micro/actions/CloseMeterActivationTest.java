package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CloseMeterActivation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-06 (14:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class CloseMeterActivationTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        CloseMeterActivation microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        int numberOfSpecs = propertySpecs.size();
        verify(this.propertySpecService, times(numberOfSpecs))
                .newPropertySpecBuilder(any(ValueFactory.class));
    }

    @Test
    public void executeWithEffectiveTimeClosesMeterActivation() {
        Instant now = Instant.ofEpochSecond(97L);
        CloseMeterActivation microAction = this.getTestInstance();
        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.EFFECTIVE_TIMESTAMP.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(now);

        // Business method
        microAction.execute(this.device, Arrays.asList(property));

        // Asserts
        verify(this.device).deactivate(now);
    }

    @Test
    public void executeWithoutEffectiveTimeClosesMeterActivation() {
        CloseMeterActivation microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verify(this.device).deactivateNow();
    }

    private CloseMeterActivation getTestInstance() {
        return new CloseMeterActivation();
    }

}