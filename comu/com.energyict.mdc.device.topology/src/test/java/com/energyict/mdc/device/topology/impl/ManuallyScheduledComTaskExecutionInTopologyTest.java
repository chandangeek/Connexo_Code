package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the aspects of the ManuallyScheduledComTaskExecution component
 * that relate to or rely on the device topology feature.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-10 (10:58)
 */
public class ManuallyScheduledComTaskExecutionInTopologyTest extends AbstractComTaskExecutionInTopologyTest {

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenDefaultConnectionTaskHasComServerFilledInTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setUseDefaultConnectionTaskClearsConnectionTaskTest", "setUseDefaultConnectionTaskClearsConnectionTaskTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(false);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "setUseDefaultOnUpdaterClearsConnectionTaskTest", "setUseDefaultOnUpdaterClearsConnectionTaskTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.useDefaultConnectionTask(false);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.useDefaultConnectionTask(true);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask()).isEmpty();
    }

}