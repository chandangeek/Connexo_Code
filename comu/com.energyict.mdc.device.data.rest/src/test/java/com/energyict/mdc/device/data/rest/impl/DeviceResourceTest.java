package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionBuilder;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.google.common.base.Optional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.assertj.core.data.MapEntry;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/19/14.
 */
public class DeviceResourceTest extends JerseyTest {

    private static final String DUMMY_THESAURUS_STRING = "";
    private static DeviceDataService deviceDataService;
    private static DeviceImportService deviceImportService;
    private static DeviceConfigurationService deviceConfigurationService;
    private static NlsService nlsService;
    private static Thesaurus thesaurus;
    private static EngineModelService engineModelService;
    private static IssueService issueService;
    private static MdcPropertyUtils mdcPropertyUtils;
    private static SchedulingService schedulingService;
    private ConnectionTask.ConnectionTaskLifecycleStatus status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;

    @BeforeClass
    public static void setUpClass() throws Exception {
        issueService=mock(IssueService.class);
        deviceDataService = mock(DeviceDataService.class);
        deviceImportService = mock(DeviceImportService.class);
        deviceConfigurationService = mock(DeviceConfigurationService.class);
        engineModelService = mock(EngineModelService.class);
        nlsService = mock(NlsService.class);
        thesaurus = mock(Thesaurus.class);
        mdcPropertyUtils = mock(MdcPropertyUtils.class);
        schedulingService = mock(SchedulingService.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(deviceImportService, engineModelService);
        when(thesaurus.getString(anyString(), anyString())).thenReturn(DUMMY_THESAURUS_STRING);
        NlsMessageFormat mft = mock(NlsMessageFormat.class);
        when(mft.format(any(Object[].class))).thenReturn("format");
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(mft);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                ResourceHelper.class,
                DeviceResource.class,
                LoadProfileResource.class,
                BulkScheduleResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(deviceDataService).to(DeviceDataService.class);
                bind(deviceImportService).to(DeviceImportService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(engineModelService).to(EngineModelService.class);
                bind(nlsService).to(NlsService.class);
                bind(ResourceHelper.class).to(ResourceHelper.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(issueService).to(IssueService.class);
                bind(mdcPropertyUtils).to(MdcPropertyUtils.class);
                bind(ConnectionMethodInfoFactory.class).to(ConnectionMethodInfoFactory.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
                bind(schedulingService).to(SchedulingService.class);
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
    public void testGetConnectionMethodsJsonBindings() throws Exception {
        Device device = mock(Device.class);
        when(deviceDataService.findByUniqueMrid("1")).thenReturn(device);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?,?>>asList(connectionTask));
        Map<String, Object> response = target("/devices/1/connectionmethods").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("connectionMethods");
        List<Map<String,Object>> connectionMethods = (List<Map<String, Object>>) response.get("connectionMethods");
        assertThat(connectionMethods).hasSize(1);
        Map<String, Object> connectionTypeJson = connectionMethods.get(0);
        assertThat(connectionTypeJson)
                .containsKey("direction")
                .containsKey("name")
                .containsKey("id")
                .containsKey("status")
                .containsKey("connectionType")
                .containsKey("comWindowStart")
                .containsKey("comWindowEnd")
                .containsKey("comPortPool")
                .containsKey("isDefault")
                .containsKey("connectionStrategy")
                .containsKey("properties")
                .containsKey("allowSimultaneousConnections")
                .containsKey("rescheduleRetryDelay")
                .containsKey("nextExecutionSpecs");
    }

    @Test
    public void testCreatePausedInboundConnectionMethod() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name="inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE;
        info.isDefault=false;
        info.comPortPool="cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock (PartialInboundConnectionTask.class);
        when(deviceDataService.findByUniqueMrid("1")).thenReturn(device);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        when(engineModelService.findComPortPool("cpp")).thenReturn(comPortPool);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);

        Response response = target("/devices/1/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(connectionTask, never()).activate();
        verify(connectionTask, never()).deactivate();
        verify(inboundConnectionTaskBuilder, times(1)).setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        verify(inboundConnectionTaskBuilder, times(1)).setComPortPool(comPortPool);
    }

    @Test
    public void testCreateActiveInboundConnectionMethod() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name="inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault=false;
        info.comPortPool="cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock (PartialInboundConnectionTask.class);
        when(deviceDataService.findByUniqueMrid("1")).thenReturn(device);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        when(engineModelService.findComPortPool("cpp")).thenReturn(comPortPool);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);

        Response response = target("/devices/1/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(connectionTask, never()).activate();
        verify(connectionTask, never()).deactivate();
        verify(inboundConnectionTaskBuilder, times(1)).setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        verify(inboundConnectionTaskBuilder, times(1)).setComPortPool(comPortPool);
        verify(deviceDataService, never()).setDefaultConnectionTask(connectionTask);
    }

    @Test
    public void testCreateDefaultInboundConnectionMethod() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name="inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault=true;
        info.comPortPool="cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock (PartialInboundConnectionTask.class);
        when(deviceDataService.findByUniqueMrid("1")).thenReturn(device);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        when(engineModelService.findComPortPool("cpp")).thenReturn(comPortPool);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);

        Response response = target("/devices/1/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(deviceDataService).setDefaultConnectionTask(connectionTask);
    }

    @Test
    public void testUpdateAndUndefaultInboundConnectionMethod() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name="inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault=false;
        info.comPortPool="cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock (PartialInboundConnectionTask.class);
        when(deviceDataService.findByUniqueMrid("1")).thenReturn(device);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        when(engineModelService.findComPortPool("cpp")).thenReturn(comPortPool);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?,?>>asList(connectionTask));
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getId()).thenReturn(5L);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);

        Response response = target("/devices/1/connectionmethods/5").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceDataService, times(1)).clearDefaultConnectionTask(device);
    }

    @Test
    public void testUpdateOnlyClearsDefaultIfConnectionMethodWasDefaultBeforeUpdate() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name="inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault=false;
        info.comPortPool="cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock (PartialInboundConnectionTask.class);
        when(deviceDataService.findByUniqueMrid("1")).thenReturn(device);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        when(engineModelService.findComPortPool("cpp")).thenReturn(comPortPool);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?,?>>asList(connectionTask));
        when(deviceConfig.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        when(partialConnectionTask.getName()).thenReturn("inbConnMethod");

        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getId()).thenReturn(5L);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.isDefault()).thenReturn(false);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);

        Response response = target("/devices/1/connectionmethods/5").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceDataService, never()).clearDefaultConnectionTask(device);
    }

    @Test
    public void testComSchedulesBulkActionsWithWrongDevice(){
        BulkRequestInfo request = new BulkRequestInfo();
        request.deviceMRIDs = Arrays.asList("mrid1", "unexisting");
        request.scheduleIds = Arrays.asList(1L);
        Entity<BulkRequestInfo> json = Entity.json(request);

        ScheduledComTaskExecutionBuilder builder = mock(ScheduledComTaskExecutionBuilder.class);

        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn("mrid1");
        when(device.getName()).thenReturn("Device with mrid1");
        when(device.newScheduledComTaskExecution(any(ComSchedule.class))).thenReturn(builder);
        when(deviceDataService.findByUniqueMrid("mrid1")).thenReturn(device);
        when(deviceDataService.findByUniqueMrid("unexisting")).thenReturn(null);

        ComSchedule schedule = mock(ComSchedule.class);
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(schedule));

        ComSchedulesBulkInfo response = target("/devices/schedules").request().put(json, ComSchedulesBulkInfo.class);
        assertThat(response.actions.size()).isEqualTo(1);
        assertThat(response.actions.get(0).successCount).isEqualTo(1);
        assertThat(response.actions.get(0).failCount).isEqualTo(1);
        assertThat(response.actions.get(0).fails.size()).isEqualTo(1);
        assertThat(response.actions.get(0).fails.get(0).message).isNotEmpty();
    }

    @Test
    public void testComSchedulesBulkActionsAddAlreadyAdded(){
        BulkRequestInfo request = new BulkRequestInfo();
        request.deviceMRIDs = Arrays.asList("mrid1", "mrid2");
        request.scheduleIds = Arrays.asList(1L);
        Entity<BulkRequestInfo> json = Entity.json(request);

        ScheduledComTaskExecutionBuilder builder = mock(ScheduledComTaskExecutionBuilder.class);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(10L);
        when(deviceType.getName()).thenReturn("Router");

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(10L);
        when(deviceConfiguration.getName()).thenReturn("BaseConfig");

        Device device1 = mock(Device.class);
        when(device1.getmRID()).thenReturn("mrid1");
        when(device1.getName()).thenReturn("Device with mrid1");
        when(device1.getDeviceType()).thenReturn(deviceType);
        when(device1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device1.newScheduledComTaskExecution(any(ComSchedule.class))).thenReturn(builder);
        when(deviceDataService.findByUniqueMrid("mrid1")).thenReturn(device1);
        when(thesaurus.getString(anyString(), anyString())).thenReturn("translated");

        Device device2 = mock(Device.class);
        when(device2.getmRID()).thenReturn("mrid2");
        when(device2.getName()).thenReturn("Device with mrid2");
        when(device2.getDeviceType()).thenReturn(deviceType);
        when(device2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device2.newScheduledComTaskExecution(any(ComSchedule.class))).thenReturn(builder);
        doThrow(new ConstraintViolationException("already exists", null)).when(device2).save();
        when(deviceDataService.findByUniqueMrid("mrid2")).thenReturn(device2);

        ComSchedule schedule = mock(ComSchedule.class);
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(schedule));

        ComSchedulesBulkInfo response = target("/devices/schedules").request().put(json, ComSchedulesBulkInfo.class);
        assertThat(response.actions.size()).isEqualTo(1);
        assertThat(response.actions.get(0).successCount).isEqualTo(1);
        assertThat(response.actions.get(0).failCount).isEqualTo(1);
        assertThat(response.actions.get(0).fails.size()).isEqualTo(1);
        assertThat(response.actions.get(0).fails.get(0).message).isNotEmpty();
    }

    @Test
    public void testGetAllLoadProfiles() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("channel1", "1.1.1", 1);
        LoadProfile loadProfile1 = mockLoadProfile("lp3", 3, new TimeDuration(10, TimeDuration.MINUTES));
        LoadProfile loadProfile2 = mockLoadProfile("Lp2", 2, new TimeDuration(10, TimeDuration.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp1", 1, new TimeDuration(10, TimeDuration.MINUTES), channel1);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceDataService.findByUniqueMrid("mrid1")).thenReturn(device1);
        when(thesaurus.getString(anyString(), anyString())).thenReturn("translated");

        Map response = target("/devices/mrid1/loadprofiles").request().get(Map.class);
        assertThat(response).containsKey("total").containsKey("loadProfiles");
        assertThat((List)response.get("loadProfiles")).isSortedAccordingTo(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((String)o1.get("name")).compareToIgnoreCase((String) o2.get("name"));
            }
        });
    }

    @Test
    public void testGetAllLoadProfilesIsSorted() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("channel1", "1.1.1", 1);
        LoadProfile loadProfile1 = mockLoadProfile("lp3", 3, new TimeDuration(10, TimeDuration.MINUTES));
        LoadProfile loadProfile2 = mockLoadProfile("Lp2", 2, new TimeDuration(10, TimeDuration.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp1", 1, new TimeDuration(10, TimeDuration.MINUTES), channel1);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceDataService.findByUniqueMrid("mrid1")).thenReturn(device1);
        when(thesaurus.getString(anyString(), anyString())).thenReturn("translated");
    }

    @Test
    public void testGetOneLoadProfile() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("Z-channel1", "1.1", 0);
        Channel channel2 = mockChannel("A-channel2", "1.2", 1);
        LoadProfile loadProfile1 = mockLoadProfile("lp1", 1, new TimeDuration(15, TimeDuration.MINUTES), channel1, channel2);
        LoadProfile loadProfile2 = mockLoadProfile("lp2", 2, new TimeDuration(15, TimeDuration.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.MINUTES));
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceDataService.findByUniqueMrid("mrid1")).thenReturn(device1);
        when(thesaurus.getString(anyString(), anyString())).thenReturn("translated");

        Map<String, Object> response = target("/devices/mrid1/loadprofiles/1").request().get(Map.class);
        assertThat(response)
                .hasSize(6)
                .contains(MapEntry.entry("id", 1))
                .contains(MapEntry.entry("name", "lp1"))
                .contains(MapEntry.entry("lastReading", 1406617200000L))
                .contains(MapEntry.entry("obisCode", "1.2.3.4.5.1"))
                .containsKey("channels")
                .containsKey("interval");
        Map<String, Object> interval = (Map<String, Object>) response.get("interval");
        assertThat(interval)
                .contains(MapEntry.entry("count", 15))
                .contains(MapEntry.entry("timeUnit", "minutes"));

        List<Map<String, Object>> channels = (List<Map<String, Object>>) response.get("channels");
        assertThat(channels).hasSize(2);
        assertThat(channels.get(0).get("name")).isEqualTo("A-channel2");
        assertThat(channels.get(1).get("name")).isEqualTo("Z-channel1");
    }

    @Test
    public void testGetNonExistingLoadProfile() throws Exception {
        Device device1 = mock(Device.class);
        LoadProfile loadProfile1 = mockLoadProfile("lp1", 1, new TimeDuration(15, TimeDuration.MINUTES));
        LoadProfile loadProfile2 = mockLoadProfile("lp2", 2, new TimeDuration(15, TimeDuration.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.MINUTES));
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceDataService.findByUniqueMrid("mrid1")).thenReturn(device1);
        when(thesaurus.getString(anyString(), anyString())).thenReturn("translated");

        Response response = target("/devices/mrid1/loadprofiles/7").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testLoadProfileData() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("channel1", "1.1", 0);
        Channel channel2 = mockChannel("channel2", "1.2", 1);
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.MINUTES), channel1, channel2);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile3));
        when(deviceDataService.findByUniqueMrid("mrid2")).thenReturn(device1);
        List<LoadProfileReading> loadProfileReadings = new ArrayList<>();
        final long startTime = 1388534400000L;
        long start = startTime;
        for (int i=0; i<2880;i++) {
            loadProfileReadings.add(mockLoadProfileReading(loadProfile3, new Interval(new Date(start), new Date(start + 900))));
            start+=900;
        }
        when(loadProfile3.getChannelData((com.elster.jupiter.util.time.Interval) anyObject())).thenReturn(loadProfileReadings);


        Map response = target("/devices/mrid2/loadprofiles/3/data")
                .queryParam("intervalStart", startTime)
                .queryParam("intervalEnd", 1391212800000L)
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(11);
        List data = (List) response.get("data");
        assertThat(data).hasSize(10);
        assertThat((Map)data.get(0))
                .containsKey("interval")
                .containsKey("channelData")
                .containsKey("readingTime")
                .containsKey("intervalFlags");
        Map<String, Long> interval = (Map<String, Long>) ((Map) data.get(0)).get("interval");
        assertThat(interval.get("start")).isEqualTo(startTime);
        assertThat(interval.get("end")).isEqualTo(startTime+900);
        Map<String, BigDecimal> channelData = (Map<String, BigDecimal>) ((Map) data.get(0)).get("channelData");
        assertThat(channelData).hasSize(2).containsKey("0").containsKey("1");
    }

    @Test
    public void testLoadProfileChannelData() throws Exception {
        Device device1 = mock(Device.class);
        long channel_id = 7L;
        Channel channel1 = mockChannel("channel1", "1.1", channel_id);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getId()).thenReturn(channel_id);
        when(channel1.getChannelSpec()).thenReturn(channelSpec);
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.MINUTES), channel1);
        when(channel1.getLoadProfile()).thenReturn(loadProfile3);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile3));
        when(deviceDataService.findByUniqueMrid("mrid2")).thenReturn(device1);
        List<LoadProfileReading> loadProfileReadings = new ArrayList<>();
        final long startTime = 1388534400000L;
        long start = startTime;
        for (int i=0; i<2880;i++) {
            loadProfileReadings.add(mockLoadProfileReading(loadProfile3, new Interval(new Date(start), new Date(start + 900))));
            start+=900;
        }
        when(channel1.getChannelData((com.elster.jupiter.util.time.Interval) anyObject())).thenReturn(loadProfileReadings);


        Map response = target("/devices/mrid2/loadprofiles/3/channels/7/data")
                .queryParam("intervalStart", startTime)
                .queryParam("intervalEnd", 1391212800000L)
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(11);
        List data = (List) response.get("data");
        assertThat(data).hasSize(10);
        assertThat((Map)data.get(0))
                .containsKey("interval")
                .containsKey("value")
                .containsKey("readingTime")
                .containsKey("intervalFlags");
        Map<String, Long> interval = (Map<String, Long>) ((Map) data.get(0)).get("interval");
        assertThat(interval.get("start")).isEqualTo(startTime);
        assertThat(interval.get("end")).isEqualTo(startTime+900);
    }

    private LoadProfileReading mockLoadProfileReading(final LoadProfile loadProfile, Interval interval) {
        LoadProfileReading loadProfileReading = mock(LoadProfileReading.class);
        when(loadProfileReading.getFlags()).thenReturn(Arrays.asList(ProfileStatus.Flag.CORRUPTED));
        when(loadProfileReading.getReadingTime()).thenReturn(new Date());
        when(loadProfileReading.getInterval()).thenReturn(interval);
        when(loadProfileReading.getChannelValues()).thenAnswer(new Answer<Set<Map.Entry<Channel, BigDecimal>>>() {
            @Override
            public Set<Map.Entry<Channel, BigDecimal>> answer(InvocationOnMock invocationOnMock) throws Throwable {
                Map<Channel, BigDecimal> map= new HashMap<>();
                for (Channel channel : loadProfile.getChannels()) {
                    map.put(channel, BigDecimal.TEN);
                }
                return map.entrySet();
            }
        });
        return loadProfileReading;
    }

    private Channel mockChannel(String name, String mrid, long id) {
        Channel mock = mock(Channel.class);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        when(mock.getChannelSpec()).thenReturn(channelSpec);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(mock.getReadingType()).thenReturn(readingType);
        when(mock.getInterval()).thenReturn(new TimeDuration("15 minutes"));
        Phenomenon phenomenon = mock(Phenomenon.class);
        when(phenomenon.getUnit()).thenReturn(Unit.get("kWh"));
        when(mock.getPhenomenon()).thenReturn(phenomenon);
        return mock;
    }

    private LoadProfile mockLoadProfile(String name, long id, TimeDuration interval, Channel... channels) {
        LoadProfile loadProfile1 = mock(LoadProfile.class);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        when(loadProfileType.getName()).thenReturn(name);
        when(loadProfile1.getInterval()).thenReturn(interval);
        when(loadProfile1.getId()).thenReturn(id);
        when(loadProfile1.getDeviceObisCode()).thenReturn(new ObisCode(1,2,3,4,5, (int) id));
        when(loadProfile1.getChannels()).thenReturn(channels==null? Collections.<Channel>emptyList() :Arrays.asList(channels));
        when(loadProfile1.getLastReading()).thenReturn(new Date(1406617200000L)); //  (GMT): Tue, 29 Jul 2014 07:00:00 GMT
        when(loadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        return loadProfile1;
    }
}
