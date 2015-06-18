package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link AllLoadProfileDataCollected} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (10:05)
 */
@RunWith(MockitoJUnitRunner.class)
public class AllLoadProfileDataCollectedTest {

    private final long meterId = 4554361215L;

    @Mock
    private Thesaurus thesaurus;
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
    private com.elster.jupiter.metering.Channel meterChannel1;
    @Mock
    private com.elster.jupiter.metering.Channel meterChannel2;
    @Mock
    private ReadingType channelReadingType1;
    @Mock
    private ReadingType channelReadingType2;

    @Before
    public void setup() {
        when(meteringService.findAmrSystem(anyLong())).thenReturn(Optional.of(mdcAmrSystem));
        when(mdcAmrSystem.findMeter(String.valueOf(meterId))).thenReturn(Optional.of(meter));
        doReturn(Optional.of(currentMeterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Collections.singletonList(channelReadingType1)).when(meterChannel1).getReadingTypes();
        doReturn(Collections.singletonList(channelReadingType2)).when(meterChannel2).getReadingTypes();
        when(currentMeterActivation.getChannels()).thenReturn(Arrays.asList(meterChannel1, meterChannel2));
    }

    @Test
    public void deviceWithoutLoadProfiles() {
        // Note that this is a very unlikely situation ;-)
        AllLoadProfileDataCollected microCheck = this.getTestInstance();
        when(this.device.getLoadProfiles()).thenReturn(Collections.emptyList());

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithLoadProfilesButNoLastReadings() {
        AllLoadProfileDataCollected microCheck = this.getTestInstance();
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getLastReading()).thenReturn(Optional.<Instant>empty());
        LoadProfile lp2 = mock(LoadProfile.class);
        when(lp2.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_LOADPROFILE_DATA_COLLECTED);
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
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, effectiveTimestamp);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_LOADPROFILE_DATA_COLLECTED);
    }

    private LoadProfile getMockedLoadProfile(Device device, Instant lastReadingTimestamp) {
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getDevice()).thenReturn(device);
        when(lp1.getLastReading()).thenReturn(Optional.of(lastReadingTimestamp));
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
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, effectiveTimestamp);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_LOADPROFILE_DATA_COLLECTED);
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
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, effectiveTimestamp);

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
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, effectiveTimestamp);

        // Asserts
        assertThat(violation).isEmpty();    }

    private AllLoadProfileDataCollected getTestInstance() {
        return new AllLoadProfileDataCollected(this.thesaurus, meteringService);
    }

}