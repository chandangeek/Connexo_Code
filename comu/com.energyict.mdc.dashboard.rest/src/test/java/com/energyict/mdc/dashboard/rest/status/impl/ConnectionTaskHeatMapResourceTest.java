package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.dashboard.ConnectionTaskDeviceTypeHeatMap;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMapRow;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.impl.ComSessionSuccessIndicatorOverviewImpl;
import com.energyict.mdc.dashboard.impl.ConnectionTaskHeatMapRowImpl;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionTaskHeatMapResourceTest extends JerseyTest {

    @Mock
    private StatusService statusService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DashboardService dashboardService;
    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private EngineModelService engineModelService;

    @Before
    public void setupMocks () {
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
        NlsMessageFormat mft = mock(NlsMessageFormat.class);
        when(mft.format(any(Object[].class))).thenReturn("format");
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(mft);

    }

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                ConnectionHeatMapResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(statusService).to(StatusService.class);
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(dashboardService).to(DashboardService.class);
                bind(deviceDataService).to(DeviceDataService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(protocolPluggableService).to(ProtocolPluggableService.class);
                bind(engineModelService).to(EngineModelService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
                bind(ConnectionTaskInfoFactory.class).to(ConnectionTaskInfoFactory.class);
                bind(ComTaskExecutionInfoFactory.class).to(ComTaskExecutionInfoFactory.class);
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
    public void testBadRequestWhenFilterIsMissing() throws Exception {
        Response response = target("/connectionheatmap").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testConnectionHeatMapJsonBinding() throws Exception {
        ConnectionTaskDeviceTypeHeatMap heatMap = createDeviceTypeHeatMap();
        when(dashboardService.getConnectionsDeviceTypeHeatMap()).thenReturn(heatMap);

        Map<String, Object> map = target("/connectionheatmap").queryParam("filter", ExtjsFilter.filter("breakdown", "deviceType")).request().get(Map.class);

        assertThat(map).containsKey("heatMap");
        assertThat(map).containsKey("breakdown");
    }

    private ConnectionTaskDeviceTypeHeatMap createDeviceTypeHeatMap() {
        ConnectionTaskDeviceTypeHeatMap heatMap = mock(ConnectionTaskDeviceTypeHeatMap.class);
        List<ConnectionTaskHeatMapRow<DeviceType>> rows = new ArrayList<>();
        ComSessionSuccessIndicatorOverviewImpl counters = new ComSessionSuccessIndicatorOverviewImpl(103L);
        counters.add(createCounter(ComSession.SuccessIndicator.Broken, 100L));
        counters.add(createCounter(ComSession.SuccessIndicator.SetupError, 101L));
        counters.add(createCounter(ComSession.SuccessIndicator.Success, 102L));
        long id=1;
        for (String name: Arrays.asList("deviceType1", "deviceType2", "deviceType3")) {
            DeviceType deviceType = mock(DeviceType.class);
            when(deviceType.getName()).thenReturn(name);
            when(deviceType.getId()).thenReturn(id++);
            rows.add(new ConnectionTaskHeatMapRowImpl<>(deviceType, counters));
        }
        when(heatMap.iterator()).thenReturn(rows.iterator());
        return heatMap;
    }

    private <C> Counter<C> createCounter(C status, Long count) {
        Counter<C> counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(count);
        when(counter.getCountTarget()).thenReturn(status);
        return counter;
    }
}