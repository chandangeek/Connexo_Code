package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LastReadingTimestampSet} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (10:05)
 */
@RunWith(MockitoJUnitRunner.class)
public class LastReadingTimestampSetTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;

    @Test
    public void deviceWithoutLoadProfilesOrRegisters() {
        // Note that this is a very unlikely situation ;-)
        LastReadingTimestampSet microCheck = this.getTestInstance();
        when(this.device.getRegisters()).thenReturn(Collections.emptyList());
        when(this.device.getLoadProfiles()).thenReturn(Collections.emptyList());

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithLoadProfilesAndRegistersButNoLastReadings() {
        LastReadingTimestampSet microCheck = this.getTestInstance();
        Register reg1 = mock(Register.class);
        when(reg1.getLastReading()).thenReturn(Optional.<Instant>empty());
        Register reg2 = mock(Register.class);
        when(reg2.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(this.device.getRegisters()).thenReturn(Arrays.asList(reg1, reg2));
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getLastReading()).thenReturn(Optional.<Instant>empty());
        LoadProfile lp2 = mock(LoadProfile.class);
        when(lp2.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.LAST_READING_TIMESTAMP_SET);
    }

    @Test
    public void deviceWithLoadProfilesWithLastReadingAndRegistersWithoutLastReading() {
        LastReadingTimestampSet microCheck = this.getTestInstance();
        Register reg1 = mock(Register.class);
        when(reg1.getLastReading()).thenReturn(Optional.<Instant>empty());
        Register reg2 = mock(Register.class);
        when(reg2.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(this.device.getRegisters()).thenReturn(Arrays.asList(reg1, reg2));
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getLastReading()).thenReturn(Optional.of(Instant.now()));
        LoadProfile lp2 = mock(LoadProfile.class);
        when(lp2.getLastReading()).thenReturn(Optional.of(Instant.now()));
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.LAST_READING_TIMESTAMP_SET);
    }

    @Test
    public void deviceWithLoadProfilesWithoutLastReadingAndRegistersWithLastReading() {
        LastReadingTimestampSet microCheck = this.getTestInstance();
        Register reg1 = mock(Register.class);
        when(reg1.getLastReading()).thenReturn(Optional.of(Instant.now()));
        Register reg2 = mock(Register.class);
        when(reg2.getLastReading()).thenReturn(Optional.of(Instant.now()));
        when(this.device.getRegisters()).thenReturn(Arrays.asList(reg1, reg2));
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getLastReading()).thenReturn(Optional.<Instant>empty());
        LoadProfile lp2 = mock(LoadProfile.class);
        when(lp2.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.LAST_READING_TIMESTAMP_SET);
    }

    @Test
    public void deviceWithLoadProfilesAndRegistersWithAllLastReadingsSet() {
        LastReadingTimestampSet microCheck = this.getTestInstance();
        Register reg1 = mock(Register.class);
        when(reg1.getLastReading()).thenReturn(Optional.of(Instant.now()));
        Register reg2 = mock(Register.class);
        when(reg2.getLastReading()).thenReturn(Optional.of(Instant.now()));
        when(this.device.getRegisters()).thenReturn(Arrays.asList(reg1, reg2));
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getLastReading()).thenReturn(Optional.of(Instant.now()));
        LoadProfile lp2 = mock(LoadProfile.class);
        when(lp2.getLastReading()).thenReturn(Optional.of(Instant.now()));
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation).isEmpty();
    }

    private LastReadingTimestampSet getTestInstance() {
        return new LastReadingTimestampSet(this.thesaurus);
    }

}