package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActivateAllRecurringCommunicationTest {

    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceService deviceService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private ConnectionTaskService connectionTaskService;

    @Test
    public void executeSchedulesAllCommunicationTasks() {
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getNextExecutionTimestamp()).thenReturn(Instant.ofEpochMilli(123456789));
        comTaskExecution1.putOnHold();
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        when(comTaskExecution2.isOnHold()).thenReturn(true);
        ComTaskExecution comTaskExecution3 = mock(ComTaskExecution.class);
        when(comTaskExecution3.isOnHold()).thenReturn(false);

        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1, comTaskExecution2, comTaskExecution3));
        when(this.device.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.of(device));
        ActivateAllRecurringCommunications microAction = this.getTestInstance();
        microAction.execute(this.device, Instant.now(), Collections.emptyList());


        Assert.assertFalse(comTaskExecution1.isOnHold());
        Assert.assertTrue(comTaskExecution2.isOnHold());
        Assert.assertFalse(comTaskExecution3.isOnHold());
    }

    private ActivateAllRecurringCommunications getTestInstance() {
        return new ActivateAllRecurringCommunications(thesaurus, deviceService, this.communicationTaskService, this.connectionTaskService);
    }

}