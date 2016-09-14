package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
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
        Instant now = Instant.ofEpochSecond(97L);
        Instant lastDataTimestamp = now.plus(10, ChronoUnit.MINUTES);
        CreateMeterActivation microAction = this.getTestInstance();
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile));
        when(loadProfile.getLastReading()).thenReturn(Optional.of(lastDataTimestamp));
        MeterActivation currentMeterActivation = mock(MeterActivation.class);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(newMeterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        doReturn(Optional.of(currentMeterActivation)).when(device).getCurrentMeterActivation();
        when(device.activate(lastDataTimestamp)).thenReturn(newMeterActivation);

        // Business method
        microAction.execute(this.device, now, Collections.emptyList());

        // Asserts
        verify(device).activate(lastDataTimestamp);
        verify(newMeterActivation).advanceStartDate(now);
    }

    private CreateMeterActivation getTestInstance() {
        return new CreateMeterActivation(thesaurus);
    }

}