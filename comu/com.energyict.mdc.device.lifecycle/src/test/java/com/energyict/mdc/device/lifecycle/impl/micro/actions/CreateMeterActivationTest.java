package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link CreateMeterActivation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-2 5(14:30)
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateMeterActivationTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        CreateMeterActivation microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        int numberOfSpecs = propertySpecs.size();
        verify(this.propertySpecService, times(numberOfSpecs))
                .basicPropertySpec(
                        anyString(),
                        anyBoolean(),
                        any(ValueFactory.class));
    }

    @Test
    public void executeCreatesMeterActivation() {
        Instant now = Instant.ofEpochSecond(97L);
        CreateMeterActivation microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, now, Collections.emptyList());

        // Asserts
        verify(this.device).activate(now);
    }

    private CreateMeterActivation getTestInstance() {
        return new CreateMeterActivation();
    }

}