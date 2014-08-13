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
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.TaskHistoryService;
import com.energyict.protocols.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.google.common.base.Optional;
import java.util.Arrays;
import java.util.Date;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.dashboard.rest.status.ComServerStatusResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
public class ConnectionResourceTest extends JerseyTest {

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
    private TaskHistoryService taskHistoryService;
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
                ConnectionResource.class,
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
                bind(taskHistoryService).to(TaskHistoryService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(protocolPluggableService).to(ProtocolPluggableService.class);
                bind(engineModelService).to(EngineModelService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
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
    public void testDeviceTypesAddedToFilter() throws Exception {
        DeviceType deviceType1 = mock(DeviceType.class);
        DeviceType deviceType2 = mock(DeviceType.class);
        DeviceType deviceType3 = mock(DeviceType.class);

        when(deviceConfigurationService.findDeviceType(101L)).thenReturn(deviceType1);
        when(deviceConfigurationService.findDeviceType(102L)).thenReturn(deviceType2);
        when(deviceConfigurationService.findDeviceType(103L)).thenReturn(deviceType3);

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("deviceTypes", "101,102,103")).queryParam("start",0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(deviceDataService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().deviceTypes).contains(deviceType1).contains(deviceType2).contains(deviceType3).hasSize(3);

    }

    @Test
    public void testBadRequestWhenPagingIsMissing() throws Exception {
        Response response = target("/connections").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testCurrentStateAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("currentStates", "Busy,OnHold")).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(deviceDataService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().taskStatuses).contains(TaskStatus.Busy).contains(TaskStatus.OnHold).hasSize(2);

    }

    @Test
    public void testComPortPoolAddedToFilter() throws Exception {
        ComPortPool comPortPool1 = mock(ComPortPool.class);
        when(comPortPool1.getId()).thenReturn(1001L);
        ComPortPool comPortPool2 = mock(ComPortPool.class);
        when(comPortPool2.getId()).thenReturn(1002L);
        when(engineModelService.findAllComPortPools()).thenReturn(Arrays.asList(comPortPool1,comPortPool2));

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("comPortPools", "1001,1002")).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(deviceDataService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().comPortPools).contains(comPortPool1).contains(comPortPool2).hasSize(2);

    }

    @Test
    public void testConnectionTypesAddedToFilter() throws Exception {
        ConnectionTypePluggableClass connectionType1 = mock(ConnectionTypePluggableClass.class);
        when(protocolPluggableService.findConnectionTypePluggableClass(2001)).thenReturn(connectionType1);
        ConnectionTypePluggableClass connectionType2 = mock(ConnectionTypePluggableClass.class);
        when(protocolPluggableService.findConnectionTypePluggableClass(2002)).thenReturn(connectionType2);

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("connectionTypes", "2001,2002")).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(deviceDataService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().connectionTypes).contains(connectionType1).contains(connectionType2).hasSize(2);
    }

    @Test
    public void testConnectionTaskJsonBinding() throws Exception {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(deviceDataService.findConnectionTasksByFilter(Matchers.<ConnectionTaskFilterSpecification>anyObject(), anyInt(), anyInt())).thenReturn(Arrays.<ConnectionTask>asList(connectionTask));
        ComSession comSession = mock(ComSession.class);
        Optional<ComSession> comSessionOptional = Optional.of(comSession);
        when(taskHistoryService.getLastComSession(connectionTask)).thenReturn(comSessionOptional);
        when(connectionTask.getId()).thenReturn(1234L);
        when(connectionTask.getName()).thenReturn("fancy name");
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getName()).thenReturn("partial connection task name");
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.isDefault()).thenReturn(true);
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn("1234-5678-9012");
        when(device.getName()).thenReturn("some device");
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1010L);
        when(deviceType.getName()).thenReturn("device type");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(connectionTask.getDevice()).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(123123L);
        when(deviceConfiguration.getName()).thenReturn("123123");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ScheduledComTaskExecution comTaskExecution1 = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution1.getConnectionTask()).thenReturn((ConnectionTask)connectionTask);
        when(comTaskExecution1.getCurrentTryCount()).thenReturn(999);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getStatus()).thenReturn(TaskStatus.Busy);
        when(device.getComTaskExecutions()).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        when(comSession.getNumberOfFailedTasks()).thenReturn(401);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(12);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(3);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        when(comSession.getStartDate()).thenReturn(new Date());
        when(comSession.getStopDate()).thenReturn(new Date());
        when(comSession.getTotalTime()).thenReturn(4L);
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("comPortPool");
        when(comPortPool.getId()).thenReturn(1111L);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        OutboundTcpIpConnectionType connectionType = mock(OutboundTcpIpConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.OUTBOUND);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        ComWindow window = mock(ComWindow.class);
        when(window.getStart()).thenReturn(PartialTime.fromHours(9));
        when(window.getEnd()).thenReturn(PartialTime.fromHours(17));
        when(connectionTask.getCommunicationWindow()).thenReturn(window);
        ComSchedule comSchedule=mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("Weekly billing");
        when(comSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(1, TimeDuration.WEEKS),new TimeDuration(12, TimeDuration.HOURS)));
        when(comTaskExecution1.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution1.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution1.getLastExecutionStartTimestamp()).thenReturn(new Date());
        when(comTaskExecution1.getLastSuccessfulCompletionTimestamp()).thenReturn(new Date());
        when(comTaskExecution1.getNextExecutionTimestamp()).thenReturn(new Date());
        when(deviceDataService.findComTaskExecutionsByConnectionTask(connectionTask)).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        Map<String, Object> map = target("/connections").queryParam("start",0).queryParam("limit", 10).request().get(Map.class);

        assertThat(map).containsKey("total");
        assertThat(map).containsKey("connectionTasks");
        Map<String, Object> connectionTaskMap = (Map) ((List) map.get("connectionTasks")).get(0);
        assertThat(connectionTaskMap)
                .containsKey("device")
                .containsKey("deviceType")
                .containsKey("deviceConfiguration")
                .containsKey("currentState")
                .containsKey("latestStatus")
                .containsKey("latestResult")
                .containsKey("taskCount")
                .containsKey("startDateTime")
                .containsKey("endDateTime")
                .containsKey("duration")
                .containsKey("comPortPool")
                .containsKey("comServer")
                .containsKey("direction")
                .containsKey("connectionType")
                .containsKey("connectionMethod")
                .containsKey("connectionStrategy")
                .containsKey("window")
                .containsKey("nextExecution")
                .containsKey("communicationTasks")
                .hasSize(19);


    }
}