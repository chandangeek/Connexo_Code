package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.DeviceDataService;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
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
    private DeviceDataService deviceDataService;
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
        when(amrSystem.getId()).thenReturn(1);

        when(meter.getAmrId()).thenReturn(Long.toString(DEVICE_ID));
        when(deviceDataService.findDeviceById(eq(DEVICE_ID))).thenReturn(device);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getValidationRuleSets()).thenReturn(Arrays.asList(ruleSet));

    }

    @Test
    public void testResolveForDevice() {
        DeviceConfigValidationRuleSetResolver resolver = new DeviceConfigValidationRuleSetResolver();
        resolver.setDeviceDataService(deviceDataService);

        List<ValidationRuleSet> setList = resolver.resolve(meterActivation, Interval.sinceEpoch());
        assertThat(setList).containsExactly(ruleSet);
    }

    @Test
    public void testDeviceNotFound() {
        when(deviceDataService.findDeviceById(DEVICE_ID)).thenReturn(null);
        DeviceConfigValidationRuleSetResolver resolver = new DeviceConfigValidationRuleSetResolver();
        resolver.setDeviceDataService(deviceDataService);

        List<ValidationRuleSet> setList = resolver.resolve(meterActivation, Interval.sinceEpoch());
        assertThat(setList).isEmpty();
    }

    @Test
    public void testNotMdc() {
        when(amrSystem.getId()).thenReturn(2);
        DeviceConfigValidationRuleSetResolver resolver = new DeviceConfigValidationRuleSetResolver();
        resolver.setDeviceDataService(deviceDataService);

        List<ValidationRuleSet> setList = resolver.resolve(meterActivation, Interval.sinceEpoch());
        assertThat(setList).isEmpty();
    }

    @Test
    public void testNoMeter() {
        when(meterActivation.getMeter()).thenReturn(Optional.<Meter>absent());
        DeviceConfigValidationRuleSetResolver resolver = new DeviceConfigValidationRuleSetResolver();
        resolver.setDeviceDataService(deviceDataService);

        List<ValidationRuleSet> setList = resolver.resolve(meterActivation, Interval.sinceEpoch());
        assertThat(setList).isEmpty();
    }
}
