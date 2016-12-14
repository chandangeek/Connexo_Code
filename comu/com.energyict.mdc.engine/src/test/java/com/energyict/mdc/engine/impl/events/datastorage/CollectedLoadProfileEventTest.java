package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 24/02/2016
 * Time: 14:29
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedLoadProfileEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory(){
        CollectedLoadProfile loadProfile = mock(CollectedLoadProfile.class);

        CollectedLoadProfileEvent event = new CollectedLoadProfileEvent(serviceProvider, loadProfile);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload(){
        // Business method
        new CollectedLoadProfileEvent(serviceProvider, null);
    }

    @Test
    public void testToString(){
        Instant now = Clock.systemDefaultZone().instant();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        Range<Instant> period =  Range.closedOpen(yesterday,now);

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        LoadProfileService loadProfileService = mock(LoadProfileService.class);

        LoadProfileIdentifierById loadProfileId = new LoadProfileIdentifierById(123L, loadProfileService, ObisCode.fromString("1.1.1.1.1.1"));

        CollectedLoadProfile loadProfile = mock(CollectedLoadProfile.class);
        when(loadProfile.getLoadProfileIdentifier()).thenReturn(loadProfileId);
        when(loadProfile.getCollectedIntervalDataRange()).thenReturn(period);

        CollectedLoadProfileEvent event = new CollectedLoadProfileEvent(serviceProvider, loadProfile);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}
