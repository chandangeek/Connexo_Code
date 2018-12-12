/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.configuration.rest.EstimationRuleSetRefInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EstimationRuleSetResourceTest extends DeviceConfigurationApplicationJerseyTest {
    
    @Mock
    DeviceType deviceType;
    @Mock
    DeviceConfiguration deviceConfiguration;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(deviceConfigurationService.findDeviceType(1003L)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(1003L, 1L)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findDeviceConfiguration(1003L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(1003L, 1L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfiguration.getId()).thenReturn(1003L);
        when(deviceConfiguration.getVersion()).thenReturn(1L);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        when(deviceType.getId()).thenReturn(1003L);
        when(deviceType.getVersion()).thenReturn(1L);
    }

    @Test
    public void testGetEstimationRuleSetsOnDeviceConfiguration() {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        when(deviceConfiguration.getEstimationRuleSets()).thenReturn(Arrays.asList(ruleSet));
        when(ruleSet.getId()).thenReturn(13L);
        when(ruleSet.getName()).thenReturn("Estimation rule");
        EstimationRule activeRule = mock(EstimationRule.class);
        when(activeRule.isActive()).thenReturn(true);
        EstimationRule inactiveRule = mock(EstimationRule.class);
        when(inactiveRule.isActive()).thenReturn(false);
        doReturn(Arrays.asList(activeRule, inactiveRule)).when(ruleSet).getRules();
        
        String response = target("/devicetypes/1003/deviceconfigurations/1003/estimationrulesets").queryParam("linkable", false).request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List<?>>get("$.estimationRuleSets")).hasSize(1);
        assertThat(model.<Number>get("$.estimationRuleSets[0].id")).isEqualTo(13);
        assertThat(model.<String>get("$.estimationRuleSets[0].name")).isEqualTo("Estimation rule");
        assertThat(model.<Number>get("$.estimationRuleSets[0].numberOfInactiveRules")).isEqualTo(1);
        assertThat(model.<Number>get("$.estimationRuleSets[0].numberOfRules")).isEqualTo(2);
        assertThat(model.<Number>get("$.estimationRuleSets[0].parent.id")).isEqualTo(1003);
        assertThat(model.<Number>get("$.estimationRuleSets[0].parent.version")).isEqualTo(1);
    }
    
    @Test
    public void testGetLinkableEstimationRuleSetsOnDeviceConfiguration() {
        mockLinkableEstimationRuleSetsCall();
        
        String response = target("/devicetypes/1003/deviceconfigurations/1003/estimationrulesets").queryParam("linkable", true).request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List<?>>get("$.estimationRuleSets")).hasSize(1);
        assertThat(model.<Number>get("$.estimationRuleSets[0].id")).isEqualTo(13);
    }
    
    @Test
    public void testAddEstimationRuleSetToDeviceConfigurationIncludeAll() {
        List<EstimationRuleSet> linkableEstimationRuleSets = mockLinkableEstimationRuleSetsCall();
        
        EstimationRuleSetRefInfo info = new EstimationRuleSetRefInfo();
        info.id = linkableEstimationRuleSets.get(0).getId();
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        
        Response response = target("/devicetypes/1003/deviceconfigurations/1003/estimationrulesets").queryParam("all", false).request().post(Entity.json(Arrays.asList(info)));
        
        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
        for (EstimationRuleSet rs : linkableEstimationRuleSets) {
            verify(deviceConfiguration).addEstimationRuleSet(rs);
        }
        verify(deviceConfiguration).save();
    }
    
    @Test
    public void testAddEstimationRuleSetToDeviceConfiguration() {
        List<EstimationRuleSet> linkableEstimationRuleSets = mockLinkableEstimationRuleSetsCall();
        
        Response response = target("/devicetypes/1003/deviceconfigurations/1003/estimationrulesets").queryParam("all", true).request().post(Entity.json(""));
        
        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
        for (EstimationRuleSet rs : linkableEstimationRuleSets) {
            verify(deviceConfiguration).addEstimationRuleSet(rs);
        }
        verify(deviceConfiguration).save();
    }
    
    @Test
    public void testDeleteEstimationRuleSetFromDeviceConfiguration() {
        EstimationRuleSet estimationRuleSet = mock(EstimationRuleSet.class);
        doReturn(Optional.of(estimationRuleSet)).when(estimationService).getEstimationRuleSet(13L);
        doReturn(Optional.of(estimationRuleSet)).when(estimationService).findAndLockEstimationRuleSet(13L, 1L);

        EstimationRuleSetRefInfo info = new EstimationRuleSetRefInfo();
        info.id = 13L;
        info.version = 1L;
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        
        Response response = target("/devicetypes/1003/deviceconfigurations/1003/estimationrulesets/13").request().method("DELETE", Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
        verify(deviceConfiguration).removeEstimationRuleSet(estimationRuleSet);
    }

    @Test
    public void testDeleteEstimationRuleSetBadVersion() {
        EstimationRuleSet estimationRuleSet = mock(EstimationRuleSet.class);
        doReturn(Optional.of(estimationRuleSet)).when(estimationService).getEstimationRuleSet(13L);
        doReturn(Optional.empty()).when(estimationService).findAndLockEstimationRuleSet(13L, 1L);

        EstimationRuleSetRefInfo info = new EstimationRuleSetRefInfo();
        info.id = 13L;
        info.version = 1L;
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());

        Response response = target("/devicetypes/1003/deviceconfigurations/1003/estimationrulesets/13").request().method("DELETE", Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Status.CONFLICT.getStatusCode());
        verify(deviceConfiguration, never()).removeEstimationRuleSet(estimationRuleSet);
    }
    
    @Test
    public void testReoderEstimationRuleSetsOnDeviceConfiguration() {
        EstimationRuleSet ruleSet1 = mock(EstimationRuleSet.class);
        when(ruleSet1.getId()).thenReturn(1L);
        EstimationRuleSet ruleSet2 = mock(EstimationRuleSet.class);
        when(ruleSet2.getId()).thenReturn(2L);
        
        when(deviceConfiguration.getEstimationRuleSets()).thenReturn(Arrays.asList(ruleSet1, ruleSet2));
        
        EstimationRuleSetRefInfo ruleSetInfo1 = new EstimationRuleSetRefInfo(ruleSet1, deviceConfiguration);
        EstimationRuleSetRefInfo ruleSetInfo2 = new EstimationRuleSetRefInfo(ruleSet2, deviceConfiguration);
        EstimationRuleSetReorderInfo info = new EstimationRuleSetReorderInfo();
        info.ruleSets = Arrays.asList(ruleSetInfo2, ruleSetInfo1);
        info.parent = new DeviceConfigurationInfo(deviceConfiguration);
        
        Response response = target("/devicetypes/1003/deviceconfigurations/1003/estimationrulesets").request().put(Entity.json(info));
        
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        verify(deviceConfiguration).reorderEstimationRuleSets(Matchers.any());
        verify(deviceConfiguration).save();
    }
    
    private List<EstimationRuleSet> mockLinkableEstimationRuleSetsCall() {
        ReadingType readingType = mock(ReadingType.class);
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(deviceConfiguration)).thenReturn(Arrays.asList(readingType));
        EstimationRuleSet ruleSetIncluded = mock(EstimationRuleSet.class);
        when(deviceConfiguration.getEstimationRuleSets()).thenReturn(Arrays.asList(ruleSetIncluded));
        EstimationRuleSet ruleSetNotIncluded = mock(EstimationRuleSet.class);
        when(ruleSetNotIncluded.getId()).thenReturn(13L);
        doReturn(Optional.of(ruleSetNotIncluded)).when(estimationService).getEstimationRuleSet(13L);
        doReturn(Arrays.asList(ruleSetIncluded, ruleSetNotIncluded)).when(estimationService).getEstimationRuleSets();
        EstimationRule rule = mock(EstimationRule.class);
        doReturn(Arrays.asList(rule)).when(ruleSetIncluded).getRules(Matchers.anySet());
        doReturn(Arrays.asList(rule)).when(ruleSetNotIncluded).getRules(Matchers.anySet());
        return Arrays.asList(ruleSetNotIncluded);
    }
    
}

