package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.tests.Matcher;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceComTaskResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final long DEVICE_CONFIGURATION_ID = 58L;
    private static final String DEVICE_MRID = "mrid";

    private static final long OK_VERSION = 1L;
    private static final long BAD_VERSION = 5L;

    @Mock
    private DeviceConfiguration deviceConfiguration;

    @Mock
    private Device device;

    @Override
    protected void setupTranslations() {
        super.setupTranslations();
        Stream.of(CompletionCodeTranslationKeys.values()).forEach(this::mockTranslation);
        Stream.of(ComSessionSuccessIndicatorTranslationKeys.values()).forEach(this::mockTranslation);
        Stream.of(DefaultTranslationKey.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(TranslationKey translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(deviceConfigurationService.findDeviceConfiguration(DEVICE_CONFIGURATION_ID)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(DEVICE_CONFIGURATION_ID, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));

        when(deviceService.findByUniqueMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceBymRIDAndVersion(eq(DEVICE_MRID), longThat(Matcher.matches(v->v!=OK_VERSION)))).thenReturn(Optional.empty());
        when(deviceService.findAndLockDeviceBymRIDAndVersion(DEVICE_MRID, OK_VERSION)).thenReturn(Optional.of(device));

        when(device.getmRID()).thenReturn(DEVICE_MRID);
        when(device.getVersion()).thenReturn(OK_VERSION);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        when(device.getComTaskExecutions()).thenReturn(comTaskExecutions);

        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIGURATION_ID);
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        List<ComTaskEnablement> comTaskEnablements = new ArrayList<>();
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(comTaskEnablements);
    }

    public DeviceInfo getDeviceInfo(){
        DeviceInfo info = new DeviceInfo();
        info.mRID = DEVICE_MRID;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(DEVICE_CONFIGURATION_ID, OK_VERSION);
        return info;
    }

    @Test
    public void testGetAllcomTaskExecutionsOneEnablementOneExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        ComTask comTask = mockUserComTask(111L);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskEnablement.getComTask()).thenReturn(comTask);

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(mock(SecurityPropertySet.class));

        Map<String, Object> response = target("/devices/1/comtasks").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("comTasks");
        List<Map<String, Object>> comTasks = (List<Map<String, Object>>) response.get("comTasks");
        assertThat(comTasks).hasSize(1);
    }

    @Test
    public void testGetAllcomTaskExecutionsTwoEnablementsOneExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        ComTask comTask1 = mockUserComTask(111L);
        ComTask comTask2 = mockUserComTask(222L);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask1));
        when(comTaskEnablement1.getComTask()).thenReturn(comTask1);
        when(comTaskEnablement2.getComTask()).thenReturn(comTask2);

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1, comTaskEnablement2));

        when(comTaskEnablement1.getSecurityPropertySet()).thenReturn(mock(SecurityPropertySet.class));
        when(comTaskEnablement2.getSecurityPropertySet()).thenReturn(mock(SecurityPropertySet.class));

        when(comTaskEnablement2.getPartialConnectionTask()).thenReturn(Optional.of(mock(PartialConnectionTask.class)));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getDeviceProtocolDialect()).thenReturn(mock(DeviceProtocolDialect.class));
        when(comTaskEnablement2.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);


        Map<String, Object> response = target("/devices/1/comtasks").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("comTasks");
        List<Map<String, Object>> comTasks = (List<Map<String, Object>>) response.get("comTasks");
        assertThat(comTasks).hasSize(2);
    }

    @Test
    public void testRunComTaskFromEnablement() throws Exception {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        mockComTask(comTaskEnablement, 111L);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement)).thenReturn(comTaskExecutionBuilder);
        when(comTaskExecutionBuilder.scheduleNow()).thenReturn(comTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution);

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecutionBuilder, times(1)).scheduleNow();
        verify(comTaskExecutionBuilder, times(1)).add();
    }

    @Test
    public void testRunComTaskFromEnablementIncompleteInfo() throws Exception {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));

        mockComTask(comTaskEnablement, 111L);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement)).thenReturn(comTaskExecutionBuilder);
        when(comTaskExecutionBuilder.scheduleNow()).thenReturn(comTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution);

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testRunNowComTaskFromEnablement() throws Exception {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));

        mockComTask(comTaskEnablement, 111L);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement)).thenReturn(comTaskExecutionBuilder);
        when(comTaskExecutionBuilder.runNow()).thenReturn(comTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution);

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/runnow").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecutionBuilder, times(1)).runNow();
        verify(comTaskExecutionBuilder, times(1)).add();
    }

    @Test
    public void testRunNowComTaskFromEnablementWithIncompleteInfo() throws Exception {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));

        mockComTask(comTaskEnablement, 111L);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement)).thenReturn(comTaskExecutionBuilder);
        when(comTaskExecutionBuilder.runNow()).thenReturn(comTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution);

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/runnow").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testRunComTaskFromExecution() throws Exception {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        deviceConfiguration.getComTaskEnablements().add(comTaskEnablement);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));

        device.getComTaskExecutions().add(comTaskExecution);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution, times(1)).scheduleNow();
    }

    @Test
    public void testRunComTaskFromExecutionWithIncompleteInfo() throws Exception {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        deviceConfiguration.getComTaskEnablements().add(comTaskEnablement);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));

        device.getComTaskExecutions().add(comTaskExecution);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testRunNowComTaskFromExecution() throws Exception {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/runnow").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution, times(1)).runNow();
    }


    @Test
    public void testRunNowComTaskFromExecutionBadVersion() throws Exception {
        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        info.device.version = BAD_VERSION;

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));

        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/runnow").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testChangeUrgencyFromManualExecution() throws Exception {
        ComTaskUrgencyInfo comTaskUrgencyInfo = new ComTaskUrgencyInfo();
        comTaskUrgencyInfo.urgency = 50;
        comTaskUrgencyInfo.device = getDeviceInfo();

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        deviceConfiguration.getComTaskEnablements().add(comTaskEnablement);

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));

        device.getComTaskExecutions().add(comTaskExecution);

        ManuallyScheduledComTaskExecutionUpdater manuallyScheduledComTaskExecutionUpdater = mock(ManuallyScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(manuallyScheduledComTaskExecutionUpdater);
        when(manuallyScheduledComTaskExecutionUpdater.priority(comTaskUrgencyInfo.urgency)).thenReturn(manuallyScheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);


        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/urgency").request().put(Entity.json(comTaskUrgencyInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(manuallyScheduledComTaskExecutionUpdater, times(1)).priority(comTaskUrgencyInfo.urgency);
        verify(manuallyScheduledComTaskExecutionUpdater, times(1)).update();
    }

    @Test
    public void testChangeUrgencyFromManualExecutionBadVersion() throws Exception {
        ComTaskUrgencyInfo comTaskUrgencyInfo = new ComTaskUrgencyInfo();
        comTaskUrgencyInfo.urgency = 50;
        comTaskUrgencyInfo.device = getDeviceInfo();
        comTaskUrgencyInfo.device.version = BAD_VERSION;

        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/urgency").request().put(Entity.json(comTaskUrgencyInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testChangeUrgencyFromSharedExecution() throws Exception {
        ComTaskUrgencyInfo comTaskUrgencyInfo = new ComTaskUrgencyInfo();
        comTaskUrgencyInfo.urgency = 50;
        comTaskUrgencyInfo.device = getDeviceInfo();

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        deviceConfiguration.getComTaskEnablements().add(comTaskEnablement);

        ScheduledComTaskExecution comTaskExecution = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));

        device.getComTaskExecutions().add(comTaskExecution);

        ScheduledComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(scheduledComTaskExecutionUpdater);
        when(scheduledComTaskExecutionUpdater.priority(comTaskUrgencyInfo.urgency)).thenReturn(scheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);


        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/urgency").request().put(Entity.json(comTaskUrgencyInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater, times(1)).priority(comTaskUrgencyInfo.urgency);
        verify(scheduledComTaskExecutionUpdater, times(1)).update();
    }

    private ComTask mockComTask(ComTaskEnablement comTaskEnablement, long comTaskId) {
        ComTask comTask = mockUserComTask(comTaskId);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(taskService.findComTask(comTaskId)).thenReturn(Optional.of(comTask));
        return comTask;
    }

    @Test
    public void testChangeConnectionMethodFromManualExecutionWithExistingConnectionMethodOnDevice() throws Exception {
        ComTaskConnectionMethodInfo comTaskConnectionMethodInfo = new ComTaskConnectionMethodInfo();
        comTaskConnectionMethodInfo.connectionMethod = "connectionMethod";
        comTaskConnectionMethodInfo.device = getDeviceInfo();

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getName()).thenReturn("connectionMethod");
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));


        ManuallyScheduledComTaskExecutionUpdater manuallyScheduledComTaskExecutionUpdater = mock(ManuallyScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(manuallyScheduledComTaskExecutionUpdater);
        when(manuallyScheduledComTaskExecutionUpdater.connectionTask(connectionTask)).thenReturn(manuallyScheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);


        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/connectionmethod").request().put(Entity.json(comTaskConnectionMethodInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(manuallyScheduledComTaskExecutionUpdater, times(1)).connectionTask(connectionTask);
        verify(manuallyScheduledComTaskExecutionUpdater, times(1)).update();
    }


    @Test
    public void testChangeConnectionMethodFromSharedExecutionWithExistingConnectionMethodOnDevice() throws Exception {
        ComTaskConnectionMethodInfo comTaskConnectionMethodInfo = new ComTaskConnectionMethodInfo();
        comTaskConnectionMethodInfo.connectionMethod = "connectionMethod";
        comTaskConnectionMethodInfo.device = getDeviceInfo();

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ScheduledComTaskExecution comTaskExecution = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getName()).thenReturn("connectionMethod");
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));


        ScheduledComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(scheduledComTaskExecutionUpdater);
        when(scheduledComTaskExecutionUpdater.connectionTask(connectionTask)).thenReturn(scheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);


        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/connectionmethod").request().put(Entity.json(comTaskConnectionMethodInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater, times(1)).connectionTask(connectionTask);
        verify(scheduledComTaskExecutionUpdater, times(1)).update();
    }

    @Test
    public void testChangeConnectionMethodFromManualExecutionWithNoExistingConnectionMethodOnDevice() throws Exception {
        ComTaskConnectionMethodInfo comTaskConnectionMethodInfo = new ComTaskConnectionMethodInfo();
        comTaskConnectionMethodInfo.connectionMethod = "connectionMethod";
        comTaskConnectionMethodInfo.device = getDeviceInfo();

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));


        ManuallyScheduledComTaskExecutionUpdater manuallyScheduledComTaskExecutionUpdater = mock(ManuallyScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(manuallyScheduledComTaskExecutionUpdater);
        when(manuallyScheduledComTaskExecutionUpdater.useDefaultConnectionTask(true)).thenReturn(manuallyScheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);


        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/connectionmethod").request().put(Entity.json(comTaskConnectionMethodInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(manuallyScheduledComTaskExecutionUpdater, times(1)).useDefaultConnectionTask(true);
        verify(manuallyScheduledComTaskExecutionUpdater, times(1)).update();
    }


    @Test
    public void testChangeConnectionMethodFromSharedExecutionWithNoExistingConnectionMethodOnDevice() throws Exception {
        ComTaskConnectionMethodInfo comTaskConnectionMethodInfo = new ComTaskConnectionMethodInfo();
        comTaskConnectionMethodInfo.connectionMethod = "connectionMethod";
        comTaskConnectionMethodInfo.device = getDeviceInfo();

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ScheduledComTaskExecution comTaskExecution = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));


        ScheduledComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(scheduledComTaskExecutionUpdater);
        when(scheduledComTaskExecutionUpdater.useDefaultConnectionTask(true)).thenReturn(scheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);


        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/connectionmethod").request().put(Entity.json(comTaskConnectionMethodInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater, times(1)).useDefaultConnectionTask(true);
        verify(scheduledComTaskExecutionUpdater, times(1)).update();
    }

    @Test
    public void testChangeProtocolDialectForMananualExecution() throws Exception {
        ComTaskProtocolDialectInfo comTaskProtocolDialectInfo = new ComTaskProtocolDialectInfo();
        comTaskProtocolDialectInfo.protocolDialect = "protocolDialect";
        comTaskProtocolDialectInfo.device = getDeviceInfo();

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ProtocolDialectConfigurationProperties dialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(deviceConfiguration.getProtocolDialectConfigurationPropertiesList()).thenReturn(Arrays.asList(dialectConfigurationProperties));
        DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
        when(deviceProtocolDialect.getDeviceProtocolDialectDisplayName()).thenReturn("protocolDialect");
        when(dialectConfigurationProperties.getDeviceProtocolDialect()).thenReturn(deviceProtocolDialect);

        ComTask comTask = mockComTask(comTaskEnablement, 111L);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ManuallyScheduledComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ManuallyScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(scheduledComTaskExecutionUpdater);
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        when(scheduledComTaskExecutionUpdater.protocolDialectConfigurationProperties(dialectConfigurationProperties)).thenReturn(scheduledComTaskExecutionUpdater);


        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/111/protocoldialect").request().put(Entity.json(comTaskProtocolDialectInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater, times(1)).protocolDialectConfigurationProperties(dialectConfigurationProperties);
        verify(scheduledComTaskExecutionUpdater, times(1)).update();
    }

    @Test
    public void testGetDeviceComTaskHistory() throws Exception {
        Device device = mock(Device.class);
        when(device.getName()).thenReturn("device name");
        when(device.getmRID()).thenReturn("X9");
        when(deviceService.findByUniqueMrid("X9")).thenReturn(Optional.of(device));

        ComTask comTask = mockUserComTask(19L);
        when(comTask.getName()).thenReturn("Read all");
        when(taskService.findComTask(19)).thenReturn(Optional.of(comTask));

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getId()).thenReturn(10L);
        when(deviceConfiguration.getName()).thenReturn("device config");
        when(deviceConfiguration.getComTaskEnablementFor(comTask)).thenReturn(Optional.of(comTaskEnablement));

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(11L);
        when(deviceType.getName()).thenReturn("device type");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);

        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("com schedule");

        ScheduledComTaskExecution comTaskExecution1 = mock(ScheduledComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));
        when(comTaskExecution1.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution1.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution1.getExecutionPriority()).thenReturn(-20);

        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.ConnectionTypeDirection.OUTBOUND);

        ConnectionTypePluggableClass pluggeableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggeableClass.getName()).thenReturn("GPRS");

        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggeableClass);

        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(13L);
        when(connectionTask.getName()).thenReturn("connection task");
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.getCurrentTryCount()).thenReturn(7);
        when(connectionTask.getDevice()).thenReturn(device);

        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(15L);
        when(comServer.getName()).thenReturn("com server");

        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn("com port");
        when(comPort.getComServer()).thenReturn(comServer);
        ComSession comSession = mock(ComSession.class);
        when(comSession.getConnectionTask()).thenReturn(connectionTask);
        when(comSession.getId()).thenReturn(14L);
        Instant sessionStartTime = LocalDateTime.of(2014, 10, 20, 14, 6, 2).toInstant(ZoneOffset.UTC);
        when(comSession.getStartDate()).thenReturn(sessionStartTime);
        Instant sessionFinishTime = LocalDateTime.of(2014, 10, 20, 14, 6, 32).toInstant(ZoneOffset.UTC);
        when(comSession.getStopDate()).thenReturn(sessionFinishTime);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Broken);
        when(comSession.getNumberOfFailedTasks()).thenReturn(1001);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(1002);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(1003);
        when(comSession.getComPort()).thenReturn(comPort);
        when(comSession.getConnectionTask()).thenReturn(connectionTask);

        ComTaskExecutionSession comTaskExecutionSession1 = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession1.getComTask()).thenReturn(comTask);
        when(comTaskExecutionSession1.getComTaskExecution()).thenReturn(comTaskExecution1);
        when(comTaskExecutionSession1.getDevice()).thenReturn(device);
        when(comTaskExecutionSession1.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.ConnectionError);
        when(comTaskExecutionSession1.getComSession()).thenReturn(comSession);
        Instant execSessionStartTime = LocalDateTime.of(2014, 10, 20, 14, 6, 4).toInstant(ZoneOffset.UTC);
        when(comTaskExecutionSession1.getStartDate()).thenReturn(execSessionStartTime);
        Instant execSessionStopTime = LocalDateTime.of(2014, 10, 20, 14, 6, 30).toInstant(ZoneOffset.UTC);
        when(comTaskExecutionSession1.getStopDate()).thenReturn(execSessionStopTime);
        Finder<ComTaskExecutionSession> finder = mockFinder(Arrays.asList(comTaskExecutionSession1));
        when(communicationTaskService.findSessionsByComTaskExecutionAndComTask(comTaskExecution1, comTask)).thenReturn(finder);

        String response = target("/devices/X9/comtasks/19/comtaskexecutionsessions").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.comTaskExecutionSessions")).hasSize(1);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].name")).isEqualTo("Read all");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].device.id")).isEqualTo("X9");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].device.name")).isEqualTo("device name");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].deviceConfiguration.id")).isEqualTo(10);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].deviceConfiguration.name")).isEqualTo("device config");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].deviceConfiguration.deviceTypeId")).isEqualTo(11);
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].deviceType.id")).isEqualTo(11);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].deviceType.name")).isEqualTo("device type");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comScheduleName")).isEqualTo("com schedule");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].urgency")).isEqualTo(-20);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].result")).isEqualTo("Connection error");
        assertThat(jsonModel.<Long>get("$.comTaskExecutionSessions[0].startTime")).isEqualTo(execSessionStartTime.toEpochMilli());
        assertThat(jsonModel.<Long>get("$.comTaskExecutionSessions[0].finishTime")).isEqualTo(execSessionStopTime.toEpochMilli());
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].durationInSeconds")).isEqualTo(26);
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comSession.id")).isEqualTo(14);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comSession.connectionMethod.name")).isEqualTo("connection task");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comSession.connectionMethod.id")).isEqualTo(13);
        assertThat(jsonModel.<Long>get("$.comTaskExecutionSessions[0].comSession.startedOn")).isEqualTo(sessionStartTime.toEpochMilli());
        assertThat(jsonModel.<Long>get("$.comTaskExecutionSessions[0].comSession.finishedOn")).isEqualTo(sessionFinishTime.toEpochMilli());
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comSession.durationInSeconds")).isEqualTo(30);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comSession.direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comSession.connectionType")).isEqualTo("GPRS");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comSession.comServer.id")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comSession.comServer.name")).isEqualTo("com server");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comSession.comPort")).isEqualTo("com port");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comSession.status")).isEqualTo("Failed");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comSession.result.id")).isEqualTo("Broken");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comSession.result.displayValue")).isEqualTo("Broken");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comSession.result.retries")).isEqualTo(7);
        assertThat(jsonModel.<Boolean>get("$.comTaskExecutionSessions[0].comSession.isDefault")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comSession.comTaskCount.numberOfSuccessfulTasks")).isEqualTo(1003);
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comSession.comTaskCount.numberOfFailedTasks")).isEqualTo(1001);
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comSession.comTaskCount.numberOfIncompleteTasks")).isEqualTo(1002);
    }

    @Test
    public void canNotRunFirmwareComTaskTest() throws IOException {
        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/1/comtasks/" + firmwareComTaskId + "/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK.getKey());
    }

    @Test
    public void canNotRunNowOnFirmwareComTaskTest() throws IOException {
        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/1/comtasks/" + firmwareComTaskId + "/runnow").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK.getKey());
    }

    @Test
    public void canNotActivateOnFirmwareComTaskTest() throws IOException {
        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/1/comtasks/" + firmwareComTaskId + "/activate").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK.getKey());
    }

    @Test
    public void canNotDeactivateOnFirmwareComTaskTest() throws IOException {
        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/1/comtasks/" + firmwareComTaskId + "/deactivate").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK.getKey());
    }


    @Test
    public void canNotChangeFrequencyOnFirmwareComTaskTest() throws IOException {
        ComTaskFrequencyInfo comTaskFrequencyInfo = new ComTaskFrequencyInfo();
        comTaskFrequencyInfo.temporalExpression = new TemporalExpressionInfo();
        comTaskFrequencyInfo.device = getDeviceInfo();

        Response response = target("/devices/1/comtasks/" + firmwareComTaskId + "/frequency").request().put(Entity.json(comTaskFrequencyInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK.getKey());
    }

    private ComTask mockUserComTask(long comTaskId) {
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.isUserComTask()).thenReturn(Boolean.TRUE);
        when(comTask1.getId()).thenReturn(comTaskId);
        return comTask1;
    }

    @Test
    public void activateAllWithoutFirmwareComTaskTest() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.isOnHold()).thenReturn(Boolean.TRUE);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        ComTask comTask1 = mockUserComTask(111L);
        when(comTaskEnablement1.getComTask()).thenReturn(comTask1);
        ComTask comTask2 = mockUserComTask(222L);
        when(comTaskEnablement2.getComTask()).thenReturn(comTask2);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask1));
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(Matchers.<ComTaskEnablement>any())).thenReturn(manuallyScheduledComTaskExecutionComTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution newComTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(manuallyScheduledComTaskExecutionComTaskExecutionBuilder.add()).thenReturn(newComTaskExecution);
        when(newComTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask2));
        when(newComTaskExecution.isOnHold()).thenReturn(Boolean.TRUE);

        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1, comTaskEnablement2));

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/activate").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).resume();
        verify(newComTaskExecution).resume();
        verify(device).newAdHocComTaskExecution(comTaskEnablement2);
    }

    @Test
    public void activateAllWithFirmwareComTaskTest() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.isOnHold()).thenReturn(Boolean.TRUE);
        ComTaskExecution firmwareComTaskExecution = mock(ComTaskExecution.class);
        when(firmwareComTaskExecution.isOnHold()).thenReturn(Boolean.TRUE);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        ComTaskEnablement comTaskEnablement3 = mock(ComTaskEnablement.class);
        when(comTaskEnablement3.getComTask()).thenReturn(firmwareComTask);
        ComTask comTask1 = mockUserComTask(111L);
        when(comTaskEnablement1.getComTask()).thenReturn(comTask1);
        ComTask comTask2 = mockUserComTask(222L);
        when(comTaskEnablement2.getComTask()).thenReturn(comTask2);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask1));
        when(firmwareComTaskExecution.getComTasks()).thenReturn(Collections.singletonList(firmwareComTask));
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(Matchers.<ComTaskEnablement>any())).thenReturn(manuallyScheduledComTaskExecutionComTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution newComTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(manuallyScheduledComTaskExecutionComTaskExecutionBuilder.add()).thenReturn(newComTaskExecution);
        when(newComTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask2));
        when(newComTaskExecution.isOnHold()).thenReturn(Boolean.TRUE);

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution, firmwareComTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1, comTaskEnablement2, comTaskEnablement3));

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/activate").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).resume();
        verify(newComTaskExecution).resume();
        verify(device).newAdHocComTaskExecution(comTaskEnablement2);
        verify(firmwareComTaskExecution, never()).resume();
    }
    @Test
    public void deActivateAllWithoutFirmwareComTaskTest() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.isOnHold()).thenReturn(Boolean.FALSE);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        ComTask comTask1 = mockUserComTask(111L);
        when(comTaskEnablement1.getComTask()).thenReturn(comTask1);
        ComTask comTask2 = mockUserComTask(222L);
        when(comTaskEnablement2.getComTask()).thenReturn(comTask2);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask1));
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(Matchers.<ComTaskEnablement>any())).thenReturn(manuallyScheduledComTaskExecutionComTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution newComTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(manuallyScheduledComTaskExecutionComTaskExecutionBuilder.add()).thenReturn(newComTaskExecution);
        when(newComTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask2));
        when(newComTaskExecution.isOnHold()).thenReturn(Boolean.FALSE);

        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1, comTaskEnablement2));

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/deactivate").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).putOnHold();
        verify(newComTaskExecution).putOnHold();
        verify(device).newAdHocComTaskExecution(comTaskEnablement2);
    }

    @Test
    public void deActivateAllWithFirmwareComTaskTest() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.isOnHold()).thenReturn(Boolean.FALSE);
        ComTaskExecution firmwareComTaskExecution = mock(ComTaskExecution.class);
        when(firmwareComTaskExecution.isOnHold()).thenReturn(Boolean.TRUE);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        ComTaskEnablement comTaskEnablement3 = mock(ComTaskEnablement.class);
        when(comTaskEnablement3.getComTask()).thenReturn(firmwareComTask);
        ComTask comTask1 = mockUserComTask(111L);
        when(comTaskEnablement1.getComTask()).thenReturn(comTask1);
        ComTask comTask2 = mockUserComTask(222L);
        when(comTaskEnablement2.getComTask()).thenReturn(comTask2);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask1));
        when(firmwareComTaskExecution.getComTasks()).thenReturn(Collections.singletonList(firmwareComTask));
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(Matchers.<ComTaskEnablement>any())).thenReturn(manuallyScheduledComTaskExecutionComTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution newComTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(manuallyScheduledComTaskExecutionComTaskExecutionBuilder.add()).thenReturn(newComTaskExecution);
        when(newComTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask2));
        when(newComTaskExecution.isOnHold()).thenReturn(Boolean.FALSE);

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution, firmwareComTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1, comTaskEnablement2, comTaskEnablement3));

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = getDeviceInfo();
        Response response = target("/devices/"+DEVICE_MRID+"/comtasks/deactivate").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).putOnHold();
        verify(newComTaskExecution).putOnHold();
        verify(device).newAdHocComTaskExecution(comTaskEnablement2);
        verify(firmwareComTaskExecution, never()).putOnHold();
    }
}
