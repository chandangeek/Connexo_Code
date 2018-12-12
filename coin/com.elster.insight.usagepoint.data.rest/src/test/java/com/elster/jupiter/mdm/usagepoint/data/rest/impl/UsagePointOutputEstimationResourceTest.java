/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.mdm.usagepoint.data.ChannelEstimationRuleOverriddenProperties;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointEstimation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointOutputEstimationResourceTest extends UsagePointDataRestApplicationJerseyTest {

    private static final String USAGEPOINT_NAME = "UP001";
    private static final Long CONTRACT_ID = 13L;
    private static final Long OUTPUT_ID = 16L;
    private static final String URL = "/usagepoints/" + USAGEPOINT_NAME + "/purposes/" + CONTRACT_ID + "/outputs/" + OUTPUT_ID + "/estimation";

    private static final Long ESTIMATION_RULE_ID = 161L;
    private static final String ESTIMATION_RULE_NAME = "er1";
    private static final String REQUIRED_PROPERTY = "required.property";
    private static final String OPTIONAL_PROPERTY = "optional.property";
    private static final String READINGTYPE_MRID = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final int RULE_REQUIRED_PROP_VALUE = 1;
    private static final int RULE_OPT_PROP_VALUE = 2;

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ReadingTypeDeliverable readingTypeDeliverable;

    private ReadingType readingType = mockReadingType(READINGTYPE_MRID);
    @Mock
    private EstimationRuleSet estimationRuleSet;
    @Mock
    private EstimationRule estimationRule;
    @Mock
    private Estimator estimator;
    @Mock
    private UsagePointEstimation usagePointEstimation;
    @Mock
    private UsagePointEstimation.PropertyOverrider propertyOverrider;

    @Before
    public void before() {
        when(meteringService.findUsagePointByName(USAGEPOINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(usagePoint.getName()).thenReturn(USAGEPOINT_NAME);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(metrologyContract.getId()).thenReturn(CONTRACT_ID);
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(readingTypeDeliverable));
        when(readingTypeDeliverable.getId()).thenReturn(OUTPUT_ID);
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        when(readingTypeDeliverable.getMetrologyContract()).thenReturn(metrologyContract);
        doReturn(Optional.of(estimationRule)).when(estimationService).getEstimationRule(ESTIMATION_RULE_ID);
        when(usagePointService.forEstimation(usagePoint)).thenReturn(usagePointEstimation);
        when(usagePointEstimation.overridePropertiesFor(estimationRule, readingType)).thenReturn(propertyOverrider);
        mockPropertyValueInfoService();
        mockEstimationRule();
    }

    private void mockEstimationRule() {
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(estimationRuleSet));
        doReturn(Collections.singletonList(estimationRule)).when(estimationRuleSet).getRules(eq(ImmutableSet.of(readingType)));
        when(estimationRule.getId()).thenReturn(ESTIMATION_RULE_ID);
        when(estimationRule.getName()).thenReturn(ESTIMATION_RULE_NAME);
        when(estimationRule.getReadingTypes()).thenReturn(Collections.singleton(readingType));
        when(estimationRule.getImplementation()).thenReturn("com...estimator");
        when(estimationService.getEstimator("com...estimator")).thenReturn(Optional.of(estimator));
        List<PropertySpec> rulePropertySpecs = Arrays.asList(mockPropertySpec(REQUIRED_PROPERTY), mockPropertySpec(OPTIONAL_PROPERTY));
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
        doReturn(Optional.of(overriddenProperties)).when(usagePointEstimation).findOverriddenProperties(estimationRule, readingType);

        // Business method
        String response = target(URL).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.estimation[0].id")).isEqualTo(14);
        assertThat(jsonModel.<Number>get("$.estimation[0].version")).isEqualTo(15);
        assertThat(jsonModel.<Number>get("$.estimation[0].ruleId")).isEqualTo(ESTIMATION_RULE_ID.intValue());
        assertThat(jsonModel.<String>get("$.estimation[0].name")).isEqualTo(ESTIMATION_RULE_NAME);
        assertThat(jsonModel.<String>get("$.estimation[0].readingType.mRID")).isEqualTo(READINGTYPE_MRID);
        assertThat(jsonModel.<Boolean>get("$.estimation[0].isActive")).isFalse();
        assertThat(jsonModel.<String>get("$.estimation[0].properties[0].key")).isEqualTo(REQUIRED_PROPERTY);
        assertThat(jsonModel.<Number>get("$.estimation[0].properties[0].propertyValueInfo.value")).isEqualTo(11);
        assertThat(jsonModel.<Number>get("$.estimation[0].properties[0].propertyValueInfo.inheritedValue")).isEqualTo(RULE_REQUIRED_PROP_VALUE);
        assertThat(jsonModel.<Boolean>get("$.estimation[0].properties[0].overridden")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.estimation[0].properties[0].canBeOverridden")).isTrue();
        assertThat(jsonModel.<String>get("$.estimation[0].properties[1].key")).isEqualTo(OPTIONAL_PROPERTY);
        assertThat(jsonModel.<Number>get("$.estimation[0].properties[1].propertyValueInfo.value")).isNull();
        assertThat(jsonModel.<Number>get("$.estimation[0].properties[1].propertyValueInfo.inheritedValue")).isEqualTo(RULE_OPT_PROP_VALUE);
        assertThat(jsonModel.<Boolean>get("$.estimation[0].properties[1].overridden")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.estimation[0].properties[1].canBeOverridden")).isTrue();
    }

    @Test
    public void getEstimationRuleOverriddenProperties() {
        ChannelEstimationRuleOverriddenProperties overriddenProperties = mock(ChannelEstimationRuleOverriddenProperties.class);
        when(overriddenProperties.getId()).thenReturn(14L);
        when(overriddenProperties.getVersion()).thenReturn(15L);
        when(overriddenProperties.getProperties()).thenReturn(ImmutableMap.of(REQUIRED_PROPERTY, 11));

        doReturn(Optional.of(overriddenProperties)).when(usagePointEstimation).findOverriddenProperties(estimationRule, readingType);
        // Business method
        String response = target(URL + "/" + ESTIMATION_RULE_ID).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(14);
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(15);
        assertThat(jsonModel.<Number>get("$.ruleId")).isEqualTo(ESTIMATION_RULE_ID.intValue());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo(ESTIMATION_RULE_NAME);
        assertThat(jsonModel.<String>get("$.readingType.mRID")).isEqualTo(READINGTYPE_MRID);
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
        ChannelEstimationRuleInfo info = new ChannelEstimationRuleInfo();
        info.ruleId = ESTIMATION_RULE_ID;
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
        doReturn(Optional.of(overriddenProperties)).when(usagePointEstimation).findAndLockChannelEstimationRuleOverriddenProperties(info.id, info.version);

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

        ChannelEstimationRuleOverriddenProperties overriddenProperties = mock(ChannelEstimationRuleOverriddenProperties.class);
        doReturn(Optional.of(overriddenProperties)).when(usagePointEstimation).findAndLockChannelEstimationRuleOverriddenProperties(info.id, info.version);

        // Business method
        Response response = target(URL + "/" + ESTIMATION_RULE_ID).request().method("DELETE", Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(overriddenProperties).delete();
    }
}
