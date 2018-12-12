/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationPropertyProvider;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.ChannelEstimationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceService;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceEstimationPropertyResolverTest {

    private static final Long DEVICE_ID = 13L;

    @Mock
    private DeviceService deviceService;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private Device device;
    @Mock
    private Meter meter;
    @Mock
    private DeviceEstimation deviceEstimation;
    @Mock
    private ReadingType readingType1, readingType2;

    private DeviceEstimationPropertyResolver resolver;

    @Before
    public void setUp() {
        this.resolver = new DeviceEstimationPropertyResolver(deviceService);
    }

    @Test
    public void resolve() {
        when(meter.getAmrId()).thenReturn(DEVICE_ID.toString());
        when(channelsContainer.getMeter()).thenReturn(Optional.of(meter));
        when(channelsContainer.getReadingTypes(any())).thenReturn(Collections.singleton(readingType1));
        when(deviceService.findDeviceById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(device.forEstimation()).thenReturn(deviceEstimation);
        doReturn(Arrays.asList(
                mockOverriddenProperties("r1", "com...estimator", readingType1, ImmutableMap.of("prop1", "value1")),
                mockOverriddenProperties("r2", "com...estimator", readingType2, ImmutableMap.of("prop2", "value2"))
        )).when(deviceEstimation).findAllOverriddenProperties();

        // Business method
        Optional<EstimationPropertyProvider> estimationPropertyProvider = resolver.resolve(channelsContainer);

        // Asserts
        assertThat(estimationPropertyProvider).isPresent();
        Map<String, Object> properties;
        properties = estimationPropertyProvider.get().getProperties(mockEstimationRule("r1", "com...estimator"), readingType1);
        assertThat(properties).containsExactly(MapEntry.entry("prop1", "value1"));
        properties = estimationPropertyProvider.get().getProperties(mockEstimationRule("r2", "com...estimator"), readingType2);
        assertThat(properties).isEmpty();
    }

    @Test
    public void resolveNothingForWrongChannelsContainer() {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());

        // Business method
        Optional<EstimationPropertyProvider> estimationPropertyProvider = resolver.resolve(channelsContainer);

        // Asserts
        assertThat(estimationPropertyProvider).isEmpty();
    }

    @Test
    public void getLevel() {
        // Business method
        EstimationPropertyDefinitionLevel level = resolver.getLevel();

        // Asserts
        assertThat(level).isEqualTo(EstimationPropertyDefinitionLevel.TARGET_OBJECT);
    }

    private EstimationRule mockEstimationRule(String ruleName, String ruleImpl) {
        EstimationRule estimationRule = mock(EstimationRule.class);
        when(estimationRule.getName()).thenReturn(ruleName);
        when(estimationRule.getImplementation()).thenReturn(ruleImpl);
        return estimationRule;
    }

    private ChannelEstimationRuleOverriddenProperties mockOverriddenProperties(String ruleName, String ruleImpl, ReadingType readingType, Map<String, Object> props) {
        ChannelEstimationRuleOverriddenProperties overriddenProperties = mock(ChannelEstimationRuleOverriddenProperties.class);
        when(overriddenProperties.getEstimationRuleName()).thenReturn(ruleName);
        when(overriddenProperties.getEstimatorImpl()).thenReturn(ruleImpl);
        when(overriddenProperties.getReadingType()).thenReturn(readingType);
        when(overriddenProperties.getProperties()).thenReturn(props);
        return overriddenProperties;
    }
}
