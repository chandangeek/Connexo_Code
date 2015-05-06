package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 25/06/2014
 * Time: 14:01
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigValidationRuleSetResolverTest {

    private static final long DEVICE_ID = 345464;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private DeviceService deviceService;
    @Mock
    private Meter meter;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private com.energyict.mdc.device.data.Device device;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ValidationRuleSet ruleSet;

    @Before
    public void setUp() throws Exception {
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);

        when(meter.getAmrId()).thenReturn(Long.toString(DEVICE_ID));
        when(deviceService.findDeviceById(eq(DEVICE_ID))).thenReturn(Optional.of(device));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getValidationRuleSets()).thenReturn(Arrays.asList(ruleSet));

    }

    @Test
    public void testResolveForDevice() {
        DeviceConfigValidationRuleSetResolver resolver = new DeviceConfigValidationRuleSetResolver();
        resolver.setDeviceService(deviceService);

        List<ValidationRuleSet> setList = resolver.resolve(meterActivation);
        assertThat(setList).containsExactly(ruleSet);
    }

    @Test
    public void testDeviceNotFound() {
        when(deviceService.findDeviceById(DEVICE_ID)).thenReturn(Optional.<Device>empty());
        DeviceConfigValidationRuleSetResolver resolver = new DeviceConfigValidationRuleSetResolver();
        resolver.setDeviceService(deviceService);

        List<ValidationRuleSet> setList = resolver.resolve(meterActivation);
        assertThat(setList).isEmpty();
    }

    @Test
    public void testNotMdc() {
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(false);
        DeviceConfigValidationRuleSetResolver resolver = new DeviceConfigValidationRuleSetResolver();
        resolver.setDeviceService(deviceService);

        List<ValidationRuleSet> setList = resolver.resolve(meterActivation);
        assertThat(setList).isEmpty();
    }

    @Test
    public void testNoMeter() {
        when(meterActivation.getMeter()).thenReturn(Optional.<Meter>empty());
        DeviceConfigValidationRuleSetResolver resolver = new DeviceConfigValidationRuleSetResolver();
        resolver.setDeviceService(deviceService);

        List<ValidationRuleSet> setList = resolver.resolve(meterActivation);
        assertThat(setList).isEmpty();
    }
}
