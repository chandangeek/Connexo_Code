/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationPropertyProvider;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.device.data.ChannelValidationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.DeviceValidation;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceValidationPropertyResolverTest {

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
    private DeviceValidation deviceValidation;
    @Mock
    private ReadingType readingType1, readingType2;

    private DeviceValidationPropertyResolver resolver;

    @Before
    public void setUp() {
        this.resolver = new DeviceValidationPropertyResolver(deviceService);
    }

    @Test
    public void resolve() {
        when(meter.getAmrId()).thenReturn(DEVICE_ID.toString());
        when(channelsContainer.getMeter()).thenReturn(Optional.of(meter));
        when(channelsContainer.getReadingTypes(any())).thenReturn(Collections.singleton(readingType1));
        when(deviceService.findDeviceById(DEVICE_ID)).thenReturn(Optional.of(device));
        when(device.forValidation()).thenReturn(deviceValidation);
        doReturn(Arrays.asList(
                mockOverriddenProperties("r1", "com...validator", ValidationAction.FAIL, readingType1, ImmutableMap.of("prop1", "value1")),
                mockOverriddenProperties("r2", "com...validator", ValidationAction.WARN_ONLY, readingType2, ImmutableMap.of("prop2", "value2"))
        )).when(deviceValidation).findAllOverriddenProperties();

        // Business method
        Optional<ValidationPropertyProvider> validationPropertyProvider = resolver.resolve(channelsContainer);

        // Asserts
        assertThat(validationPropertyProvider).isPresent();
        Map<String, Object> properties;
        properties = validationPropertyProvider.get()
                .getProperties(mockValidationRule("r1", "com...validator", ValidationAction.FAIL), readingType1);
        assertThat(properties).containsExactly(MapEntry.entry("prop1", "value1"));
        properties = validationPropertyProvider.get()
                .getProperties(mockValidationRule("r2", "com...validator", ValidationAction.WARN_ONLY), readingType2);
        assertThat(properties).isEmpty();
    }

    @Test
    public void resolveNothingForWrongChannelsContainer() {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());

        // Business method
        Optional<ValidationPropertyProvider> validationPropertyProvider = resolver.resolve(channelsContainer);

        // Asserts
        assertThat(validationPropertyProvider).isEmpty();
    }

    @Test
    public void getLevel() {
        // Business method
        ValidationPropertyDefinitionLevel level = resolver.getLevel();

        // Asserts
        assertThat(level).isEqualTo(ValidationPropertyDefinitionLevel.TARGET_OBJECT);
    }

    private ValidationRule mockValidationRule(String ruleName, String ruleImpl, ValidationAction validationAction) {
        ValidationRule validationRule = mock(ValidationRule.class);
        when(validationRule.getName()).thenReturn(ruleName);
        when(validationRule.getImplementation()).thenReturn(ruleImpl);
        when(validationRule.getAction()).thenReturn(validationAction);
        return validationRule;
    }

    private ChannelValidationRuleOverriddenProperties mockOverriddenProperties(String ruleName, String ruleImpl, ValidationAction validationAction,
                                                                               ReadingType readingType, Map<String, Object> props) {
        ChannelValidationRuleOverriddenProperties overriddenProperties = mock(ChannelValidationRuleOverriddenProperties.class);
        when(overriddenProperties.getValidationRuleName()).thenReturn(ruleName);
        when(overriddenProperties.getValidatorImpl()).thenReturn(ruleImpl);
        when(overriddenProperties.getValidationAction()).thenReturn(validationAction);
        when(overriddenProperties.getReadingType()).thenReturn(readingType);
        when(overriddenProperties.getProperties()).thenReturn(props);
        return overriddenProperties;
    }
}
