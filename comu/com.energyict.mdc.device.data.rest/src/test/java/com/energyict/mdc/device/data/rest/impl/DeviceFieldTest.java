package com.energyict.mdc.device.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

public class DeviceFieldTest extends JerseyTest {

    private static NlsService nlsService;
    private static Thesaurus thesaurus;

    @BeforeClass
    public static void setUpClass() throws Exception {
        nlsService = mock(NlsService.class);
        thesaurus = mock(Thesaurus.class);
        
        when(nlsService.getThesaurus(DeviceApplication.COMPONENT_NAME, Layer.REST)).thenReturn(thesaurus);
        
        when(thesaurus.getString(Matchers.anyString(), Matchers.anyString())).then(new Answer<String>() {
            
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[1];
            }
        });
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(DeviceFieldResource.class);
        
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class);
        super.configureClient(config);
    }

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
