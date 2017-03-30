/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;

import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigChangeMetrologyConfigRequirementsTest {

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private DeviceImpl device;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceConfiguration destDeviceConfiguration;
    @Mock
    private ReadingType readingType;
    @Mock
    private DeviceType deviceType;

    @Mock
    private ReadingTypeRequirement readingTypeRequirement;

    @Test
    @Expected(DeviceConfigurationChangeException.class)
    public void validateMetrologyConfigRequirementsTest() {
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(metrologyConfiguration.getRequirements()).thenReturn(Collections.singletonList(readingTypeRequirement));
        when(device.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(destDeviceConfiguration)).thenReturn(Collections.singletonList(readingType));
        when(metrologyConfiguration.getMandatoryReadingTypeRequirements()).thenReturn(Collections.singletonList(readingTypeRequirement));
        when(readingTypeRequirement.matches(any(ReadingType.class))).thenReturn(false);
        device.validateMetrologyConfigRequirements(destDeviceConfiguration);
    }
}