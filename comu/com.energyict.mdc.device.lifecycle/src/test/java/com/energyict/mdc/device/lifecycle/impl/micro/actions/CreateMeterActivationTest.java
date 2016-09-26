package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.data.Device;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CreateMeterActivation} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-2 5(14:30)
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateMeterActivationTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        CreateMeterActivation microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        int numberOfSpecs = propertySpecs.size();
        verify(this.propertySpecService, times(numberOfSpecs))
                .specForValuesOf(any(ValueFactory.class));
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

    @Test
    public void executeCreatesMeterActivationIfDeviceHasDataAfterTimestamp() {
        CreateMeterActivation microAction = this.getTestInstance();
        Instant now = Instant.ofEpochSecond(10000L);
        ChannelsContainer newChannelsContainer = mock(ChannelsContainer.class);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(newMeterActivation.getChannelsContainer()).thenReturn(newChannelsContainer);
        MeterActivation existingMeterActivation = mock(MeterActivation.class);
        when(existingMeterActivation.split(now)).thenReturn(newMeterActivation);
        when(existingMeterActivation.getRange()).thenReturn(Range.atLeast(Instant.ofEpochSecond(5000L)));
        when(device.getMeterActivationsMostRecentFirst()).thenReturn(Collections.singletonList(existingMeterActivation));

        // Business method
        microAction.execute(this.device, now, Collections.emptyList());

        // Asserts
        verify(existingMeterActivation).split(now);
    }

    private CreateMeterActivation getTestInstance() {
        return new CreateMeterActivation(thesaurus);
    }

}