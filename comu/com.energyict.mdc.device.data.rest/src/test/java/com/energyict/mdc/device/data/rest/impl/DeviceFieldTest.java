package com.energyict.mdc.device.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Matchers;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.jayway.jsonpath.JsonModel;

public class DeviceFieldTest extends DeviceDataRestApplicationJerseyTest {


    @Test
    public void testGetEndDeviceDomains() throws Exception {        
        Map<String, Object> domains = target("field/enddevicedomains").request().get(Map.class);

        List<?> domainsList = (List<?>) domains.get("domains");
        
        assertThat(domainsList).hasSize(EndDeviceDomain.values().length);
        for (int i = 0; i < domainsList.size(); i++) {
            assertThat((Map<String, Object>) domainsList.get(i)).containsKeys("domain", "localizedValue");
        }
    }
    
    @Test
    public void testGetEndDeviceSubDomains() throws Exception {      
        Map<String, Object> subDomains = target("field/enddevicesubdomains").request().get(Map.class);

        List<?> subDomainsList = (List<?>) subDomains.get("subDomains");
        
        assertThat(subDomainsList).hasSize(EndDeviceSubDomain.values().length);
        for (int i = 0; i < subDomainsList.size(); i++) {
            assertThat((Map<String, Object>) subDomainsList.get(i)).containsKeys("subDomain", "localizedValue");
        }
    }
    
    @Test
    public void testGetEndDeviceEventOrActions() throws Exception {       
        Map<String, Object> eventOrActions = target("field/enddeviceeventoractions").request().get(Map.class);

        List<?> eventOrActionsList = (List<?>) eventOrActions.get("eventOrActions");
        
        assertThat(eventOrActionsList).hasSize(EndDeviceEventorAction.values().length);
        for (int i = 0; i < eventOrActionsList.size(); i++) {
            assertThat((Map<String, Object>) eventOrActionsList.get(i)).containsKeys("eventOrAction", "localizedValue");
        }
    }
    
    @Test
    public void testGetGateways() {
        Finder<Device> finder = mock(Finder.class);
        when(deviceService.findAllDevices(Matchers.any(Condition.class))).thenReturn(finder);
        when(finder.paged(Matchers.anyInt(), Matchers.anyInt())).thenReturn(finder);
        when(finder.sorted(Matchers.anyString(), Matchers.anyBoolean())).thenReturn(finder);
        
        Device device1 = mockDevice(1L, "device1", true);
        Device device2 = mockDevice(2L, "device2", false);
        
        when(finder.find()).thenReturn(Arrays.asList(device1, device2));
        
        String response = target("field/gateways").queryParam("search", "00").queryParam("excludeDeviceMRID", "001").queryParam("limit", 2).request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        assertThat(model.<List<?>>get("$.gateways")).hasSize(2);
        assertThat(model.<List<Integer>>get("$.gateways[*].id")).containsExactly(1, 1);
        assertThat(model.<List<String>>get("$.gateways[*].name")).containsExactly("device1", "device1");
    }
    
    @Test
    public void testGetGatewaysEmptryResult() {
        Finder<Device> finder = mock(Finder.class);
        when(deviceService.findAllDevices(Matchers.any(Condition.class))).thenReturn(finder);
        when(finder.paged(Matchers.anyInt(), Matchers.anyInt())).thenReturn(finder);
        when(finder.sorted(Matchers.anyString(), Matchers.anyBoolean())).thenReturn(finder);
        
        when(finder.find()).thenReturn(Arrays.asList());
        
        String response = target("field/gateways").queryParam("search", "00").queryParam("excludeDeviceMRID", "001").queryParam("limit", 2).request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        assertThat(model.<List<?>>get("$.gateways")).isEmpty();
    }
    
    private Device mockDevice(long id, String mrid, boolean canActAsGateway) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(id);
        when(device.getmRID()).thenReturn(mrid);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.canActAsGateway()).thenReturn(canActAsGateway);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        return device;
    }
}
