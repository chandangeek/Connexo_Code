package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.NumericalReading;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AllDataValid} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (09:48)
 */
@RunWith(MockitoJUnitRunner.class)
public class AllDataValidTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;
    @Mock
    private DeviceValidation deviceValidation;

    @Before
    public void initializeMocks() {
        when(this.device.forValidation()).thenReturn(this.deviceValidation);
        when(this.deviceValidation.allDataValidated(any(Channel.class), any(Instant.class))).thenReturn(false);
        when(this.deviceValidation.allDataValidated(any(Register.class), any(Instant.class))).thenReturn(false);
    }

    @Test
    public void deviceWithoutLoadProfilesOrRegisters() {
        // Note that this is a very unlikely situation ;-)
        AllDataValid microCheck = this.getTestInstance();
        when(this.device.getRegisters()).thenReturn(Collections.emptyList());
        when(this.device.getLoadProfiles()).thenReturn(Collections.emptyList());

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithLoadProfilesAndRegistersButNoLastReadings() {
        AllDataValid microCheck = this.getTestInstance();
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
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithValidLoadProfiles() {
        AllDataValid microCheck = this.getTestInstance();
        Instant now = Instant.now();
        when(this.device.getRegisters()).thenReturn(Collections.emptyList());
        Channel lp1_ch1 = mock(Channel.class);
        when(this.deviceValidation.allDataValidated(lp1_ch1, now)).thenReturn(true);
        Channel lp1_ch2 = mock(Channel.class);
        when(this.deviceValidation.allDataValidated(lp1_ch2, now)).thenReturn(true);
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getDevice()).thenReturn(this.device);
        when(lp1.getLastReading()).thenReturn(Optional.of(now));
        when(lp1.getChannels()).thenReturn(Arrays.asList(lp1_ch1, lp1_ch2));
        Channel lp2_ch1 = mock(Channel.class);
        when(this.deviceValidation.allDataValidated(lp2_ch1, now)).thenReturn(true);
        Channel lp2_ch2 = mock(Channel.class);
        when(this.deviceValidation.allDataValidated(lp2_ch2, now)).thenReturn(true);
        LoadProfile lp2 = mock(LoadProfile.class);
        when(lp2.getDevice()).thenReturn(this.device);
        when(lp1.getChannels()).thenReturn(Arrays.asList(lp2_ch1, lp2_ch2));
        when(lp2.getLastReading()).thenReturn(Optional.of(now));
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, now);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithSomeChannelsWithUnvalidatedData() {
        AllDataValid microCheck = this.getTestInstance();
        Instant now = Instant.now();
        when(this.device.getRegisters()).thenReturn(Collections.emptyList());
        Channel lp1_ch1 = mock(Channel.class);
        when(this.deviceValidation.allDataValidated(lp1_ch1, now)).thenReturn(true);
        Channel lp1_ch2 = mock(Channel.class);
        when(this.deviceValidation.allDataValidated(lp1_ch2, now)).thenReturn(false);
        LoadProfile lp1 = mock(LoadProfile.class);
        when(lp1.getDevice()).thenReturn(this.device);
        when(lp1.getLastReading()).thenReturn(Optional.of(now));
        when(lp1.getChannels()).thenReturn(Arrays.asList(lp1_ch1, lp1_ch2));
        Channel lp2_ch1 = mock(Channel.class);
        when(this.deviceValidation.allDataValidated(lp2_ch1, now)).thenReturn(false);
        Channel lp2_ch2 = mock(Channel.class);
        when(this.deviceValidation.allDataValidated(lp2_ch2, now)).thenReturn(true);
        LoadProfile lp2 = mock(LoadProfile.class);
        when(lp2.getDevice()).thenReturn(this.device);
        when(lp1.getChannels()).thenReturn(Arrays.asList(lp2_ch1, lp2_ch2));
        when(lp2.getLastReading()).thenReturn(Optional.of(now));
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(lp1, lp2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, now);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_DATA_VALID);
    }

    @Test
    public void deviceWithValidRegisters() {
        AllDataValid microCheck = this.getTestInstance();
        Instant now = Instant.now();
        when(this.device.getLoadProfiles()).thenReturn(Collections.emptyList());
        Register register1 = mock(Register.class);
        when(this.deviceValidation.allDataValidated(register1, now)).thenReturn(true);
        when(register1.getDevice()).thenReturn(this.device);
        NumericalReading lastReading1 = mock(NumericalReading.class);
        when(lastReading1.getTimeStamp()).thenReturn(now);
        when(register1.getLastReading()).thenReturn(Optional.of(lastReading1));
        Register register2 = mock(Register.class);
        when(this.deviceValidation.allDataValidated(register2, now)).thenReturn(true);
        when(register2.getDevice()).thenReturn(this.device);
        NumericalReading lastReading2 = mock(NumericalReading.class);
        when(lastReading2.getTimeStamp()).thenReturn(now);
        when(register2.getLastReading()).thenReturn(Optional.of(lastReading2));
        when(this.device.getRegisters()).thenReturn(Arrays.asList(register1, register2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, now);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithSomeRegistersWithUnvalidatedData() {
        AllDataValid microCheck = this.getTestInstance();
        Instant now = Instant.now();
        when(this.device.getLoadProfiles()).thenReturn(Collections.emptyList());
        Register register1 = mock(Register.class);
        when(this.deviceValidation.allDataValidated(register1, now)).thenReturn(true);
        when(register1.getDevice()).thenReturn(this.device);
        NumericalReading lastReading1 = mock(NumericalReading.class);
        when(lastReading1.getTimeStamp()).thenReturn(now);
        when(register1.getLastReading()).thenReturn(Optional.of(lastReading1));
        Register register2 = mock(Register.class);
        when(this.deviceValidation.allDataValidated(register2, now)).thenReturn(false);
        when(register2.getDevice()).thenReturn(this.device);
        NumericalReading lastReading2 = mock(NumericalReading.class);
        when(lastReading2.getTimeStamp()).thenReturn(now);
        when(register2.getLastReading()).thenReturn(Optional.of(lastReading2));
        when(this.device.getRegisters()).thenReturn(Arrays.asList(register1, register2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, now);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_DATA_VALID);
    }

    private AllDataValid getTestInstance() {
        return new AllDataValid(this.thesaurus);
    }

}