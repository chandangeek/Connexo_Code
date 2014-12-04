package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
