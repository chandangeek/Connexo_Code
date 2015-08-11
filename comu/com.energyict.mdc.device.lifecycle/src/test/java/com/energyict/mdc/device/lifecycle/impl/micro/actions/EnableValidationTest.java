package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;

import java.time.Instant;
import java.util.*;

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
 * Tests the {@link EnableValidation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-04 (17:12)
 */
@RunWith(MockitoJUnitRunner.class)
public class EnableValidationTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        EnableValidation enableValidation = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = enableValidation.getPropertySpecs(this.propertySpecService);

        // Asserts
        int numberOfSpecs = propertySpecs.size();
        verify(this.propertySpecService, times(numberOfSpecs))
                .newPropertySpecBuilder(any(ValueFactory.class));
    }

    @Test
    public void executeEnablesValidation() {
        Instant now = Instant.ofEpochSecond(97L);
        EnableValidation enableValidation = this.getTestInstance();
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(this.device.forValidation()).thenReturn(deviceValidation);
        ExecutableActionProperty property = mock(ExecutableActionProperty.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key());
        when(property.getPropertySpec()).thenReturn(propertySpec);
        when(property.getValue()).thenReturn(now);

        // Business method
        enableValidation.execute(this.device, Instant.now(), Collections.singletonList(property));

        // Asserts
        verify(deviceValidation).activateValidation(now);
    }

    public EnableValidation getTestInstance() {
        return new EnableValidation(thesaurus);
    }

}