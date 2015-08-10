package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.rest.DevicePrivileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by bvn on 6/19/14.
 */
public class DeviceResourceTest extends DeviceDataRestApplicationJerseyTest {

    public static final Instant NOW = Instant.ofEpochMilli(1409738114);
    public ReadingType readingType;

    @Before
    public void setupStubs() {
        readingType = mockReadingType("0.1.2.3.5.6.7.8.9.1.2.3.4.5.6.7.8");
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.of(readingType));
    }

    @Test
    public void testGetConnectionMethodsJsonBindings() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getCommunicationWindow()).thenReturn(new ComWindow(100,200));
        when(connectionTask.isSimultaneousConnectionsAllowed()).thenReturn(true);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(connectionTask.getRescheduleDelay()).thenReturn(TimeDuration.minutes(15));
        when(connectionTask.getProperties()).thenReturn(Collections.emptyList());
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("occp");
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        NextExecutionSpecs nextExecSpecs = mock(NextExecutionSpecs.class);
        when(nextExecSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.minutes(60)));
        when(connectionTask.getNextExecutionSpecs()).thenReturn(nextExecSpecs);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getName()).thenReturn("sct");
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());
        when(pluggableClass.getName()).thenReturn("ctpc");
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?, ?>>asList(connectionTask));
        Map<String, Object> response = target("/devices/1/connectionmethods").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("connectionMethods");
        List<Map<String, Object>> connectionMethods = (List<Map<String, Object>>) response.get("connectionMethods");
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
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE;
        info.isDefault = false;
        info.comPortPool = "cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");
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
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault = false;
        info.comPortPool = "cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");
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
        verify(connectionTaskService, never()).setDefaultConnectionTask(connectionTask);
    }

    @Test
    public void testCreateDefaultInboundConnectionMethod() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault = true;
        info.comPortPool = "cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");
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
        verify(connectionTaskService).setDefaultConnectionTask(connectionTask);
    }

    @Test
    public void testUpdateAndUndefaultInboundConnectionMethod() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault = false;
        info.comPortPool = "cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?, ?>>asList(connectionTask));
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
        verify(connectionTaskService, times(1)).clearDefaultConnectionTask(device);
    }

    @Test
    public void testUpdateOnlyClearsDefaultIfConnectionMethodWasDefaultBeforeUpdate() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "inbConnMethod";
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.isDefault = false;
        info.comPortPool = "cpp";

        Device device = mock(Device.class);
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = mock(Device.InboundConnectionTaskBuilder.class);
        when(device.getInboundConnectionTaskBuilder(Matchers.<PartialInboundConnectionTask>any())).thenReturn(inboundConnectionTaskBuilder);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTaskBuilder.add()).thenReturn(connectionTask);
        doReturn(Optional.of(comPortPool)).when(engineConfigurationService).findInboundComPortPoolByName("cpp");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?, ?>>asList(connectionTask));
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
        verify(connectionTaskService, never()).clearDefaultConnectionTask(device);
    }

    @Test
    public void testComSchedulesBulkActionsWithWrongDevice() {
        BulkRequestInfo request = new BulkRequestInfo();
        request.deviceMRIDs = Arrays.asList("mrid1", "unexisting");
        request.scheduleIds = Arrays.asList(1L);
        Entity<BulkRequestInfo> json = Entity.json(request);

        ComTaskExecutionBuilder<ScheduledComTaskExecution> builder = mock(ComTaskExecutionBuilder.class);

        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn("mrid1");
        when(device.getName()).thenReturn("Device with mrid1");
        when(device.newScheduledComTaskExecution(any(ComSchedule.class))).thenReturn(builder);
        when(deviceService.findByUniqueMrid("mrid1")).thenReturn(Optional.of(device));
        when(deviceService.findByUniqueMrid("unexisting")).thenReturn(Optional.<Device>empty());

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
    public void testComSchedulesBulkActionsAddAlreadyAdded() {
        BulkRequestInfo request = new BulkRequestInfo();
        request.deviceMRIDs = Arrays.asList("mrid1", "mrid2");
        request.scheduleIds = Arrays.asList(1L);
        Entity<BulkRequestInfo> json = Entity.json(request);

        ComTaskExecutionBuilder<ScheduledComTaskExecution> builder = mock(ComTaskExecutionBuilder.class);
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
        when(deviceService.findByUniqueMrid("mrid1")).thenReturn(Optional.of(device1));
        doReturn("translated").when(thesaurus).getString(anyString(), anyString());

        Device device2 = mock(Device.class);
        when(device2.getmRID()).thenReturn("mrid2");
        when(device2.getName()).thenReturn("Device with mrid2");
        when(device2.getDeviceType()).thenReturn(deviceType);
        when(device2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device2.newScheduledComTaskExecution(any(ComSchedule.class))).thenReturn(builder);
        doThrow(new ConstraintViolationException("already exists", null)).when(device2).save();
        when(deviceService.findByUniqueMrid("mrid2")).thenReturn(Optional.of(device2));

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
        LoadProfile loadProfile1 = mockLoadProfile("lp3", 3, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile2 = mockLoadProfile("Lp2", 2, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp1", 1, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES), channel1);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceService.findByUniqueMrid("mrid1")).thenReturn(Optional.of(device1));
        doReturn("translated").when(thesaurus).getString(anyString(), anyString());
        when(channel1.getReadingType()).thenReturn(readingType);

        Map response = target("/devices/mrid1/loadprofiles").request().get(Map.class);
        assertThat(response).containsKey("total").containsKey("loadProfiles");
        assertThat((List) response.get("loadProfiles")).isSortedAccordingTo(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((String) o1.get("name")).compareToIgnoreCase((String) o2.get("name"));
            }
        });
    }

    @Test
    public void testGetAllLoadProfilesIsSorted() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("channel1", "1.1.1", 1);
        LoadProfile loadProfile1 = mockLoadProfile("lp3", 3, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile2 = mockLoadProfile("Lp2", 2, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp1", 1, new TimeDuration(10, TimeDuration.TimeUnit.MINUTES), channel1);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceService.findByUniqueMrid("mrid1")).thenReturn(Optional.of(device1));
        doReturn("translated").when(thesaurus).getString(anyString(), anyString());
    }

    @Test
    public void testGetOneLoadProfile() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("Z-channel1", "1.1", 0);
        Channel channel2 = mockChannel("A-channel2", "1.2", 1);
        LoadProfile loadProfile1 = mockLoadProfile("lp1", 1, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES), channel1, channel2);
        LoadProfile loadProfile2 = mockLoadProfile("lp2", 2, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceService.findByUniqueMrid("mrid1")).thenReturn(Optional.of(device1));
        doReturn("translated").when(thesaurus).getString(anyString(), anyString());
        when(clock.instant()).thenReturn(NOW);
        when(channel1.getDevice()).thenReturn(device1);
        when(channel2.getDevice()).thenReturn(device1);
        when(channel1.getLastDateTime()).thenReturn(Optional.empty());
        when(channel2.getLastDateTime()).thenReturn(Optional.empty());
        when(channel1.getLoadProfile()).thenReturn(loadProfile1);
        when(channel2.getLoadProfile()).thenReturn(loadProfile1);
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(device1.forValidation()).thenReturn(deviceValidation);
        when(channel1.getReadingType()).thenReturn(readingType);
        when(channel2.getReadingType()).thenReturn(readingType);

        Map<String, Object> response = target("/devices/mrid1/loadprofiles/1").request().get(Map.class);
        assertThat(response)
                .hasSize(7)
                .contains(MapEntry.entry("id", 1))
                .contains(MapEntry.entry("name", "lp1"))
                .contains(MapEntry.entry("lastReading", 1406617200000L))
                .contains(MapEntry.entry("obisCode", "1.2.3.4.5.1"))
                .containsKey("channels")
                .containsKey("validationInfo")
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
        LoadProfile loadProfile1 = mockLoadProfile("lp1", 1, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile2 = mockLoadProfile("lp2", 2, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES));
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2, loadProfile3));
        when(deviceService.findByUniqueMrid("mrid1")).thenReturn(Optional.of(device1));
        doReturn("translated").when(thesaurus).getString(anyString(), anyString());

        Response response = target("/devices/mrid1/loadprofiles/7").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testLoadProfileData() throws Exception {
        Device device1 = mock(Device.class);
        Channel channel1 = mockChannel("channel1", "1.1", 0);
        Channel channel2 = mockChannel("channel2", "1.2", 1);
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES), channel1, channel2);
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile3));
        when(deviceService.findByUniqueMrid("mrid2")).thenReturn(Optional.of(device1));
        when(channel1.getDevice()).thenReturn(device1);
        when(channel2.getDevice()).thenReturn(device1);
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(device1.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(channel1, NOW)).thenReturn(true);
        when(deviceValidation.isValidationActive(channel2, NOW)).thenReturn(true);
        List<LoadProfileReading> loadProfileReadings = new ArrayList<>();
        final long startTime = 1388534400000L;
        long start = startTime;
        for (int i = 0; i < 2880; i++) {
            loadProfileReadings.add(mockLoadProfileReading(loadProfile3, Ranges.openClosed(Instant.ofEpochMilli(start), Instant.ofEpochMilli(start + 900))));
            start += 900;
        }
        when(loadProfile3.getChannelData(any(Range.class))).thenReturn(loadProfileReadings);

        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":" + startTime + "},{\"property\":\"intervalEnd\",\"value\":1391212800000}]");
        Map response = target("/devices/mrid2/loadprofiles/3/data")
                .queryParam("filter", filter)
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(11);
        List data = (List) response.get("data");
        assertThat(data).hasSize(10);
        assertThat((Map) data.get(0))
                .containsKey("interval")
                .containsKey("channelData")
                .containsKey("readingTime")
                .containsKey("intervalFlags");
        Map<String, Long> interval = (Map<String, Long>) ((Map) data.get(0)).get("interval");
        assertThat(interval.get("start")).isEqualTo(startTime);
        assertThat(interval.get("end")).isEqualTo(startTime + 900);
        Map<String, BigDecimal> channelData = (Map<String, BigDecimal>) ((Map) data.get(0)).get("channelData");
        assertThat(channelData).hasSize(2).containsKey("0").containsKey("1");
    }

    @Test
    public void testLoadProfileChannelData() throws Exception {
        Device device1 = mock(Device.class);
        long channel_id = 7L;
        Channel channel1 = mockChannel("channel1", "1.1", channel_id);
        when(channel1.getDevice()).thenReturn(device1);
        DeviceValidation deviceValidation = mock(DeviceValidation.class);
        when(device1.forValidation()).thenReturn(deviceValidation);
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getId()).thenReturn(channel_id);
        when(channel1.getChannelSpec()).thenReturn(channelSpec);
        LoadProfile loadProfile3 = mockLoadProfile("lp3", 3, new TimeDuration(15, TimeDuration.TimeUnit.MINUTES), channel1);
        when(channel1.getLoadProfile()).thenReturn(loadProfile3);
        when(channel1.getReadingType()).thenReturn(readingType);
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(device1.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile3));
        when(deviceService.findByUniqueMrid("mrid2")).thenReturn(Optional.of(device1));
        List<LoadProfileReading> loadProfileReadings = new ArrayList<>();
        final long startTime = 1388534400000L;
        long start = startTime;
        for (int i = 0; i < 2880; i++) {
            loadProfileReadings.add(mockLoadProfileReading(loadProfile3, Ranges.openClosed(Instant.ofEpochMilli(start), Instant.ofEpochMilli(start + 900))));
            start += 900;
        }
        when(channel1.getChannelData(any(Range.class))).thenReturn(loadProfileReadings);

        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":1410774630000},{\"property\":\"intervalEnd\",\"value\":1410828630000}]");
        Map response = target("/devices/mrid2/channels/7/data")
                .queryParam("filter", filter)
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(11);
        List data = (List) response.get("data");
        assertThat(data).hasSize(10);
        assertThat((Map) data.get(0))
                .containsKey("interval")
                .containsKey("value")
                .containsKey("readingTime")
                .containsKey("intervalFlags");
        Map<String, Long> interval = (Map<String, Long>) ((Map) data.get(0)).get("interval");
        assertThat(interval.get("start")).isEqualTo(startTime);
        assertThat(interval.get("end")).isEqualTo(startTime + 900);
    }

    @Test
    public void testGetAllLogBooksReturnsEmptyList() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        Map response = target("/devices/mrid/logbooks").queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        assertThat(response)
                .contains(MapEntry.entry("total", 0))
                .containsKey("data");
    }

    @Test
    public void testGetAllLogBooks() {
        Instant lastLogBook = Instant.now();
        Instant lastReading = Instant.now();

        Device device = mock(Device.class);
        List<LogBook> logBooks = new ArrayList<>();
        logBooks.add(mockLogBook("C_LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", lastLogBook, lastReading));
        logBooks.add(mockLogBook("B_LogBook", 2L, "0.0.0.0.0.3", "0.0.0.0.0.4", lastLogBook, lastReading));
        logBooks.add(mockLogBook("A_LogBook", 3L, "0.0.0.0.0.5", "0.0.0.0.0.6", lastLogBook, lastReading));

        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(logBooks);

        Map response = target("/devices/mrid/logbooks").queryParam("start", 0).queryParam("limit", 2).request().get(Map.class);

        assertThat(response)
                .contains(MapEntry.entry("total", 3))
                .containsKey("data");

        assertThat((List) response.get("data")).hasSize(2);

        Map logBookInfo1 = (Map) ((List) response.get("data")).get(0);
        assertThat(logBookInfo1)
                .contains(MapEntry.entry("id", 3))
                .contains(MapEntry.entry("name", "A_LogBook"))
                .contains(MapEntry.entry("obisCode", "0.0.0.0.0.5"))
                .contains(MapEntry.entry("overruledObisCode", "0.0.0.0.0.6"))
                .contains(MapEntry.entry("lastEventDate", lastLogBook.toEpochMilli()))
                .contains(MapEntry.entry("lastReading", lastReading.toEpochMilli()));

        Map logBookInfo2 = (Map) ((List) response.get("data")).get(1);
        assertThat(logBookInfo2)
                .contains(MapEntry.entry("id", 2))
                .contains(MapEntry.entry("name", "B_LogBook"))
                .contains(MapEntry.entry("obisCode", "0.0.0.0.0.3"))
                .contains(MapEntry.entry("overruledObisCode", "0.0.0.0.0.4"))
                .contains(MapEntry.entry("lastEventDate", lastLogBook.toEpochMilli()))
                .contains(MapEntry.entry("lastReading", lastReading.toEpochMilli()));
    }

    @Test
    public void testGetLogBookById() {
        Instant lastLogBook = Instant.now();
        Instant lastReading = Instant.now();

        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", lastLogBook, lastReading);
        EndDeviceEventRecord endDeviceEvent = mock(EndDeviceEventRecord.class);
        EndDeviceEventType endDeviceEventType = mock(EndDeviceEventType.class);

        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));
        when(logBook.getEndDeviceEvents(Matchers.any(Range.class))).thenReturn(Arrays.asList(endDeviceEvent));
        when(endDeviceEvent.getEventType()).thenReturn(endDeviceEventType);
        when(endDeviceEventType.getMRID()).thenReturn("0.2.38.57");
        when(endDeviceEventType.getType()).thenReturn(EndDeviceType.NA);
        when(endDeviceEventType.getDomain()).thenReturn(EndDeviceDomain.BATTERY);
        when(endDeviceEventType.getSubDomain()).thenReturn(EndDeviceSubDomain.VOLTAGE);
        when(endDeviceEventType.getEventOrAction()).thenReturn(EndDeviceEventorAction.DECREASED);
        when(nlsService.getThesaurus(Matchers.anyString(), Matchers.<Layer>any())).thenReturn(thesaurus);

        LogBookInfo info = target("/devices/mrid/logbooks/1").request().get(LogBookInfo.class);

        assertThat(info.id).isEqualTo(1);
        assertThat(info.name).isEqualTo("LogBook");
        assertThat(info.obisCode).isEqualTo(ObisCode.fromString("0.0.0.0.0.1"));
        assertThat(info.overruledObisCode).isEqualTo(ObisCode.fromString("0.0.0.0.0.2"));
        assertThat(info.lastEventDate).isEqualTo(lastLogBook);
        assertThat(info.lastReading).isEqualTo(lastReading);

        EndDeviceEventTypeInfo eventType = info.lastEventType;
        assertThat(eventType.code).isEqualTo("0.2.38.57");
        assertThat(eventType.deviceType.id).isEqualTo(0);
        assertThat(eventType.deviceType.name).isEqualTo("NA");
        assertThat(eventType.domain.id).isEqualTo(2);
        assertThat(eventType.domain.name).isEqualTo("Battery");
        assertThat(eventType.subDomain.id).isEqualTo(38);
        assertThat(eventType.subDomain.name).isEqualTo("Voltage");
        assertThat(eventType.eventOrAction.id).isEqualTo(57);
        assertThat(eventType.eventOrAction.name).isEqualTo("Decreased");
    }

    @Test
    public void testGetLogBookByIdNotFound() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/mrid/logbooks/134").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookDataIncorrectIntervalParameter() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/mrid/logbooks/1/data").queryParam("filter", "[%7B%22property%22:%22intervalStart%22,%22value%22:2%7D,%7B%22property%22:%22intervalEnd%22,%22value%22:1%7D]").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookDataInvalidDomainParameter() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/mrid/logbooks/1/data").queryParam("filter", "[%7B%22property%22:%22domain%22,%22value%22:%22100500%22%7D]").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookDataInvalidSubDomainParameter() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/mrid/logbooks/1/data").queryParam("filter", "[%7B%22property%22:%22subDomain%22,%22value%22:%22100500%22%7D]").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookDataInvalidEventOrActionParameter() {
        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);

        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));

        Response response = target("/devices/mrid/logbooks/1/data").queryParam("filter", "[%7B%22property%22:%22eventOrAction%22,%22value%22:%22100500%22%7D]").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogBookData() {
        Instant start = Instant.now();
        Instant end = Instant.now();
        String message = "Message";
        int eventLogId = 13;
        String deviceCode = "DeviceEventType";
        String eventTypeCode = "0.2.1.4";
        EndDeviceType type = EndDeviceType.NA;
        EndDeviceDomain domain = EndDeviceDomain.BATTERY;
        EndDeviceSubDomain subDomain = EndDeviceSubDomain.ACCESS;
        EndDeviceEventorAction eventorAction = EndDeviceEventorAction.ACTIVATED;

        Device device = mock(Device.class);
        LogBook logBook = mockLogBook("LogBook", 1L, "0.0.0.0.0.1", "0.0.0.0.0.2", null, null);
        EndDeviceEventRecord endDeviceEventRecord = mock(EndDeviceEventRecord.class);
        EndDeviceEventType endDeviceType = mock(EndDeviceEventType.class);

        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));
        when(device.getLogBooks()).thenReturn(Arrays.asList(logBook));
        List<EndDeviceEventRecord> records = new ArrayList<>();
        records.add(endDeviceEventRecord);
        when(logBook.getEndDeviceEventsByFilter(Matchers.<EndDeviceEventRecordFilterSpecification>any())).thenReturn(records);
        when(endDeviceEventRecord.getCreatedDateTime()).thenReturn(start);
        when(endDeviceEventRecord.getModTime()).thenReturn(end);
        when(endDeviceEventRecord.getDescription()).thenReturn(message);
        when(endDeviceEventRecord.getLogBookPosition()).thenReturn(eventLogId);
        when(endDeviceEventRecord.getEventType()).thenReturn(endDeviceType);
        when(endDeviceEventRecord.getDeviceEventType()).thenReturn(deviceCode);
        when(endDeviceType.getMRID()).thenReturn(eventTypeCode);
        when(endDeviceType.getType()).thenReturn(type);
        when(endDeviceType.getDomain()).thenReturn(domain);
        when(endDeviceType.getSubDomain()).thenReturn(subDomain);
        when(endDeviceType.getEventOrAction()).thenReturn(eventorAction);

        when(nlsService.getThesaurus(Matchers.anyString(), Matchers.<Layer>any())).thenReturn(thesaurus);

        Map<?, ?> response = target("/devices/mrid/logbooks/1/data")
                .queryParam("filter", ("[{\"property\":\"intervalStart\",\"value\":1},"
                        + "{\"property\":\"intervalEnd\",\"value\":2},"
                        + "{\"property\":\"domain\",\"value\":\"BATTERY\"},"
                        + "{\"property\":\"subDomain\",\"value\":\"ACCESS\"},"
                        + "{\"property\":\"eventOrAction\",\"value\":\"ACTIVATED\"}]").replace("{", "%7B").replace("}", "%7D").replace("\"", "%22")).request().get(Map.class);

        assertThat(response.get("total")).isEqualTo(1);

        List<?> infos = (List<?>) response.get("data");
        assertThat(infos).hasSize(1);
        assertThat((Map<String, Object>) infos.get(0))
                .contains(MapEntry.entry("eventDate", start.toEpochMilli()))
                .contains(MapEntry.entry("deviceCode", deviceCode))
                .contains(MapEntry.entry("eventLogId", eventLogId))
                .contains(MapEntry.entry("readingDate", end.toEpochMilli()))
                .contains(MapEntry.entry("message", message));

        Map<String, Object> eventType = (Map<String, Object>) ((Map<String, Object>) infos.get(0)).get("eventType");
        assertThat(eventType).contains(MapEntry.entry("code", eventTypeCode));
        Map<String, Object> deviceTypeMap = (Map<String, Object>) eventType.get("deviceType");
        assertThat(deviceTypeMap).contains(MapEntry.entry("id", type.getValue())).contains(MapEntry.entry("name", type.getMnemonic()));

        Map<String, Object> domainTypeMap = (Map<String, Object>) eventType.get("domain");
        assertThat(domainTypeMap).contains(MapEntry.entry("id", domain.getValue())).contains(MapEntry.entry("name", domain.getMnemonic()));

        Map<String, Object> subDomainMap = (Map<String, Object>) eventType.get("subDomain");
        assertThat(subDomainMap).contains(MapEntry.entry("id", subDomain.getValue())).contains(MapEntry.entry("name", subDomain.getMnemonic()));

        Map<String, Object> eventOrActionMap = (Map<String, Object>) eventType.get("eventOrAction");
        assertThat(eventOrActionMap).contains(MapEntry.entry("id", eventorAction.getValue())).contains(MapEntry.entry("name", eventorAction.getMnemonic()));
    }

    @Test
    public void testGetCommunicationTopology() {
        mockTopologyTimeline();

        String response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(7);
        assertThat(model.<List>get("$.slaveDevices")).hasSize(7);
        assertThat(model.<String>get("$.slaveDevices[0].mRID")).isEqualTo("slave1");
        assertThat(model.<String>get("$.slaveDevices[6].mRID")).isEqualTo("slave7");
    }

    @Test
    public void testCommunicationTopologyPaging() {
        mockTopologyTimeline();
        String response = target("/devices/gateway/topology/communication")
                .queryParam("start", 3).queryParam("limit", 2)
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(6); // 3 (start) + 2 (limit) + 1 (for FE)
        assertThat(model.<List>get("$.slaveDevices")).hasSize(2);
        assertThat(model.<String>get("$.slaveDevices[0].mRID")).isEqualTo("slave4");
        assertThat(model.<String>get("$.slaveDevices[1].mRID")).isEqualTo("slave5");
    }

    @Test
    public void testGetCommunicationTopologyPagingBigStart() {
        mockTopologyTimeline();
        String response = target("/devices/gateway/topology/communication")
                .queryParam("start", 1000).queryParam("limit", 2)
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(1000); // 1000 (start) + 0 (limit) + 0 (for FE: no additional pages)
        assertThat(model.<List>get("$.slaveDevices")).hasSize(0);
    }

    @Test
    public void testGetCommunicationTopologyPagingBigEnd() {
        mockTopologyTimeline();
        String response = target("/devices/gateway/topology/communication")
                .queryParam("start", 6).queryParam("limit", 1000)
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(7);
        assertThat(model.<List>get("$.slaveDevices")).hasSize(1);
    }

    @Test
    public void testGetCommunicationTopologyNoPaging() {
        mockTopologyTimeline();
        String response = target("/devices/gateway/topology/communication")
                .request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(7);
        assertThat(model.<List>get("$.slaveDevices")).hasSize(7);
    }

    private void mockTopologyTimeline() {
        Device gateway = mockDeviceForTopologyTest("gateway");
        Device slave1 = mockDeviceForTopologyTest("slave1");
        Device slave2 = mockDeviceForTopologyTest("slave2");
        Device slave3 = mockDeviceForTopologyTest("slave3");
        Device slave4 = mockDeviceForTopologyTest("slave4");
        Device slave5 = mockDeviceForTopologyTest("slave5");
        Device slave6 = mockDeviceForTopologyTest("slave6");
        Device slave7 = mockDeviceForTopologyTest("slave7");
        Set<Device> slaves = new HashSet<>(Arrays.<Device>asList(slave5, slave2, slave7, slave4, slave1, slave6, slave3));

        TopologyTimeline topologyTimeline = mock(TopologyTimeline.class);
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        when(topologyTimeline.mostRecentlyAddedOn(slave1)).thenReturn(Optional.of(Instant.ofEpochMilli(10L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave2)).thenReturn(Optional.of(Instant.ofEpochMilli(20L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave3)).thenReturn(Optional.of(Instant.ofEpochMilli(30L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave4)).thenReturn(Optional.of(Instant.ofEpochMilli(40L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave5)).thenReturn(Optional.of(Instant.ofEpochMilli(50L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave6)).thenReturn(Optional.of(Instant.ofEpochMilli(60L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave7)).thenReturn(Optional.of(Instant.ofEpochMilli(70L)));

        when(deviceService.findByUniqueMrid("gateway")).thenReturn(Optional.of(gateway));
        when(topologyService.getPysicalTopologyTimeline(gateway)).thenReturn(topologyTimeline);
    }

    @Test
    public void testGetCommunicationTopologyFilter() throws Exception {
        Device gateway = mockDeviceForTopologyTest("gateway");
        Device slave1 = mockDeviceForTopologyTest("SimpleStringMrid");
        Device slave2 = mockDeviceForTopologyTest("123456789");
        when(slave2.getSerialNumber()).thenReturn(null);
        Set<Device> slaves = new HashSet<>(Arrays.<Device>asList(slave1, slave2));

        TopologyTimeline topologyTimeline = mock(TopologyTimeline.class);
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        when(topologyTimeline.mostRecentlyAddedOn(slave1)).thenReturn(Optional.of(Instant.ofEpochMilli(10L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave2)).thenReturn(Optional.of(Instant.ofEpochMilli(20L)));

        when(deviceService.findByUniqueMrid("gateway")).thenReturn(Optional.of(gateway));
        when(topologyService.getPysicalTopologyTimeline(gateway)).thenReturn(topologyTimeline);


        Map<?, ?> response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"mrid\",\"value\":\"*\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"mrid\",\"value\":\"%\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"mrid\",\"value\":\"Simple%Mrid\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"mrid\",\"value\":\"Simple?Mrid\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(0);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"mrid\",\"value\":\"1234*\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"mrid\",\"value\":\"*789\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"mrid\",\"value\":\"%34*7?9\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);
    }


    @Test
    public void testGetCommunicationTopologyFilterOnSerialNumber() throws Exception {
        Device gateway = mockDeviceForTopologyTest("gateway");
        Device slave1 = mockDeviceForTopologyTest("SimpleStringMrid");
        Device slave2 = mockDeviceForTopologyTest("123456789");
        when(slave2.getSerialNumber()).thenReturn(null);
        Set<Device> slaves = new HashSet<>(Arrays.<Device>asList(slave1, slave2));

        TopologyTimeline topologyTimeline = mock(TopologyTimeline.class);
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        when(topologyTimeline.mostRecentlyAddedOn(slave1)).thenReturn(Optional.of(Instant.ofEpochMilli(10L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave2)).thenReturn(Optional.of(Instant.ofEpochMilli(20L)));

        when(deviceService.findByUniqueMrid("gateway")).thenReturn(Optional.of(gateway));
        when(topologyService.getPysicalTopologyTimeline(gateway)).thenReturn(topologyTimeline);


        Map<?, ?> response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"*\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"%\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(2);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"D(E%\\\\Q\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(0);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"123456?89\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"1234*\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"*789\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);

        response = target("/devices/gateway/topology/communication")
                .queryParam("start", 0).queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"serialNumber\",\"value\":\"%34*7?9\"}]", "UTF-8"))
                .request().get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);
    }

    @Test
    public void testDeviceTopologyInfo() {
        Device slave1 = mockDeviceForTopologyTest("slave1");
        Device slave2 = mockDeviceForTopologyTest("slave2");
        Device slave3 = mockDeviceForTopologyTest("slave3");
        Device slave4 = mockDeviceForTopologyTest("slave4");
        Device slave5 = mockDeviceForTopologyTest("slave5");
        Device slave6 = mockDeviceForTopologyTest("slave6");
        Device slave7 = mockDeviceForTopologyTest("slave7");
        Set<Device> slaves = new HashSet<>(Arrays.<Device>asList(slave3, slave4, slave5, slave6, slave7));

        TopologyTimeline topologyTimeline = mock(TopologyTimeline.class);
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        when(topologyTimeline.mostRecentlyAddedOn(slave1)).thenReturn(Optional.of(Instant.ofEpochMilli(10L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave2)).thenReturn(Optional.of(Instant.ofEpochMilli(20L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave3)).thenReturn(Optional.of(Instant.ofEpochMilli(30L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave4)).thenReturn(Optional.of(Instant.ofEpochMilli(40L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave5)).thenReturn(Optional.of(Instant.ofEpochMilli(50L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave6)).thenReturn(Optional.of(Instant.ofEpochMilli(60L)));
        when(topologyTimeline.mostRecentlyAddedOn(slave7)).thenReturn(Optional.of(Instant.ofEpochMilli(70L)));

        List<DeviceTopologyInfo> infos = DeviceTopologyInfo.from(topologyTimeline);

        assertThat(infos.size()).isEqualTo(5);
        assertThat(infos.get(0).mRID).isEqualTo("slave7");
        assertThat(infos.get(1).mRID).isEqualTo("slave6");
        assertThat(infos.get(2).mRID).isEqualTo("slave5");
        assertThat(infos.get(3).mRID).isEqualTo("slave4");
        assertThat(infos.get(4).mRID).isEqualTo("slave3");

        slaves = new HashSet<>(Arrays.<Device>asList(slave1));
        when(topologyTimeline.getAllDevices()).thenReturn(slaves);
        infos = DeviceTopologyInfo.from(topologyTimeline);
        assertThat(infos.size()).isEqualTo(1);
    }

    @Test
    public void testUpdateMasterDevice() {
        Device device = mockDeviceForTopologyTest("device");
        when(device.getCurrentMeterActivation()).thenReturn(Optional.empty());
        Device gateway = mockDeviceForTopologyTest("gateway");
        when(deviceService.findAndLockDeviceByIdAndVersion(1L, 13L)).thenReturn(Optional.of(device));
        when(deviceService.findByUniqueMrid("gateway")).thenReturn(Optional.of(gateway));
        when(batchService.findBatch(device)).thenReturn(Optional.empty());
        Device oldGateway = mockDeviceForTopologyTest("oldGateway");
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(oldGateway));

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13L;
        info.masterDeviceId = gateway.getId();
        info.masterDevicemRID = gateway.getmRID();

        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(topologyService).setPhysicalGateway(device, gateway);
    }

    @Test
    public void testImpossibleToSetMasterDeviceBecauseItIsGateway() {
        Device device = mockDeviceForTopologyTest("device");
        DeviceConfiguration deviceConfig = device.getDeviceConfiguration();
        when(deviceConfig.isDirectlyAddressable()).thenReturn(true);
        when(deviceService.findByUniqueMrid("device")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByIdAndVersion(1L, 13L)).thenReturn(Optional.of(device));

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13l;
        info.masterDeviceId = 2L;
        info.masterDevicemRID = "2";

        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDeleteMasterDevice() {
        Device device = mockDeviceForTopologyTest("device");
        when(device.getCurrentMeterActivation()).thenReturn(Optional.empty());
        when(deviceService.findByUniqueMrid("device")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByIdAndVersion(1L, 13L)).thenReturn(Optional.of(device));
        when(batchService.findBatch(device)).thenReturn(Optional.empty());
        Device oldMaster = mock(Device.class);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(oldMaster));

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13l;
        info.masterDevicemRID = null;

        Response response = target("/devices/1").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(topologyService).clearPhysicalGateway(device);
    }

    @Test
    public void testActivateEstimationOnDevice() {
        Device device = mockDeviceForTopologyTest("device");
        when(deviceService.findAndLockDeviceByIdAndVersion(1L, 13L)).thenReturn(Optional.of(device));
        when(deviceService.findByUniqueMrid("device")).thenReturn(Optional.of(device));
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(batchService.findBatch(device)).thenReturn(Optional.empty());
        when(device.getCurrentMeterActivation()).thenReturn(Optional.empty());

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13l;
        info.estimationStatus = new DeviceEstimationStatusInfo();
        info.estimationStatus.active = true;

        Response response = target("/devices/device/estimationrulesets/esimationstatus").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(device.forEstimation()).activateEstimation();
    }

    @Test
    public void testDeactivateEstimationOnDevice() {
        Device device = mockDeviceForTopologyTest("device");
        when(deviceService.findAndLockDeviceByIdAndVersion(1L, 13L)).thenReturn(Optional.of(device));
        when(deviceService.findByUniqueMrid("device")).thenReturn(Optional.of(device));
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(batchService.findBatch(device)).thenReturn(Optional.empty());
        when(device.getCurrentMeterActivation()).thenReturn(Optional.empty());

        DeviceInfo info = new DeviceInfo();
        info.id = 1L;
        info.version = 13l;
        info.estimationStatus = new DeviceEstimationStatusInfo();
        info.estimationStatus.active = false;

        Response response = target("/devices/device/estimationrulesets/esimationstatus").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(device.forEstimation()).deactivateEstimation();
    }

    private Device mockDeviceForTopologyTest(String name) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(device.getmRID()).thenReturn(name);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(name + "DeviceType");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name + "DeviceConfig");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSerialNumber()).thenReturn("123456789");
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(pluggableClass);
        when(pluggableClass.getId()).thenReturn(10L);
        DeviceEstimation deviceEstimation = mock(DeviceEstimation.class);
        when(device.forEstimation()).thenReturn(deviceEstimation);
        mockForCountDataValidationIssues();
        State state = mockDeviceState("In stock");
        when(device.getState()).thenReturn(state);
        Instant now = Instant.now();
        CIMLifecycleDates dates = mock(CIMLifecycleDates.class);
        when(dates.getReceivedDate()).thenReturn(Optional.of(now.minus(5, ChronoUnit.DAYS)));
        when(dates.getInstalledDate()).thenReturn(Optional.of(now.minus(4, ChronoUnit.DAYS)));
        when(dates.getRemovedDate()).thenReturn(Optional.of(now.minus(3, ChronoUnit.DAYS)));
        when(dates.getRetiredDate()).thenReturn(Optional.of(now.minus(2, ChronoUnit.DAYS)));
        when(device.getLifecycleDates()).thenReturn(dates);
        return device;
    }

    private LoadProfileReading mockLoadProfileReading(final LoadProfile loadProfile, Range<Instant> interval) {
        LoadProfileReading loadProfileReading = mock(LoadProfileReading.class);
        IntervalReadingRecord intervalReadingRecord = mock(IntervalReadingRecord.class);
        when(intervalReadingRecord.getValue()).thenReturn(BigDecimal.TEN);
        when(loadProfileReading.getFlags()).thenReturn(Arrays.asList(ProfileStatus.Flag.CORRUPTED));
        when(loadProfileReading.getReadingTime()).thenReturn(Instant.now());
        when(loadProfileReading.getRange()).thenReturn(interval);
        Map<Channel, IntervalReadingRecord> map = new HashMap<>();
        for (Channel channel : loadProfile.getChannels()) {
            map.put(channel, intervalReadingRecord);
        }
        when(loadProfileReading.getChannelValues()).thenReturn(map);
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
        when(mock.getReadingType().getCalculatedReadingType()).thenReturn(Optional.of(readingType));
        when(mock.getInterval()).thenReturn(new TimeDuration("15 minutes"));
        Unit unit = Unit.get("kWh");
        when(mock.getLastReading()).thenReturn(Optional.empty());
        when(mock.getUnit()).thenReturn(unit);
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
        when(loadProfile1.getDeviceObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, (int) id));
        when(loadProfile1.getChannels()).thenReturn(channels == null ? Collections.<Channel>emptyList() : Arrays.asList(channels));
        when(loadProfile1.getLastReading()).thenReturn(Optional.of(Instant.ofEpochMilli(1406617200000L))); //  (GMT): Tue, 29 Jul 2014 07:00:00 GMT
        when(loadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        return loadProfile1;
    }

    private LogBook mockLogBook(String name, long id, String obis, String overruledObis, Instant lastLogBook, Instant lastReading) {
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getName()).thenReturn(name);

        LogBook logBook = mock(LogBook.class);
        when(logBook.getId()).thenReturn(id);
        when(logBook.getLogBookType()).thenReturn(logBookType);
        when(logBookType.getObisCode()).thenReturn(ObisCode.fromString(obis));
        when(logBook.getDeviceObisCode()).thenReturn(ObisCode.fromString(overruledObis));
        Optional<Instant> lastLogBookOptional = Optional.ofNullable(lastLogBook);
        when(logBook.getLastLogBook()).thenReturn(lastLogBookOptional);
        Optional<Instant> lastReadingOptional = Optional.ofNullable(lastReading);
        when(logBook.getLatestEventAdditionDate()).thenReturn(lastReadingOptional);
        return logBook;
    }

    private State mockDeviceState(String name) {
        State state = mock(State.class);
        when(state.getName()).thenReturn(name);
        return state;
    }

    private void mockForCountDataValidationIssues() {
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        Meter meter = mock(Meter.class);
        when(amrSystem.findMeter(Matchers.anyString())).thenReturn(Optional.of(meter));
        IssueStatus status = mock(IssueStatus.class);
        when(issueService.findStatus(Matchers.anyString())).thenReturn(Optional.of(status));
        Finder finder = mock(Finder.class);
        when(issueDataValidationService.findAllDataValidationIssues(Matchers.any())).thenReturn(finder);
        when(finder.find()).thenReturn(Collections.emptyList());
    }

    @Test
    public void testPrivilegesForInStockState(){
        State state = mock(State.class);
        when(state.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device device = mock(Device.class);
        when(device.getState()).thenReturn(state);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));

        String response = target("/devices/1/privileges").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(16);
        List<String> privileges = model.<List<String>>get("$.privileges[*].name");
        assertThat(privileges).contains(
                DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TOPOLOGY,
                DevicePrivileges.DEVICES_WIDGET_CONNECTION,
                DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TASKS,
                DevicePrivileges.DEVICES_ACTIONS_VALIDATION_RULE_SETS,
                DevicePrivileges.DEVICES_ACTIONS_ESTIMATION_RULE_SETS,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_PLANNING,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TOPOLOGY,
                DevicePrivileges.DEVICES_ACTIONS_DEVICE_COMMANDS,
                DevicePrivileges.DEVICES_ACTIONS_SECURITY_SETTINGS,
                DevicePrivileges.DEVICES_ACTIONS_PROTOCOL_DIALECTS,
                DevicePrivileges.DEVICES_ACTIONS_GENERAL_ATTRIBUTES,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TASKS,
                DevicePrivileges.DEVICES_ACTIONS_CONNECTION_METHODS,
                DevicePrivileges.DEVICES_ACTIONS_DATA_EDIT,
                DevicePrivileges.DEVICES_ACTIONS_FIRMWARE_MANAGEMENT,
                DevicePrivileges.DEVICES_PAGES_COMMUNICATION_PLANNING
        );
    }

    @Test
    public void testPrivilegesForInDecommissionedState(){
        State state = mock(State.class);
        when(state.getName()).thenReturn(DefaultState.DECOMMISSIONED.getKey());
        Device device = mock(Device.class);
        when(device.getState()).thenReturn(state);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));

        String response = target("/devices/1/privileges").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        List<String> privileges = model.<List<String>>get("$.privileges[*].name");
        assertThat(privileges).isEmpty();
    }

    @Test
    public void testPrivilegesForCustomState(){
        State state = mock(State.class);
        when(state.getName()).thenReturn("Custom state");
        Device device = mock(Device.class);
        when(device.getState()).thenReturn(state);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));

        String response = target("/devices/1/privileges").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(20);
        List<String> privileges = model.<List<String>>get("$.privileges[*].name");
        assertThat(privileges).contains(
                DevicePrivileges.DEVICES_WIDGET_ISSUES,
                DevicePrivileges.DEVICES_WIDGET_VALIDATION,
                DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TOPOLOGY,
                DevicePrivileges.DEVICES_WIDGET_CONNECTION,
                DevicePrivileges.DEVICES_WIDGET_COMMUNICATION_TASKS,
                DevicePrivileges.DEVICES_ACTIONS_VALIDATION,
                DevicePrivileges.DEVICES_ACTIONS_ESTIMATION,
                DevicePrivileges.DEVICES_ACTIONS_VALIDATION_RULE_SETS,
                DevicePrivileges.DEVICES_ACTIONS_ESTIMATION_RULE_SETS,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_PLANNING,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TOPOLOGY,
                DevicePrivileges.DEVICES_ACTIONS_DEVICE_COMMANDS,
                DevicePrivileges.DEVICES_ACTIONS_SECURITY_SETTINGS,
                DevicePrivileges.DEVICES_ACTIONS_PROTOCOL_DIALECTS,
                DevicePrivileges.DEVICES_ACTIONS_GENERAL_ATTRIBUTES,
                DevicePrivileges.DEVICES_ACTIONS_COMMUNICATION_TASKS,
                DevicePrivileges.DEVICES_ACTIONS_CONNECTION_METHODS,
                DevicePrivileges.DEVICES_ACTIONS_DATA_EDIT,
                DevicePrivileges.DEVICES_ACTIONS_FIRMWARE_MANAGEMENT,
                DevicePrivileges.DEVICES_PAGES_COMMUNICATION_PLANNING
        );
    }
}