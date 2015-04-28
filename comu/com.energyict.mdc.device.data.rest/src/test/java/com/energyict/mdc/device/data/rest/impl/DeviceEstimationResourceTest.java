package com.energyict.mdc.device.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.energyict.mdc.device.configuration.rest.EntityRefInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;
import com.jayway.jsonpath.JsonModel;

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

        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByIdAndVersion(1L, 22l)).thenReturn(Optional.of(device));
    }

    @Test
    public void testGetEstimationRuleSetsForDevice() {
        DeviceEstimationRuleSetActivation rsa1 = mockEstimationRuleSetActivation(1L, "RS1", true);
        DeviceEstimationRuleSetActivation rsa2 = mockEstimationRuleSetActivation(123L, "RS2", false);
        when(deviceEstimation.getEstimationRuleSetActivations()).thenReturn(Arrays.asList(rsa1, rsa2));
        
        String response = target("/devices/mrid/estimationrulesets").request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List<Number>>get("$.estimationRuleSets[*].id")).containsExactly(1, 123);
        assertThat(model.<List<String>>get("$.estimationRuleSets[*].name")).containsExactly("RS1", "RS2");
        assertThat(model.<List<Boolean>>get("$.estimationRuleSets[*].active")).containsExactly(true, false);
        assertThat(model.<List<Number>>get("$.estimationRuleSets[*].parent.id")).containsExactly(1, 1);
        assertThat(model.<List<Number>>get("$.estimationRuleSets[*].parent.version")).containsExactly(22, 22);
    }
    
    @Test
    public void testActivateEstimationRuleSetOnDevice() {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class); 
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(1L);
        
        DeviceEstimationRuleSetRefInfo info = new DeviceEstimationRuleSetRefInfo();
        info.id = 1L;
        info.active = true;
        info.parent = new EntityRefInfo(1L, 22L);
        
        Response response = target("/devices/mrid/estimationrulesets/1").request().put(Entity.json(info));
        
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        verify(deviceEstimation).activateEstimationRuleSet(ruleSet);
    }
    
    @Test
    public void testDeactivateEstimationRuleSetOnDevice() {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class); 
        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(1L);
        
        DeviceEstimationRuleSetRefInfo info = new DeviceEstimationRuleSetRefInfo();
        info.id = 1L;
        info.active = false;
        info.parent = new EntityRefInfo(1L, 22L);
        
        Response response = target("/devices/mrid/estimationrulesets/1").request().put(Entity.json(info));
        
        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
        verify(deviceEstimation).deactivateEstimationRuleSet(ruleSet);
    }

    private DeviceEstimationRuleSetActivation mockEstimationRuleSetActivation(long ruleSetId, String name, boolean active) {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        when(ruleSet.getId()).thenReturn(ruleSetId);
        when(ruleSet.getName()).thenReturn(name);
        DeviceEstimationRuleSetActivation rsa = mock(DeviceEstimationRuleSetActivation.class);
        when(rsa.getEstimationRuleSet()).thenReturn(ruleSet);
        when(rsa.isActive()).thenReturn(active);
        return rsa;
    }

}
