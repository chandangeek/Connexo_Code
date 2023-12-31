/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AllLoadProfileDataCollected} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class AllLoadProfileDataCollectedTest {

    private final long meterId = 4554361215L;

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private Device device;
    @Mock
    private MeteringService meteringService;
    @Mock
    private AmrSystem mdcAmrSystem;
    @Mock
    private Meter meter;
    @Mock
    private MeterActivation currentMeterActivation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private com.elster.jupiter.metering.Channel meterChannel1;
    @Mock
    private com.elster.jupiter.metering.Channel meterChannel2;
    @Mock
    private ReadingType channelReadingType1;
    @Mock
    private ReadingType channelReadingType2;
    @Mock
    private State state;

    @Before
    public void setup() {
        when(meteringService.findAmrSystem(anyLong())).thenReturn(Optional.of(mdcAmrSystem));
        when(mdcAmrSystem.findMeter(String.valueOf(meterId))).thenReturn(Optional.of(meter));
        doReturn(Optional.of(currentMeterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Collections.singletonList(channelReadingType1)).when(meterChannel1).getReadingTypes();
        doReturn(Collections.singletonList(channelReadingType2)).when(meterChannel2).getReadingTypes();
        when(currentMeterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getChannels()).thenReturn(Arrays.asList(meterChannel1, meterChannel2));
    }

    @Test
    public void deviceWithoutLoadProfiles() {
        // Note that this is a very unlikely situation ;-)
        AllLoadProfileDataCollected microCheck = this.getTestInstance();
        when(this.device.getLoadProfiles()).thenReturn(Collections.emptyList());

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithLoadProfilesButNoLastReadings() {
        AllLoadProfileDataCollected microCheck = this.getTestInstance();
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getLastReading()).thenReturn(null);
        LoadProfile lp2 = mock(LoadProfile.class);
        when(lp2.getLastReading()).thenReturn(null);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void deviceWithLoadProfilesButNoLastReadingsOnEffectiveTimestamp() {
        Device device = getMockedDevice();
        Instant lastReadingTimestamp = Instant.ofEpochMilli(100000L);
        Instant nextTimeStamp = lastReadingTimestamp.plus(1L, ChronoUnit.HOURS);
        Instant effectiveTimestamp = lastReadingTimestamp.plus(7L, ChronoUnit.DAYS);
        AllLoadProfileDataCollected microCheck = this.getTestInstance();
        LoadProfile lp1 = getMockedLoadProfile(device, lastReadingTimestamp);
        LoadProfile lp2 = getMockedLoadProfile(device, lastReadingTimestamp);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));
        when(meterChannel1.getNextDateTime(lastReadingTimestamp)).thenReturn(nextTimeStamp);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, effectiveTimestamp, state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    private LoadProfile getMockedLoadProfile(Device device, Instant lastReadingTimestamp) {
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getDevice()).thenReturn(device);
        when(lp1.getLastReading()).thenReturn(Date.from(lastReadingTimestamp));
        mockChannelsOnLoadProfile(lp1);
        return lp1;
    }

    private void mockChannelsOnLoadProfile(LoadProfile loadProfile) {
        Channel channel1 = mock(Channel.class);
        when(channel1.getReadingType()).thenReturn(channelReadingType1);
        Channel channel2 = mock(Channel.class);
        when(channel2.getReadingType()).thenReturn(channelReadingType2);
        when(loadProfile.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
    }

    private Device getMockedDevice() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(meterId);
        return device;
    }

    @Test
    public void deviceWithOneLoadProfileOkOtherNokTest() {
        Device device = getMockedDevice();
        Instant lastReadingTimestamp = Instant.ofEpochMilli(100000L);
        Instant okNextTimeStamp = lastReadingTimestamp.plus(10L, ChronoUnit.DAYS);
        Instant nokNextTimeStamp = lastReadingTimestamp.plus(1L, ChronoUnit.HOURS);
        Instant effectiveTimestamp = lastReadingTimestamp.plus(7L, ChronoUnit.DAYS);
        AllLoadProfileDataCollected microCheck = this.getTestInstance();
        LoadProfile lp1 = getMockedLoadProfile(device, lastReadingTimestamp);
        LoadProfile lp2 = getMockedLoadProfile(device, lastReadingTimestamp);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));
        when(meterChannel1.getNextDateTime(lastReadingTimestamp)).thenReturn(okNextTimeStamp, nokNextTimeStamp);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, effectiveTimestamp, state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void deviceWithEverythingOKTest() {
        Device device = getMockedDevice();
        Instant lastReadingTimestamp = Instant.ofEpochMilli(100000L);
        Instant okNextTimeStamp = lastReadingTimestamp.plus(10L, ChronoUnit.DAYS);
        Instant effectiveTimestamp = lastReadingTimestamp.plus(7L, ChronoUnit.DAYS);
        AllLoadProfileDataCollected microCheck = this.getTestInstance();
        LoadProfile lp1 = getMockedLoadProfile(device, lastReadingTimestamp);
        LoadProfile lp2 = getMockedLoadProfile(device, lastReadingTimestamp);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));
        when(meterChannel1.getNextDateTime(lastReadingTimestamp)).thenReturn(okNextTimeStamp);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, effectiveTimestamp, state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithLastReadingSetToCurrentEffectiveInstantWhichIsOKTest() {
        Device device = getMockedDevice();
        Instant lastReadingTimestamp = Instant.ofEpochMilli(100000L);
        Instant effectiveTimestamp = lastReadingTimestamp.plus(7L, ChronoUnit.DAYS);
        AllLoadProfileDataCollected microCheck = this.getTestInstance();
        LoadProfile lp1 = getMockedLoadProfile(device, lastReadingTimestamp);
        LoadProfile lp2 = getMockedLoadProfile(device, lastReadingTimestamp);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));
        when(meterChannel1.getNextDateTime(lastReadingTimestamp)).thenReturn(effectiveTimestamp);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, effectiveTimestamp, state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    private AllLoadProfileDataCollected getTestInstance() {
        AllLoadProfileDataCollected allLoadProfileDataCollected = new AllLoadProfileDataCollected();
        allLoadProfileDataCollected.setThesaurus(this.thesaurus);
        allLoadProfileDataCollected.setMeteringService(this.meteringService);
        return allLoadProfileDataCollected;
    }
}
