package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceEstimationResourceTest extends DeviceDataRestApplicationJerseyTest {
    
    @Mock
    DeviceEstimation deviceEstimation;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(device.getVersion()).thenReturn(22L);
        when(device.forEstimation()).thenReturn(deviceEstimation);
        when(device.getName()).thenReturn("name");

        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("name", 22l)).thenReturn(Optional.of(device));
    }

    @Test
    public void testGetEstimationRuleSetsForDevice() {
        DeviceEstimationRuleSetActivation rsa1 = mockEstimationRuleSetActivation(1L, "RS1", true);
        DeviceEstimationRuleSetActivation rsa2 = mockEstimationRuleSetActivation(123L, "RS2", false);
        when(deviceEstimation.getEstimationRuleSetActivations()).thenReturn(Arrays.asList(rsa1, rsa2));

        String response = target("/devices/name/estimationrulesets").request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List<Number>>get("$.estimationRuleSets[*].id")).containsExactly(1, 123);
        assertThat(model.<List<String>>get("$.estimationRuleSets[*].name")).containsExactly("RS1", "RS2");
        assertThat(model.<List<Boolean>>get("$.estimationRuleSets[*].active")).containsExactly(true, false);
        assertThat(model.<List<String>>get("$.estimationRuleSets[*].parent.id")).containsExactly("name", "name");
        assertThat(model.<List<Number>>get("$.estimationRuleSets[*].parent.version")).containsExactly(22, 22);
    }
    
    @Test
    public void testActivateEstimationRuleSetOnDevice() {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class); 
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(1L);
        doReturn(Optional.of(ruleSet)).when(estimationService).findAndLockEstimationRuleSet(1L, 1L);
        
        DeviceEstimationRuleSetRefInfo info = new DeviceEstimationRuleSetRefInfo();
        info.id = 1L;
        info.active = true;
        info.version = 1L;
        info.parent = new VersionInfo<>("name", 22L);

        Response response = target("/devices/name/estimationrulesets/1").request().put(Entity.json(info));
        
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        verify(deviceEstimation).activateEstimationRuleSet(ruleSet);
    }
    
    @Test
    public void testDeactivateEstimationRuleSetOnDevice() {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class); 
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(1L);
        doReturn(Optional.of(ruleSet)).when(estimationService).findAndLockEstimationRuleSet(1L, 1L);

        DeviceEstimationRuleSetRefInfo info = new DeviceEstimationRuleSetRefInfo();
        info.id = 1L;
        info.active = false;
        info.version = 1L;
        info.parent = new VersionInfo<>("name", 22L);

        Response response = target("/devices/name/estimationrulesets/1").request().put(Entity.json(info));
        
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        verify(deviceEstimation).deactivateEstimationRuleSet(ruleSet);
    }

    private DeviceEstimationRuleSetActivation mockEstimationRuleSetActivation(long ruleSetId, String name, boolean active) {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        when(ruleSet.getId()).thenReturn(ruleSetId);
        when(ruleSet.getName()).thenReturn(name);
        when(ruleSet.getVersion()).thenReturn(1L);
        DeviceEstimationRuleSetActivation rsa = mock(DeviceEstimationRuleSetActivation.class);
        when(rsa.getEstimationRuleSet()).thenReturn(ruleSet);
        when(rsa.isActive()).thenReturn(active);
        return rsa;
    }
}
