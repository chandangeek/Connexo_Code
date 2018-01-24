/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.ChannelValidationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.IntegerFactory;

import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class ChannelValidationResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final String DEVICE_NAME = "SPE001";
    private static final Long CHANNEL_ID = 131L;
    private static final Long REGISTER_ID = 132L;
    private static final String COLLECTED_READINGTYPE_MRID = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String CALCULATED_READINGTYPE_MRID = "0.0.4.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final Long VALIDATION_RULE_ID = 161L;
    private static final String VALIDATION_RULE_NAME = "vr1";
    private static final String REQUIRED_PROPERTY = "required.property";
    private static final String OPTIONAL_PROPERTY = "optional.property";
    private static final int RULE_REQUIRED_PROP_VALUE = 1;
    private static final int RULE_OPT_PROP_VALUE = 2;

    private String URL;

    private ReadingType collectedReadingType = mockReadingType(COLLECTED_READINGTYPE_MRID);
    private ReadingType calculatedReadingType = mockReadingType(CALCULATED_READINGTYPE_MRID);

    @Mock
    private Device device;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private Channel channel;
    @Mock
    private Register register;
    @Mock
    private ValidationRuleSet validationRuleSet;
    @Mock
    private ValidationRuleSetVersion validationRuleSetVersion;
    @Mock
    private ValidationRule validationRule;
    @Mock
    private Validator validator;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private DeviceValidation.PropertyOverrider propertyOverrider;

    public ChannelValidationResourceTest(String URL) {
        this.URL = URL;
    }

    @Parameterized.Parameters
    public static Collection urls() {
        return Arrays.asList(new String[][]{
                {"/devices/" + DEVICE_NAME + "/channels/" + CHANNEL_ID + "/validation"},
                {"/devices/" + DEVICE_NAME + "/registers/" + REGISTER_ID + "/validation"}
        });
    }

    @Before
    public void before() {
        // mock device
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        when(device.getName()).thenReturn(DEVICE_NAME);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.forValidation()).thenReturn(deviceValidation);
        doReturn(Optional.of(meterActivation)).when(device).getCurrentMeterActivation();
        when(deviceValidation.isValidationActive()).thenReturn(true);

        // mock channels
        when(device.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(channel.getReadingType()).thenReturn(collectedReadingType);
        when(channel.getCalculatedReadingType(any())).thenReturn(Optional.of(calculatedReadingType));

        // mock registers
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        when(register.getRegisterSpecId()).thenReturn(REGISTER_ID);
        when(register.getReadingType()).thenReturn(collectedReadingType);
        when(register.getCalculatedReadingType(any())).thenReturn(Optional.of(calculatedReadingType));

        // mock validation configuration
        when(deviceConfiguration.getValidationRules(any())).thenReturn(Collections.singletonList(validationRule));
        doReturn(Optional.of(validationRule)).when(validationService).findValidationRule(VALIDATION_RULE_ID);
        when(deviceValidation.overridePropertiesFor(eq(validationRule), any())).thenReturn(propertyOverrider);

        mockValidationRule();
    }

    private void mockValidationRule() {
        when(validationRule.getId()).thenReturn(VALIDATION_RULE_ID);
        when(validationRule.getName()).thenReturn(VALIDATION_RULE_NAME);
        when(validationRule.getReadingTypes()).thenReturn(Collections.singleton(collectedReadingType));
        when(validationRule.getImplementation()).thenReturn("com...validator");
        when(validationService.getValidator("com...validator")).thenReturn(validator);
        ValueFactory valueFactory = new IntegerFactory();
        List<PropertySpec> rulePropertySpecs = Arrays.asList(mockPropertySpec(REQUIRED_PROPERTY, valueFactory), mockPropertySpec(OPTIONAL_PROPERTY, valueFactory));
        when(validationRule.getPropertySpecs(ValidationPropertyDefinitionLevel.TARGET_OBJECT)).thenReturn(rulePropertySpecs);
        when(validationRule.getPropertySpecs()).thenReturn(rulePropertySpecs);
        when(validationRule.getRuleSetVersion()).thenReturn(validationRuleSetVersion);
        when(validationRuleSetVersion.getStatus()).thenReturn(ValidationVersionStatus.CURRENT);
        when(validationRule.getProps()).thenReturn(ImmutableMap.of(REQUIRED_PROPERTY, RULE_REQUIRED_PROP_VALUE, OPTIONAL_PROPERTY, RULE_OPT_PROP_VALUE));
    }

    @Test
    public void getValidationConfiguration() {
        ChannelValidationRuleOverriddenProperties overriddenProperties = mock(ChannelValidationRuleOverriddenProperties.class);
        when(overriddenProperties.getId()).thenReturn(14L);
        when(overriddenProperties.getVersion()).thenReturn(15L);
        when(overriddenProperties.getProperties()).thenReturn(ImmutableMap.of(REQUIRED_PROPERTY, 11));
        when(overriddenProperties.getDevice()).thenReturn(device);
        doReturn(Optional.of(overriddenProperties)).when(deviceValidation).findOverriddenProperties(validationRule, collectedReadingType);
        doReturn(Optional.empty()).when(deviceValidation).findOverriddenProperties(validationRule, calculatedReadingType);

        // Business method
        String response = target(URL).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);

        JsonModel collectedRTSubModel = jsonModel.getSubModel("$.rulesForCollectedReadingType[0]");

        assertThat(collectedRTSubModel.<Number>get("$.id")).isEqualTo(14);
        assertThat(collectedRTSubModel.<Number>get("$.version")).isEqualTo(15);
        assertThat(collectedRTSubModel.<Number>get("$.ruleId")).isEqualTo(VALIDATION_RULE_ID.intValue());
        assertThat(collectedRTSubModel.<String>get("$.name")).isEqualTo(VALIDATION_RULE_NAME);
        assertThat(collectedRTSubModel.<String>get("$.readingType.mRID")).isEqualTo(COLLECTED_READINGTYPE_MRID);
        assertThat(collectedRTSubModel.<Boolean>get("$.isEffective")).isTrue();
        assertThat(collectedRTSubModel.<Boolean>get("$.isActive")).isFalse();
        assertThat(collectedRTSubModel.<String>get("$.properties[0].key")).isEqualTo(REQUIRED_PROPERTY);
        assertThat(collectedRTSubModel.<Number>get("$.properties[0].propertyValueInfo.value")).isEqualTo(11);
        assertThat(collectedRTSubModel.<Number>get("$.properties[0].propertyValueInfo.inheritedValue")).isEqualTo(RULE_REQUIRED_PROP_VALUE);
        assertThat(collectedRTSubModel.<Boolean>get("$.properties[0].overridden")).isTrue();
        assertThat(collectedRTSubModel.<Boolean>get("$.properties[0].canBeOverridden")).isEqualTo(true);
        assertThat(collectedRTSubModel.<String>get("$.properties[1].key")).isEqualTo(OPTIONAL_PROPERTY);
        assertThat(collectedRTSubModel.<Number>get("$.properties[1].propertyValueInfo.value")).isNull();
        assertThat(collectedRTSubModel.<Number>get("$.properties[1].propertyValueInfo.inheritedValue")).isEqualTo(RULE_OPT_PROP_VALUE);
        assertThat(collectedRTSubModel.<Boolean>get("$.properties[1].overridden")).isFalse();
        assertThat(collectedRTSubModel.<Boolean>get("$.properties[1].canBeOverridden")).isTrue();

        JsonModel calculatedRTSubModel = jsonModel.getSubModel("$.rulesForCalculatedReadingType[0]");

        assertThat(calculatedRTSubModel.<Number>get("$.id")).isNull();
        assertThat(calculatedRTSubModel.<Number>get("$.version")).isNull();
        assertThat(calculatedRTSubModel.<Number>get("$.ruleId")).isEqualTo(VALIDATION_RULE_ID.intValue());
        assertThat(calculatedRTSubModel.<String>get("$.name")).isEqualTo(VALIDATION_RULE_NAME);
        assertThat(calculatedRTSubModel.<String>get("$.readingType.mRID")).isEqualTo(CALCULATED_READINGTYPE_MRID);
        assertThat(calculatedRTSubModel.<Boolean>get("$.isEffective")).isTrue();
        assertThat(calculatedRTSubModel.<Boolean>get("$.isActive")).isFalse();
        assertThat(calculatedRTSubModel.<String>get("$.properties[0].key")).isEqualTo(REQUIRED_PROPERTY);
        assertThat(calculatedRTSubModel.<Number>get("$.properties[0].propertyValueInfo.value")).isNull();
        assertThat(calculatedRTSubModel.<Number>get("$.properties[0].propertyValueInfo.inheritedValue")).isEqualTo(RULE_REQUIRED_PROP_VALUE);
        assertThat(calculatedRTSubModel.<Boolean>get("$.properties[0].overridden")).isFalse();
        assertThat(calculatedRTSubModel.<Boolean>get("$.properties[0].canBeOverridden")).isTrue();
        assertThat(calculatedRTSubModel.<String>get("$.properties[1].key")).isEqualTo(OPTIONAL_PROPERTY);
        assertThat(calculatedRTSubModel.<Number>get("$.properties[1].propertyValueInfo.value")).isNull();
        assertThat(calculatedRTSubModel.<Number>get("$.properties[1].propertyValueInfo.inheritedValue")).isEqualTo(RULE_OPT_PROP_VALUE);
        assertThat(calculatedRTSubModel.<Boolean>get("$.properties[1].overridden")).isFalse();
        assertThat(calculatedRTSubModel.<Boolean>get("$.properties[1].canBeOverridden")).isTrue();
    }

    @Test
    public void getValidationRuleOverriddenProperties() {
        ChannelValidationRuleOverriddenProperties overriddenProperties = mock(ChannelValidationRuleOverriddenProperties.class);
        when(overriddenProperties.getId()).thenReturn(14L);
        when(overriddenProperties.getVersion()).thenReturn(15L);
        when(overriddenProperties.getProperties()).thenReturn(ImmutableMap.of(REQUIRED_PROPERTY, 11));
        when(overriddenProperties.getDevice()).thenReturn(device);
        doReturn(Optional.of(overriddenProperties)).when(deviceValidation).findOverriddenProperties(validationRule, collectedReadingType);

        // Business method
        String response = target(URL + "/" + VALIDATION_RULE_ID).queryParam("readingType", COLLECTED_READINGTYPE_MRID).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(14);
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(15);
        assertThat(jsonModel.<Number>get("$.ruleId")).isEqualTo(VALIDATION_RULE_ID.intValue());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo(VALIDATION_RULE_NAME);
        assertThat(jsonModel.<String>get("$.readingType.mRID")).isEqualTo(COLLECTED_READINGTYPE_MRID);
        assertThat(jsonModel.<Boolean>get("$.isEffective")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.isActive")).isFalse();
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo(REQUIRED_PROPERTY);
        assertThat(jsonModel.<Number>get("$.properties[0].propertyValueInfo.value")).isEqualTo(11);
        assertThat(jsonModel.<Number>get("$.properties[0].propertyValueInfo.inheritedValue")).isEqualTo(RULE_REQUIRED_PROP_VALUE);
        assertThat(jsonModel.<Boolean>get("$.properties[0].overridden")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.properties[0].canBeOverridden")).isTrue();
        assertThat(jsonModel.<String>get("$.properties[1].key")).isEqualTo(OPTIONAL_PROPERTY);
        assertThat(jsonModel.<Number>get("$.properties[1].propertyValueInfo.value")).isNull();
        assertThat(jsonModel.<Number>get("$.properties[1].propertyValueInfo.inheritedValue")).isEqualTo(RULE_OPT_PROP_VALUE);
        assertThat(jsonModel.<Boolean>get("$.properties[1].overridden")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.properties[1].canBeOverridden")).isTrue();
    }

    @Test
    public void overrideProperties() {
        ChannelValidationRuleInfo info = new ChannelValidationRuleInfo();
        info.ruleId = VALIDATION_RULE_ID;
        info.readingType = new ReadingTypeInfo();
        info.readingType.mRID = COLLECTED_READINGTYPE_MRID;
        PropertyInfo requiredPropertyInfo = new PropertyInfo();
        requiredPropertyInfo.key = REQUIRED_PROPERTY;
        requiredPropertyInfo.propertyValueInfo = new PropertyValueInfo<>(10, null, false);
        PropertyInfo optionalPropertyInfo = new PropertyInfo();
        optionalPropertyInfo.key = OPTIONAL_PROPERTY;
        optionalPropertyInfo.propertyValueInfo = new PropertyValueInfo<>();
        info.properties = Arrays.asList(
                new OverriddenPropertyInfo(requiredPropertyInfo, true, false),
                new OverriddenPropertyInfo(optionalPropertyInfo, true, false)
        );

        // Business method
        Response response = target(URL).request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(propertyOverrider).override(REQUIRED_PROPERTY, 10);
        verify(propertyOverrider).complete();
    }

    @Test
    public void updateOverriddenProperties() {
        ChannelValidationRuleInfo info = new ChannelValidationRuleInfo();
        info.id = 17L;
        info.version = 1L;
        info.ruleId = VALIDATION_RULE_ID;
        info.readingType = new ReadingTypeInfo();
        info.readingType.mRID = COLLECTED_READINGTYPE_MRID;
        PropertyInfo requiredPropertyInfo = new PropertyInfo();
        requiredPropertyInfo.key = REQUIRED_PROPERTY;
        requiredPropertyInfo.propertyValueInfo = new PropertyValueInfo<>(10, null, false);
        PropertyInfo optionalPropertyInfo = new PropertyInfo();
        optionalPropertyInfo.key = OPTIONAL_PROPERTY;
        optionalPropertyInfo.propertyValueInfo = new PropertyValueInfo<>();
        info.properties = Arrays.asList(
                new OverriddenPropertyInfo(requiredPropertyInfo, true, false),
                new OverriddenPropertyInfo(optionalPropertyInfo, true, false)
        );

        ChannelValidationRuleOverriddenProperties overriddenProperties = mock(ChannelValidationRuleOverriddenProperties.class);
        doReturn(Optional.of(overriddenProperties)).when(deviceValidation).findAndLockChannelValidationRuleOverriddenProperties(info.id, info.version);

        // Business method
        Response response = target(URL + "/" + VALIDATION_RULE_ID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(overriddenProperties).setProperties(eq(ImmutableMap.of(REQUIRED_PROPERTY, 10)));
        verify(overriddenProperties).update();
    }

    @Test
    public void deleteOverriddenProperties() {
        ChannelValidationRuleInfo info = new ChannelValidationRuleInfo();
        info.id = 17L;
        info.version = 1L;
        info.ruleId = VALIDATION_RULE_ID;
        info.readingType = new ReadingTypeInfo();
        info.readingType.mRID = COLLECTED_READINGTYPE_MRID;

        ChannelValidationRuleOverriddenProperties overriddenProperties = mock(ChannelValidationRuleOverriddenProperties.class);
        doReturn(Optional.of(overriddenProperties)).when(deviceValidation).findAndLockChannelValidationRuleOverriddenProperties(info.id, info.version);

        // Business method
        Response response = target(URL + "/" + VALIDATION_RULE_ID).request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(overriddenProperties).delete();
    }
}
