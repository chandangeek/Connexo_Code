package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.tasks.ComTask;
import com.google.common.base.Optional;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class DeviceComTaskResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testGetAllcomTaskExecutionsOneEnablementOneExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskEnablement.getComTask()).thenReturn(comTask);

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(mock(SecurityPropertySet.class));

        Map<String, Object> response = target("/devices/1/comtasks").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("comTasks");
        List<Map<String,Object>> comTasks = (List<Map<String, Object>>) response.get("comTasks");
        assertThat(comTasks).hasSize(1);
    }

    @Test
    public void testGetAllcomTaskExecutionsTwoEnablementsOneExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        ComTaskEnablement comTaskEnablement2 = mock(ComTaskEnablement.class);
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(111L);
        ComTask comTask2 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(222L);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask1));
        when(comTaskEnablement1.getComTask()).thenReturn(comTask1);
        when(comTaskEnablement2.getComTask()).thenReturn(comTask2);

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1,comTaskEnablement2));

        when(comTaskEnablement1.getSecurityPropertySet()).thenReturn(mock(SecurityPropertySet.class));
        when(comTaskEnablement2.getSecurityPropertySet()).thenReturn(mock(SecurityPropertySet.class));

        when(comTaskEnablement2.getPartialConnectionTask()).thenReturn(Optional.of(mock(PartialConnectionTask.class)));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getDeviceProtocolDialect()).thenReturn(mock(DeviceProtocolDialect.class));
        when(comTaskEnablement2.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));


        Map<String, Object> response = target("/devices/1/comtasks").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("comTasks");
        List<Map<String,Object>> comTasks = (List<Map<String, Object>>) response.get("comTasks");
        assertThat(comTasks).hasSize(2);
    }

    @Test
    public void testRunComTaskFromEnablement() throws Exception{
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ComTask comTask = mock(ComTask.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTask.getId()).thenReturn(111L);


        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement,protocolDialectConfigurationProperties)).thenReturn(comTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution);

        Response response = target("/devices/1/comtasks/111/run").request().put(Entity.json(""));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecutionBuilder, times(1)).add();
        verify(comTaskExecution,times(1)).scheduleNow();
    }

    @Test
    public void testRunNowComTaskFromEnablement() throws Exception{
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ComTask comTask = mock(ComTask.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTask.getId()).thenReturn(111L);


        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement,protocolDialectConfigurationProperties)).thenReturn(comTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution);

        Response response = target("/devices/1/comtasks/111/runnow").request().put(Entity.json(""));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecutionBuilder, times(1)).add();
        verify(comTaskExecution,times(1)).runNow();
    }

    @Test
    public void testRunComTaskFromExecution() throws Exception{
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));

        Response response = target("/devices/1/comtasks/111/run").request().put(Entity.json(""));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution,times(1)).scheduleNow();
    }

    @Test
    public void testRunNowComTaskFromExecution() throws Exception{
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));

        Response response = target("/devices/1/comtasks/111/runnow").request().put(Entity.json(""));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution,times(1)).runNow();
    }

    @Test
    public void testChangeUrgencyFromManualExecution() throws Exception{
        ComTaskUrgencyInfo comTaskUrgencyInfo = new ComTaskUrgencyInfo();
        comTaskUrgencyInfo.urgency = 50;

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ManuallyScheduledComTaskExecutionUpdater manuallyScheduledComTaskExecutionUpdater = mock(ManuallyScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(manuallyScheduledComTaskExecutionUpdater);
        when(manuallyScheduledComTaskExecutionUpdater.priority(comTaskUrgencyInfo.urgency)).thenReturn(manuallyScheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));


        Response response = target("/devices/1/comtasks/111/urgency").request().put(Entity.json(comTaskUrgencyInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(manuallyScheduledComTaskExecutionUpdater,times(1)).priority(comTaskUrgencyInfo.urgency);
        verify(manuallyScheduledComTaskExecutionUpdater,times(1)).update();
    }

    @Test
    public void testChangeUrgencyFromSharedExecution() throws Exception{
        ComTaskUrgencyInfo comTaskUrgencyInfo = new ComTaskUrgencyInfo();
        comTaskUrgencyInfo.urgency = 50;

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ScheduledComTaskExecution comTaskExecution = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ScheduledComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(scheduledComTaskExecutionUpdater);
        when(scheduledComTaskExecutionUpdater.priority(comTaskUrgencyInfo.urgency)).thenReturn(scheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));


        Response response = target("/devices/1/comtasks/111/urgency").request().put(Entity.json(comTaskUrgencyInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater,times(1)).priority(comTaskUrgencyInfo.urgency);
        verify(scheduledComTaskExecutionUpdater,times(1)).update();
    }

    @Test
    public void testChangeConnectionMethodFromManualExecutionWithExistingConnectionMethodOnDevice() throws Exception{
        ComTaskConnectionMethodInfo comTaskConnectionMethodInfo = new ComTaskConnectionMethodInfo();
        comTaskConnectionMethodInfo.connectionMethod = "connectionMethod";

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getName()).thenReturn("connectionMethod");
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));


        ManuallyScheduledComTaskExecutionUpdater manuallyScheduledComTaskExecutionUpdater = mock(ManuallyScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(manuallyScheduledComTaskExecutionUpdater);
        when(manuallyScheduledComTaskExecutionUpdater.connectionTask(connectionTask)).thenReturn(manuallyScheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));


        Response response = target("/devices/1/comtasks/111/connectionmethod").request().put(Entity.json(comTaskConnectionMethodInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(manuallyScheduledComTaskExecutionUpdater,times(1)).connectionTask(connectionTask);
        verify(manuallyScheduledComTaskExecutionUpdater,times(1)).update();
    }


    @Test
    public void testChangeConnectionMethodFromSharedExecutionWithExistingConnectionMethodOnDevice() throws Exception{
        ComTaskConnectionMethodInfo comTaskConnectionMethodInfo = new ComTaskConnectionMethodInfo();
        comTaskConnectionMethodInfo.connectionMethod = "connectionMethod";

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ScheduledComTaskExecution comTaskExecution = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getName()).thenReturn("connectionMethod");
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));


        ScheduledComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(scheduledComTaskExecutionUpdater);
        when(scheduledComTaskExecutionUpdater.connectionTask(connectionTask)).thenReturn(scheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));


        Response response = target("/devices/1/comtasks/111/connectionmethod").request().put(Entity.json(comTaskConnectionMethodInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater,times(1)).connectionTask(connectionTask);
        verify(scheduledComTaskExecutionUpdater,times(1)).update();
    }

    @Test
    public void testChangeConnectionMethodFromManualExecutionWithNoExistingConnectionMethodOnDevice() throws Exception{
        ComTaskConnectionMethodInfo comTaskConnectionMethodInfo = new ComTaskConnectionMethodInfo();
        comTaskConnectionMethodInfo.connectionMethod = "connectionMethod";

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));


        ManuallyScheduledComTaskExecutionUpdater manuallyScheduledComTaskExecutionUpdater = mock(ManuallyScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(manuallyScheduledComTaskExecutionUpdater);
        when(manuallyScheduledComTaskExecutionUpdater.useDefaultConnectionTask(true)).thenReturn(manuallyScheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));


        Response response = target("/devices/1/comtasks/111/connectionmethod").request().put(Entity.json(comTaskConnectionMethodInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(manuallyScheduledComTaskExecutionUpdater,times(1)).useDefaultConnectionTask(true);
        verify(manuallyScheduledComTaskExecutionUpdater, times(1)).update();
    }


    @Test
    public void testChangeConnectionMethodFromSharedExecutionWithNoExistingConnectionMethodOnDevice() throws Exception{
        ComTaskConnectionMethodInfo comTaskConnectionMethodInfo = new ComTaskConnectionMethodInfo();
        comTaskConnectionMethodInfo.connectionMethod = "connectionMethod";

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ScheduledComTaskExecution comTaskExecution = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));


        ScheduledComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(scheduledComTaskExecutionUpdater);
        when(scheduledComTaskExecutionUpdater.useDefaultConnectionTask(true)).thenReturn(scheduledComTaskExecutionUpdater);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));


        Response response = target("/devices/1/comtasks/111/connectionmethod").request().put(Entity.json(comTaskConnectionMethodInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater,times(1)).useDefaultConnectionTask(true);
        verify(scheduledComTaskExecutionUpdater,times(1)).update();
    }

    @Test
    public void testChangeProtocolDialectForMananualExecution() throws Exception{
        ComTaskProtocolDialectInfo comTaskProtocolDialectInfo = new ComTaskProtocolDialectInfo();
        comTaskProtocolDialectInfo.protocolDialect = "protocolDialect";

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ProtocolDialectConfigurationProperties dialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(deviceConfiguration.getProtocolDialectConfigurationPropertiesList()).thenReturn(Arrays.asList(dialectConfigurationProperties));
        DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
        when(deviceProtocolDialect.getDisplayName()).thenReturn("protocolDialect");
        when(dialectConfigurationProperties.getDeviceProtocolDialect()).thenReturn(deviceProtocolDialect);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ManuallyScheduledComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ManuallyScheduledComTaskExecutionUpdater.class);
        when(device.getComTaskExecutionUpdater(comTaskExecution)).thenReturn(scheduledComTaskExecutionUpdater);
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));
        when(scheduledComTaskExecutionUpdater.protocolDialectConfigurationProperties(dialectConfigurationProperties)).thenReturn(scheduledComTaskExecutionUpdater);



        Response response = target("/devices/1/comtasks/111/protocoldialect").request().put(Entity.json(comTaskProtocolDialectInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater,times(1)).protocolDialectConfigurationProperties(dialectConfigurationProperties);
        verify(scheduledComTaskExecutionUpdater,times(1)).update();
    }


}
