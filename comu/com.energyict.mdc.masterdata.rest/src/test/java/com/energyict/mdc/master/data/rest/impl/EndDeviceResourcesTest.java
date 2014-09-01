package com.energyict.mdc.master.data.rest.impl;

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
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.rest.impl.EndDeviceDomainResource;
import com.energyict.mdc.masterdata.rest.impl.EndDeviceEventOrActionResource;
import com.energyict.mdc.masterdata.rest.impl.EndDeviceSubDomainResource;

public class EndDeviceResourcesTest extends JerseyTest {

    private static Thesaurus thesaurus;

    @BeforeClass
    public static void setUpClass() throws Exception {
        thesaurus = mock(Thesaurus.class);
        
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
        ResourceConfig resourceConfig = new ResourceConfig(
                EndDeviceDomainResource.class,
                EndDeviceSubDomainResource.class,
                EndDeviceEventOrActionResource.class);
        
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
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
        Map<String, Object> domains = target("/enddevicedomains").request().get(Map.class);

        assertThat(domains.get("total")).isEqualTo(EndDeviceDomain.values().length);

        List<?> domainsList = (List<?>) domains.get("data");
        
        assertThat(domainsList).hasSize(EndDeviceDomain.values().length);
        for (int i = 0; i < domainsList.size(); i++) {
            assertThat((Map) domainsList.get(i)).containsKeys("id", "name");
        }
    }
    
    @Test
    public void testGetEndDeviceSubDomains() throws Exception {      
        Map<String, Object> subDomains = target("/enddevicesubdomains").request().get(Map.class);

        assertThat(subDomains.get("total")).isEqualTo(EndDeviceSubDomain.values().length);

        List<?> subDomainsList = (List<?>) subDomains.get("data");
        
        assertThat(subDomainsList).hasSize(EndDeviceSubDomain.values().length);
        for (int i = 0; i < subDomainsList.size(); i++) {
            assertThat((Map<String, Object>) subDomainsList.get(i)).containsKeys("id", "name");
        }
    }
    
    @Test
    public void testGetEndDeviceEventOrActions() throws Exception {       
        Map<String, Object> eventOrActions = target("/enddeviceeventoractions").request().get(Map.class);

        assertThat(eventOrActions.get("total")).isEqualTo(EndDeviceEventorAction.values().length);

        List<?> eventOrActionsList = (List<?>) eventOrActions.get("data");
        
        assertThat(eventOrActionsList).hasSize(EndDeviceEventorAction.values().length);
        for (int i = 0; i < eventOrActionsList.size(); i++) {
            assertThat((Map<String, Object>) eventOrActionsList.get(i)).containsKeys("id", "name");
        }
    }
}
