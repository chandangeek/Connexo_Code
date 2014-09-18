package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusResource;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusSummaryResource;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class DashboardRESTJerseyTest extends JerseyTest {

    @Mock
    StatusService statusService;
    @Mock
    NlsService nlsService;
    @Mock
    Thesaurus thesaurus;
    @Mock
    DeviceDataService deviceDataService;
    @Mock
    SchedulingService schedulingService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    TaskService taskService;
    @Mock
    DashboardService dashboardService;
    @Mock
    EngineModelService engineModelService;
    @Mock
    ProtocolPluggableService protocolPluggableService;

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
    protected final Application configure() {
        MockitoAnnotations.initMocks(this);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                LocalizedFieldValidationExceptionMapper.class,
                ConstraintViolationExceptionMapper.class,
                JsonMappingExceptionMapper.class,
                LocalizedExceptionMapper.class,
                ExceptionLogger.class,
                ComServerStatusResource.class,
                ComServerStatusSummaryResource.class,
                ConnectionOverviewResource.class,
                DashboardFieldResource.class,
                ConnectionResource.class,
                ConnectionHeatMapResource.class,
                CommunicationResource.class,
                CommunicationOverviewResource.class,
                CommunicationHeatMapResource.class,
                DeviceConfigurationService.class // This service is here intentionally: needed for the ComServerStatusResource apparently: this will create an osgi warning: A provider com.energyict.mdc.device.config.DeviceConfigurationService registered in SERVER runtime does not implement any provider interfaces applicable in the SERVER runtime. Due to constraint configuration problems the provider com.energyict.mdc.device.config.DeviceConfigurationService will be ignored.
        );
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(statusService).to(StatusService.class);
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(deviceDataService).to(DeviceDataService.class);
                bind(schedulingService).to(SchedulingService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(taskService).to(TaskService.class);
                bind(dashboardService).to(DashboardService.class);
                bind(engineModelService).to(EngineModelService.class);
                bind(protocolPluggableService).to(ProtocolPluggableService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
                bind(BreakdownFactory.class).to(BreakdownFactory.class);
                bind(OverviewFactory.class).to(OverviewFactory.class);
                bind(ConnectionTaskInfoFactory.class).to(ConnectionTaskInfoFactory.class);
                bind(ComTaskExecutionInfoFactory.class).to(ComTaskExecutionInfoFactory.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected final void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing
        super.configureClient(config);
    }

}