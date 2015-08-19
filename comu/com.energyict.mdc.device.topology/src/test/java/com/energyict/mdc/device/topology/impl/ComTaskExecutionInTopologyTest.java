package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the aspects of the ComTaskExecution component
 * that relate to or rely on the device topology feature.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-10 (10:40)
 */
public class ComTaskExecutionInTopologyTest extends AbstractComTaskExecutionInTopologyTest {

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenDefaultConnectionTaskHasComServerFilledInTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        device.save();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.useDefaultConnectionTask(true);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);
        inMemoryPersistence.update("update " + com.energyict.mdc.device.data.impl.TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + "where id = " + connectionTask.getId());

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void setUseDefaultConnectionTaskClearsConnectionTaskTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setUseDefaultConnectionTaskClearsConnectionTaskTest", "setUseDefaultConnectionTaskClearsConnectionTaskTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        comTaskExecutionBuilder.useDefaultConnectionTask(true);    // this call should clear the connectionTask
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(comTaskExecution.getConnectionTask()).isEmpty();
    }

    @Test
    @Transactional
    public void setUseDefaultOnUpdaterClearsConnectionTaskTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.useDefaultConnectionTask(false);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.useDefaultConnectionTask(true);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask()).isEmpty();
    }

    @Test
    @Transactional
    public void clearingDefaultFlagOnConnectionTaskClearsTheConnectionTaskOnTheComTaskExecutionTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "dcf", "dcf");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        inMemoryPersistence.getConnectionTaskService().setDefaultConnectionTask(connectionTask);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.useDefaultConnectionTask(true);
        comTaskExecutionBuilder.add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        ComTaskExecution reloadedComTaskExecution = reloadedDevice.getComTaskExecutions().get(0);
        assertThat(reloadedComTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());

        inMemoryPersistence.getConnectionTaskService().clearDefaultConnectionTask(device);
        Device finalReloadedDevice = getReloadedDevice(device);
        ComTaskExecution comTaskExecutionWithoutAConnectionTask = finalReloadedDevice.getComTaskExecutions().get(0);

        assertThat(comTaskExecutionWithoutAConnectionTask.getConnectionTask()).isEmpty();
    }

}