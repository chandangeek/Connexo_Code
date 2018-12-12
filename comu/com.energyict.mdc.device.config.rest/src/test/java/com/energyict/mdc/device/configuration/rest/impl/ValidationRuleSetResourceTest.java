/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValidationRuleSetResourceTest extends DeviceConfigurationApplicationJerseyTest {

    private static final String APPLICATION_HEADER_PARAM = "X-CONNEXO-APPLICATION-NAME";

    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;
    private static final long DEVICE_TYPE_ID = 564L;
    private static final long DEVICE_CONFIGURATION_ID = 211L;
    private static final long RULESET_ID_1 = 1L;
    private static final long RULESET_ID_2 = 2L;
    @Mock
    private ValidationRuleSet validationRuleSet1, validationRuleSet2;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ReadingType readingType1, readingType2;
    @Mock
    private ValidationRule rule1, rule2;
    @Mock
    private PropertyValueInfoService propertyValueInfoService;

    @Test
    public void testAddRuleSetsToDeviceConfiguration() throws Exception {
        when(validationRuleSet1.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(validationRuleSet2.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
    	doReturn(Optional.of(validationRuleSet1)).when(validationService).getValidationRuleSet(RULESET_ID_1);
    	doReturn(Optional.of(validationRuleSet2)).when(validationService).getValidationRuleSet(RULESET_ID_2);
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(DEVICE_CONFIGURATION_ID, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findDeviceConfiguration(DEVICE_CONFIGURATION_ID)).thenReturn(Optional.of(deviceConfiguration));
        when(propertyValueInfoService.getPropertyInfos(anyList(), anyMap())).thenReturn(Collections.emptyList());

        Invocation.Builder all = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIGURATION_ID + "/validationrulesets/")
                .queryParam("all", Boolean.FALSE)
                .request()
                .header(APPLICATION_HEADER_PARAM, QualityCodeSystem.MDC.name());
        Response response = all.post(Entity.json(Arrays.asList(RULESET_ID_1, RULESET_ID_2)));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet1);
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet2);
    }

    @Test
    public void testAddAllRuleSetsToDeviceConfiguration() throws Exception {
        when(validationRuleSet1.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(validationRuleSet2.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(validationService.getValidationRuleSets()).thenReturn(Arrays.asList(validationRuleSet1, validationRuleSet2));
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration)).thenReturn(Arrays.asList(readingType1, readingType2));
        when(validationRuleSet1.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Collections.singletonList(rule1));
        when(validationRuleSet2.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Collections.singletonList(rule2));
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(DEVICE_CONFIGURATION_ID, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findDeviceConfiguration(DEVICE_CONFIGURATION_ID)).thenReturn(Optional.of(deviceConfiguration));

        Invocation.Builder all = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIGURATION_ID + "/validationrulesets/")
                .queryParam("all", Boolean.TRUE)
                .request()
                .header(APPLICATION_HEADER_PARAM, QualityCodeSystem.MDC.name());
        Response response = all.post(Entity.json(Collections.emptyList()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet1);
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet2);
    }

    @Test
    public void testAddAllRuleSetsToDeviceConfigurationWithoutNonMatching() throws Exception {
        when(validationRuleSet1.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(validationRuleSet2.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(DEVICE_CONFIGURATION_ID, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findDeviceConfiguration(DEVICE_CONFIGURATION_ID)).thenReturn(Optional.of(deviceConfiguration));

        when(validationService.getValidationRuleSets()).thenReturn(Arrays.asList(validationRuleSet1, validationRuleSet2));
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration)).thenReturn(Arrays.asList(readingType1, readingType2));
        when(validationRuleSet1.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Collections.emptyList());
        when(validationRuleSet2.getRules(Arrays.asList(readingType1, readingType2))).thenReturn(Collections.singletonList(rule2));

        Invocation.Builder all = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIGURATION_ID + "/validationrulesets/")
                .queryParam("all", Boolean.TRUE)
                .request()
                .header(APPLICATION_HEADER_PARAM, QualityCodeSystem.MDC.name());
        Response response = all.post(Entity.json(Collections.emptyList()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(deviceConfiguration, never()).addValidationRuleSet(validationRuleSet1);
        verify(deviceConfiguration).addValidationRuleSet(validationRuleSet2);
    }

    @Test
    public void testDeleteValidationRuleSetOkVersion() throws Exception {
        when(validationRuleSet1.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(validationRuleSet2.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(DEVICE_CONFIGURATION_ID, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findDeviceConfiguration(DEVICE_CONFIGURATION_ID)).thenReturn(Optional.of(deviceConfiguration));

        when(validationRuleSet1.getId()).thenReturn(RULESET_ID_1);
        when(validationRuleSet1.getVersion()).thenReturn(OK_VERSION);
        doReturn(Optional.of(validationRuleSet1)).when(validationService).getValidationRuleSet(RULESET_ID_1);
        doReturn(Optional.of(validationRuleSet1)).when(validationService).findAndLockValidationRuleSetByIdAndVersion(RULESET_ID_1, OK_VERSION);

        ValidationRuleSetInfo info = new ValidationRuleSetInfo();
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(DEVICE_CONFIGURATION_ID, OK_VERSION);

        Response response = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIGURATION_ID + "/validationrulesets/" + RULESET_ID_1)
                .request()
                .header(APPLICATION_HEADER_PARAM, QualityCodeSystem.MDC.name())
                .build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration).removeValidationRuleSet(validationRuleSet1);
    }

    @Test
    public void testDeleteValidationRuleSetBadVersion() throws Exception {
        when(validationRuleSet1.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(validationRuleSet2.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(deviceConfiguration.getVersion()).thenReturn(BAD_VERSION);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(DEVICE_CONFIGURATION_ID, BAD_VERSION)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceConfiguration(DEVICE_CONFIGURATION_ID)).thenReturn(Optional.of(deviceConfiguration));

        when(validationRuleSet1.getId()).thenReturn(RULESET_ID_1);
        when(validationRuleSet1.getVersion()).thenReturn(OK_VERSION);
        doReturn(Optional.of(validationRuleSet1)).when(validationService).getValidationRuleSet(RULESET_ID_1);
        doReturn(Optional.of(validationRuleSet1)).when(validationService).findAndLockValidationRuleSetByIdAndVersion(RULESET_ID_1, OK_VERSION);

        ValidationRuleSetInfo info = new ValidationRuleSetInfo();
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(DEVICE_CONFIGURATION_ID, BAD_VERSION);

        Response response = target("/devicetypes/" + DEVICE_TYPE_ID + "/deviceconfigurations/" + DEVICE_CONFIGURATION_ID + "/validationrulesets/" + RULESET_ID_1)
                .request()
                .header(APPLICATION_HEADER_PARAM, QualityCodeSystem.MDC.name())
                .build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(deviceConfiguration, never()).removeValidationRuleSet(validationRuleSet1);
    }

}