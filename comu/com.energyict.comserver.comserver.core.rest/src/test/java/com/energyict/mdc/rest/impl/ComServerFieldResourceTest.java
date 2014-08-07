package com.energyict.mdc.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.rest.impl.comserver.MessageSeeds;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class ComServerFieldResourceTest extends JerseyTest {

    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(nlsService.getThesaurus(anyString(), (Layer) anyObject())).thenReturn(thesaurus);
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                for (MessageSeeds messageSeeds : MessageSeeds.values()) {
                    if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                        return messageSeeds.getDefaultFormat();
                    }
                }
                return (String) invocationOnMock.getArguments()[1];
            }
        });
    }

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(ComServerFieldResource.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
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
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }

    @Test
    public void testNrOfStopBitsKeys() throws Exception {
        Map response = target("/field/nrOfStopBits").request().get(Map.class);
        assertThat(response).containsKey("nrOfStopBits");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("nrOfStopBits");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("nrOfStopBits"));
        }
    }

    @Test
    public void testNrOfDataBitsKeys() throws Exception {
        Map response = target("/field/nrOfDataBits").request().get(Map.class);
        assertThat(response).containsKey("nrOfDataBits");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("nrOfDataBits");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("nrOfDataBits"));
        }
    }

    @Test
    public void testLogLevelKeys() throws Exception {
        Map response = target("/field/logLevel").request().get(Map.class);
        assertThat(response).containsKey("logLevels");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("logLevels");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("logLevel"));
        }
    }

    @Test
    public void testComPortTypeKeys() throws Exception {
        Map response = target("/field/comPortType").request().get(Map.class);
        assertThat(response).containsKey("comPortTypes");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("comPortTypes");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("comPortType"));
        }
    }

    @Test
    public void testBaudRateKeys() throws Exception {
        Map response = target("/field/baudRate").request().get(Map.class);
        assertThat(response).containsKey("baudRates");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("baudRates");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("baudRate"));
        }
    }

    @Test
    public void testTimeUnitKeys() throws Exception {
        Map response = target("/field/timeUnit").request().get(Map.class);
        assertThat(response).containsKey("timeUnits");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("timeUnits");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("timeUnit"));
        }
    }

    @Test
    public void testFlowControlKeys() throws Exception {
        Map response = target("/field/flowControl").request().get(Map.class);
        assertThat(response).containsKey("flowControls");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("flowControls");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("flowControl"));
        }
    }

    @Test
    public void testParitiesKeys() throws Exception {
        Map response = target("/field/parity").request().get(Map.class);
        assertThat(response).containsKey("parities");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("parities");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("parity"));
        }
    }
}