/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.ChannelEstimationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.IntegerFactory;

import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChannelEstimationResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final String DEVICE_NAME = "SPE001";
    private static final Long CHANNEL_ID = 131L;
    private static final String COLLECTED_READINGTYPE_MRID = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String CALCULATED_READINGTYPE_MRID = "0.0.4.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final Long ESTIMATION_RULE_ID = 161L;
    private static final String ESTIMATION_RULE_NAME = "er1";
    private static final String REQUIRED_PROPERTY = "required.property";
    private static final String OPTIONAL_PROPERTY = "optional.property";
    private static final int RULE_REQUIRED_PROP_VALUE = 1;
    private static final int RULE_OPT_PROP_VALUE = 2;

    private static final String URL = "/devices/" + DEVICE_NAME + "/channels/" + CHANNEL_ID + "/estimation";

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
    private EstimationRuleSet estimationRuleSet;
    @Mock
    private EstimationRule estimationRule;
    @Mock
    private Estimator estimator;
    @Mock
    private DeviceEstimation deviceEstimation;
    @Mock
    private DeviceEstimation.PropertyOverrider propertyOverrider;

    @Before
    public void before() {
        // mock device
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        when(device.getName()).thenReturn(DEVICE_NAME);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.forEstimation()).thenReturn(deviceEstimation);

        // mock channels
        when(device.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(channel.getReadingType()).thenReturn(collectedReadingType);
        when(channel.getCalculatedReadingType(any())).thenReturn(Optional.of(calculatedReadingType));

        // mock estimation configuration
        when(deviceConfiguration.getEstimationRuleSets()).thenReturn(Collections.singletonList(estimationRuleSet));
        doReturn(Collections.singletonList(estimationRule)).when(estimationRuleSet).getRules(Collections.singleton(collectedReadingType));
        doReturn(Collections.singletonList(estimationRule)).when(estimationRuleSet).getRules(Collections.singleton(calculatedReadingType));
        doReturn(Optional.of(estimationRule)).when(estimationService).getEstimationRule(ESTIMATION_RULE_ID);
        when(deviceEstimation.overridePropertiesFor(eq(estimationRule), any())).thenReturn(propertyOverrider);

        mockEstimationRule();
    }

    private void mockEstimationRule() {
        when(estimationRule.getId()).thenReturn(ESTIMATION_RULE_ID);
        when(estimationRule.getName()).thenReturn(ESTIMATION_RULE_NAME);
        when(estimationRule.getReadingTypes()).thenReturn(Collections.singleton(collectedReadingType));
        when(estimationRule.getImplementation()).thenReturn("com...estimator");
        when(estimationService.getEstimator("com...estimator")).thenReturn(Optional.of(estimator));
        ValueFactory valueFactory = new IntegerFactory();
        List<PropertySpec> rulePropertySpecs = Arrays.asList(mockPropertySpec(REQUIRED_PROPERTY, valueFactory), mockPropertySpec(OPTIONAL_PROPERTY, valueFactory));
        when(estimationRule.getPropertySpecs(EstimationPropertyDefinitionLevel.TARGET_OBJECT)).thenReturn(rulePropertySpecs);
        when(estimationRule.getPropertySpecs()).thenReturn(rulePropertySpecs);
        when(estimationRule.getProps()).thenReturn(ImmutableMap.of(REQUIRED_PROPERTY, RULE_REQUIRED_PROP_VALUE, OPTIONAL_PROPERTY, RULE_OPT_PROP_VALUE));
    }

    @Test
    public void getEstimationConfiguration() {
        ChannelEstimationRuleOverriddenProperties overriddenProperties = mock(ChannelEstimationRuleOverriddenProperties.class);
        when(overriddenProperties.getId()).thenReturn(14L);
        when(overriddenProperties.getVersion()).thenReturn(15L);
        when(overriddenProperties.getProperties()).thenReturn(ImmutableMap.of(REQUIRED_PROPERTY, 11));
        doReturn(Optional.of(overriddenProperties)).when(deviceEstimation).findOverriddenProperties(estimationRule, collectedReadingType);
        doReturn(Optional.empty()).when(deviceEstimation).findOverriddenProperties(estimationRule, calculatedReadingType);

        // Business method
        String response = target(URL).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);

        JsonModel collectedRTSubModel = jsonModel.getSubModel("$.rulesForCollectedReadingType[0]");

        assertThat(collectedRTSubModel.<Number>get("$.id")).isEqualTo(14);
        assertThat(collectedRTSubModel.<Number>get("$.version")).isEqualTo(15);
        assertThat(collectedRTSubModel.<Number>get("$.ruleId")).isEqualTo(ESTIMATION_RULE_ID.intValue());
        assertThat(collectedRTSubModel.<String>get("$.name")).isEqualTo(ESTIMATION_RULE_NAME);
        assertThat(collectedRTSubModel.<String>get("$.readingType.mRID")).isEqualTo(COLLECTED_READINGTYPE_MRID);
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
        assertThat(calculatedRTSubModel.<Number>get("$.ruleId")).isEqualTo(ESTIMATION_RULE_ID.intValue());
        assertThat(calculatedRTSubModel.<String>get("$.name")).isEqualTo(ESTIMATION_RULE_NAME);
        assertThat(calculatedRTSubModel.<String>get("$.readingType.mRID")).isEqualTo(CALCULATED_READINGTYPE_MRID);
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
    public void getEstimationRuleOverriddenProperties() {
        ChannelEstimationRuleOverriddenProperties overriddenProperties = mock(ChannelEstimationRuleOverriddenProperties.class);
        when(overriddenProperties.getId()).thenReturn(14L);
        when(overriddenProperties.getVersion()).thenReturn(15L);
        when(overriddenProperties.getProperties()).thenReturn(ImmutableMap.of(REQUIRED_PROPERTY, 11));

        doReturn(Optional.of(overriddenProperties)).when(deviceEstimation).findOverriddenProperties(estimationRule, collectedReadingType);

        // Business method
        String response = target(URL + "/" + ESTIMATION_RULE_ID).queryParam("readingType", COLLECTED_READINGTYPE_MRID).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(14);
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(15);
        assertThat(jsonModel.<Number>get("$.ruleId")).isEqualTo(ESTIMATION_RULE_ID.intValue());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo(ESTIMATION_RULE_NAME);
        assertThat(jsonModel.<String>get("$.readingType.mRID")).isEqualTo(COLLECTED_READINGTYPE_MRID);
        assertThat(jsonModel.<Boolean>get("$.isActive")).isFalse();
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo(REQUIRED_PROPERTY);
        assertThat(jsonModel.<Number>get("$.properties[0].propertyValueInfo.value")).isEqualTo(11);
        assertThat(jsonModel.<Number>get("$.properties[0].propertyValueInfo.inheritedValue")).isEqualTo(RULE_REQUIRED_PROP_VALUE);
        assertThat(jsonModel.<Boolean>get("$.properties[0].overridden")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.properties[0].canBeOverridden")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.properties[1].key")).isEqualTo(OPTIONAL_PROPERTY);
        assertThat(jsonModel.<Number>get("$.properties[1].propertyValueInfo.value")).isNull();
        assertThat(jsonModel.<Number>get("$.properties[1].propertyValueInfo.inheritedValue")).isEqualTo(RULE_OPT_PROP_VALUE);
        assertThat(jsonModel.<Boolean>get("$.properties[1].overridden")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.properties[1].canBeOverridden")).isTrue();
    }

    @Test
    public void overrideProperties() {
        ChannelEstimationRuleInfo info = new ChannelEstimationRuleInfo();
        info.ruleId = ESTIMATION_RULE_ID;
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
        ChannelEstimationRuleInfo info = new ChannelEstimationRuleInfo();
        info.id = 17L;
        info.version = 1L;
        info.ruleId = ESTIMATION_RULE_ID;
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

        ChannelEstimationRuleOverriddenProperties overriddenProperties = mock(ChannelEstimationRuleOverriddenProperties.class);
        doReturn(Optional.of(overriddenProperties)).when(deviceEstimation).findAndLockChannelEstimationRuleOverriddenProperties(info.id, info.version);

        // Business method
        Response response = target(URL + "/" + ESTIMATION_RULE_ID).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(overriddenProperties).setProperties(eq(ImmutableMap.of(REQUIRED_PROPERTY, 10)));
        verify(overriddenProperties).update();
    }

    @Test
    public void deleteOverriddenProperties() {
        ChannelEstimationRuleInfo info = new ChannelEstimationRuleInfo();
        info.id = 17L;
        info.version = 1L;
        info.ruleId = ESTIMATION_RULE_ID;
        info.readingType = new ReadingTypeInfo();
        info.readingType.mRID = COLLECTED_READINGTYPE_MRID;

        ChannelEstimationRuleOverriddenProperties overriddenProperties = mock(ChannelEstimationRuleOverriddenProperties.class);
        doReturn(Optional.of(overriddenProperties)).when(deviceEstimation).findAndLockChannelEstimationRuleOverriddenProperties(info.id, info.version);

        // Business method
        Response response = target(URL + "/" + ESTIMATION_RULE_ID).request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(overriddenProperties).delete();
    }
}
