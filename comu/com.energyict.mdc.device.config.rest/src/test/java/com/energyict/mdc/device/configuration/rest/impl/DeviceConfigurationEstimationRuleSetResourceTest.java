/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceConfigurationEstimationRuleSetResourceTest extends DeviceConfigurationApplicationJerseyTest {

    @Test
    public void testGetDeviceConfigurationsForEstimationRuleSet() {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(1L);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class, Answers.RETURNS_DEEP_STUBS.get());
        Finder<DeviceConfiguration> finder = mock(Finder.class);
        when(deviceConfigurationService.findDeviceConfigurationsForEstimationRuleSet(ruleSet)).thenReturn(finder);
        when(finder.find()).thenReturn(Arrays.asList(deviceConfig));
        when(finder.paged(Matchers.anyInt(), Matchers.anyInt())).thenReturn(finder);
        when(finder.from(any())).thenReturn(finder);
        when(deviceConfig.getId()).thenReturn(18L);
        when(deviceConfig.getName()).thenReturn("Device Config");
        when(deviceConfig.getLoadProfileSpecs().size()).thenReturn(5);
        when(deviceConfig.getRegisterSpecs().size()).thenReturn(10);
        when(deviceConfig.getDeviceType().getId()).thenReturn(1L);
        when(deviceConfig.getDeviceType().getName()).thenReturn("Device Type");

        String response = target("estimationrulesets/1/deviceconfigurations").request().get(String.class);

        JsonModel model = JsonModel.model(response);

        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List<?>>get("$.deviceConfigurations")).hasSize(1);
        assertThat(model.<Number>get("$.deviceConfigurations[0].id")).isEqualTo(18);
        assertThat(model.<Number>get("$.deviceConfigurations[0].deviceTypeId")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceConfigurations[0].deviceTypeName")).isEqualTo("Device Type");
        assertThat(model.<String>get("$.deviceConfigurations[0].name")).isEqualTo("Device Config");
        assertThat(model.<Boolean>get("$.deviceConfigurations[0].active")).isFalse();
        assertThat(model.<Number>get("$.deviceConfigurations[0].loadProfileCount")).isEqualTo(5);
        assertThat(model.<Number>get("$.deviceConfigurations[0].registerCount")).isEqualTo(10);
    }

    @Test
    public void testGetDeviceConfigurationForEstimationRuleSetNotFound() {
        doReturn(Optional.empty()).when(estimationService).getEstimationRuleSet(1L);

        Response response = target("estimationrulesets/1/deviceconfigurations").request().get();

        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }
    
    @Test
    public void testGetLinkableDeviceConfigurations() {
        mockComputeLinkableDeviceConfigCall();
       
        String response = target("estimationrulesets/1/linkabledeviceconfigurations").request().get(String.class);

        JsonModel model = JsonModel.model(response);

        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List<?>>get("$.deviceConfigurations")).hasSize(1);
        assertThat(model.<Number>get("$.deviceConfigurations[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.deviceConfigurations[0].deviceTypeName")).isEqualTo("Device Type");
        assertThat(model.<String>get("$.deviceConfigurations[0].name")).isEqualTo("Device Config 2");
        assertThat(model.<Boolean>get("$.deviceConfigurations[0].active")).isFalse();
        assertThat(model.<Number>get("$.deviceConfigurations[0].loadProfileCount")).isEqualTo(0);
        assertThat(model.<Number>get("$.deviceConfigurations[0].registerCount")).isEqualTo(0);
    }

    @Test
    public void testAddDeviceConfigurationToEstimationRuleSet() {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(1L);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(1l, 13l)).thenReturn(Optional.of(deviceConfig));
        
        DeviceConfigurationRefInfo info = new DeviceConfigurationRefInfo();
        info.id = 1;
        info.version = 13;
        
        Response response = target("estimationrulesets/1/deviceconfigurations").request().post(Entity.json(Arrays.asList(info)));

        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    }
    
    @Test
    public void testAddDeviceConfigurationToEstimationRuleSetIncludeAll() {
        mockComputeLinkableDeviceConfigCall();
        
        Response response = target("estimationrulesets/1/deviceconfigurations").queryParam("all", true).request().post(Entity.json(""));

        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    }

    private void mockComputeLinkableDeviceConfigCall() {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(1L);
        when(ruleSet.getId()).thenReturn(1L);
        
        EstimationRule rule = mock(EstimationRule.class);
        doReturn(Arrays.asList(rule)).when(ruleSet).getRules();
        Set<ReadingType> readingTypes = new HashSet<>();
        when(rule.getReadingTypes()).thenReturn(readingTypes);
        ReadingType rt = mock(ReadingType.class);
        readingTypes.add(rt);
        when(rt.isCumulative()).thenReturn(true);
        when(rt.getCalculatedReadingType()).thenReturn(Optional.empty());
        
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("Device Type");
        Finder<DeviceType> finder = mock(Finder.class);
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);
        when(finder.stream()).thenReturn(Arrays.asList(deviceType).stream());
        
        DeviceConfiguration deviceConfig1 = mock(DeviceConfiguration.class);
        when(deviceConfig1.getId()).thenReturn(1L);
        when(deviceConfig1.getDeviceType()).thenReturn(deviceType);
        when(deviceConfig1.getEstimationRuleSets()).thenReturn(Arrays.asList(ruleSet));
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfig1)).thenReturn(Arrays.asList(rt));
        
        DeviceConfiguration deviceConfig2 = mock(DeviceConfiguration.class);
        when(deviceConfig2.getId()).thenReturn(2L);
        when(deviceConfig2.getName()).thenReturn("Device Config 2");
        when(deviceConfig2.getDeviceType()).thenReturn(deviceType);
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfig2)).thenReturn(Arrays.asList(rt));
        
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfig1, deviceConfig2));
        
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(1, 0)).thenReturn(Optional.of(deviceConfig1));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(2, 0)).thenReturn(Optional.of(deviceConfig2));
    }
}
