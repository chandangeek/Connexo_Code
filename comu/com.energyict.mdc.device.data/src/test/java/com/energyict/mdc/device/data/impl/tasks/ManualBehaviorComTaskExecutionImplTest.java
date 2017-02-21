/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;

import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the ComTaskExecutionImpl component with the manual behavior.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (13:44)
 */
public class ManualBehaviorComTaskExecutionImplTest extends AbstractComTaskExecutionImplTest {

    @Test
    @Transactional
    public void createManuallyScheduledWithoutViolations() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createManuallyScheduled", "createManuallyScheduled", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder =
                device.newManuallyScheduledComTaskExecution(
                        comTaskEnablement,
                        new TemporalExpression(TimeDuration.days(1)));
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().isPresent()).isTrue();
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().get().getId()).isEqualTo(comTaskExecution.getNextExecutionSpecs().get().getId());
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
        assertThat(reloadedComTaskExecution.isScheduledManually()).isTrue();
        assertThat(reloadedComTaskExecution.usesSharedSchedule()).isFalse();
    }

    @Test
    @Transactional
    public void createAdhocScheduledWithoutViolations() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createManuallyScheduled", "createManuallyScheduled", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder =
                device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().isPresent()).isFalse();
        assertThat(reloadedComTaskExecution.isAdHoc()).isTrue();
        assertThat(reloadedComTaskExecution.isScheduledManually()).isTrue();
        assertThat(reloadedComTaskExecution.usesSharedSchedule()).isFalse();
    }

    @Test
    @Transactional
    public void manualSchedulingCreatesNextExecSpecTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "NextExecSpecCreate", "NextExecSpecCreate", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionSpecs().isPresent()).isTrue();
        long nextExecutionSpecId = comTaskExecution.getNextExecutionSpecs().get().getId();
        assertThat(inMemoryPersistence.getSchedulingService().findNextExecutionSpecs(nextExecutionSpecId)).isNotNull();
    }

    @Test
    @Transactional
    public void adhocSchedulingDoesNotCreateNextExecSpecTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "NextExecSpecCreate", "NextExecSpecCreate", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionSpecs().isPresent()).isFalse();
    }
/*
Irrelevant as delete is not supported any more
    @Test
    @Transactional
    public void nextExecSpecIsDeletedAfterComTaskExecutionDeletedTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "NextExecSpecDelete", "NextExecSpecDelete");
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        long nextExecutionSpecId = comTaskExecution.getNextExecutionSpecs().get().getId();

        // Business method
        ((ComTaskExecutionImpl) comTaskExecution).delete();

        // Asserts
        assertThat(inMemoryPersistence.getSchedulingService().findNextExecutionSpecs(nextExecutionSpecId)).isNull();
    }
*/
    @Test
    @Transactional
    public void removeComTaskTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        device.removeComTaskExecution(reloadedComTaskExecution);

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(false);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "BuilderTest", "BuilderTest", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);

        // Business method
        comTaskExecutionBuilder.useDefaultConnectionTask(true);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(comTaskExecution.getConnectionTask()).isEmpty();
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        boolean originalDefaultValue = false;
        boolean testUseDefault = !originalDefaultValue;
        ComTaskEnablement comTaskEnablement = enableComTask(originalDefaultValue);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.connectionTask(createASAPConnectionStandardTask(device));
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.useDefaultConnectionTask(testUseDefault);

        // Business method
        comTaskExecutionUpdater.update();
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "BuilderTest", "BuilderTest", Instant.now());
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        assertThat(connectionTask.getNextExecutionTimestamp()).isNull();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isFalse();
        assertThat(comTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(comTaskExecution.getNextExecutionTimestamp());
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "BuilderTest", "BuilderTest", Instant.now());
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isFalse();
        assertThat(reloadedComTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setPriorityOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        int myPriority = 514;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "PriorityTester", "PriorityTester", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.priority(myPriority);

        // Business methods
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        int myPriority = -123;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.priority(myPriority);

        // Business methods
        comTaskExecutionBuilder.add();
        device.save();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void setPriorityOutOfRangeTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY + 1;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.priority(myPriority);

        // Business methods
        comTaskExecutionBuilder.add();
        device.save();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void setPriorityOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        int myPriority = 231;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "PriorityUpdater", "PriorityUpdater", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);

        // Business method
        comTaskExecutionUpdater.update();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        int myPriority = -7859;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);

        // Business methods
        comTaskExecutionUpdater.update();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void updatePriorityOutOfRangeTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY + 1;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);

        // Business method
        comTaskExecutionUpdater.update();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.ignoreNextExecutionSpecForInbound(true);

        // Business methods
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isTrue();
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.ignoreNextExecutionSpecForInbound(false);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.ignoreNextExecutionSpecForInbound(true);

        // Business method
        comTaskExecutionUpdater.update();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isTrue();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{field.required}", property = "protocolDialectConfigurationProperties")
    @Transactional
    public void setNullProtocolDialectTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true, null, COM_TASK_NAME);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Dialect", "Dialect", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void setProtocolDialectOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ProtocolDialectConfigurationProperties otherDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new OtherComTaskExecutionDialect());
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Dialect", "Dialect", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.protocolDialectConfigurationProperties(otherDialect);

        // Business method
        comTaskExecutionUpdater.update();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(otherDialect.getId());
    }

    @Test
    @Transactional
    public void makeSuccessfulObsoleteTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithMyNextExecSpec", "WithMyNextExecSpec", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        device.removeComTaskExecution(comTaskExecution);

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test(expected = ComTaskExecutionIsAlreadyObsoleteException.class)
    @Transactional
    public void makeObsoleteTwiceTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        device.removeComTaskExecution(comTaskExecution);

        // Business method
        device.removeComTaskExecution(comTaskExecution);

        // Asserts: see expected exception rule
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenInUseTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        OutboundComPort outboundComPort = createOutboundComPort();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        inMemoryPersistence.update("update " + TableSpecs.DDC_COMTASKEXEC.name() + " set comport = " + outboundComPort.getId() + " where id = " + comTaskExecution.getId());

        // Business method
        device.removeComTaskExecution(comTaskExecution);

        // Asserts: see expected exception rule
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenConnectionTaskHasComServerFilledInTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest", Instant.now());
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());

        // Business method
        device.removeComTaskExecution(comTaskExecution);

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void isScheduledTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesSharedSchedule()).isFalse();
        assertThat(reloadedComTaskExecution.isScheduledManually()).isTrue();
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK + "}", strict = false)
    public void comTaskAlreadyScheduledViaComScheduleTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Duplicate", "Duplicate", Instant.now());
        ComTaskExecutionBuilder scheduledComTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        scheduledComTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionBuilder comTaskExecutionBuilder1 = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder1.add();

        // Business method
        device.save();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void checkCorrectTimeStampsWithAsapConnectionTaskTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Instant fixedTimeStamp = createFixedTimeStamp(2014, 4, 4, 11, 0, 0, 0);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "TimeChecks", "TimeChecks", fixedTimeStamp);
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device, TimeDuration.minutes(5));
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastSuccessfulCompletionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void checkCorrectTimeStampWithMinimizeConnectionTaskTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Instant nextFromComSchedule = createFixedTimeStamp(2014, 4, 4, 11, 0, 0, 0);
        Instant nextFromConnectionTask = createFixedTimeStamp(2014, 4, 5, 0, 0, 0, 0);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "TimeChecks", "TimeChecks", nextFromComSchedule);
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createMinimizeOneDayConnectionStandardTask(device);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(nextFromConnectionTask);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(nextFromComSchedule);
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastSuccessfulCompletionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void putOnHoldTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "PutOnHold", "PutOnHold", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, new TemporalExpression(TimeDuration
                .days(1)));
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);

        // Business method
        reloadedComTaskExecution.putOnHold();

        // Asserts
        ComTaskExecution onHoldComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(onHoldComTaskExecution.isOnHold()).isTrue();
        assertThat(onHoldComTaskExecution.getPlannedNextExecutionTimestamp()).isNotNull();
    }

    @Test
    @Transactional
    public void manuallyScheduledNextExecutionSpecWithOffsetTest() {
        freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Instant fixedTimeStamp = createFixedTimeStamp(2014, 4, 5, 3, 0, 0, 0, TimeZone.getTimeZone("UTC"));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "TimeChecks", "TimeChecks", fixedTimeStamp);
        ComTaskExecutionBuilder comTaskExecutionBuilder =
                device.newManuallyScheduledComTaskExecution(
                        comTaskEnablement,
                        new TemporalExpression(
                                TimeDuration.days(1),
                                TimeDuration.hours(3)));

        // Business methods
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastSuccessfulCompletionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void removeNextExecutionSpecsTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", inMemoryPersistence.getClock()
                        .instant());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(Instant.ofEpochMilli(temporalExpression.nextOccurrence(Calendar.getInstance(utcTimeZone)).getTime()));

        // Business method
        comTaskExecution.getUpdater().createNextExecutionSpecs(null).update();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isNull();
        assertThat(comTaskExecution.getPlannedNextExecutionTimestamp()).isNull();
        assertThat(comTaskExecution.getNextExecutionSpecs()).isEmpty();
    }

    @Test
    @Transactional
    public void updateNextExecutionSpecsTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.days(1));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", inMemoryPersistence.getClock()
                        .instant());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(Instant.ofEpochMilli(temporalExpression.nextOccurrence(Calendar.getInstance(utcTimeZone)).getTime()));

        // Business method
        temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        comTaskExecution.getUpdater().createNextExecutionSpecs(temporalExpression).update();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(Instant.ofEpochMilli(temporalExpression.nextOccurrence(Calendar.getInstance(utcTimeZone)).getTime()));
        assertThat(comTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(Instant.ofEpochMilli(temporalExpression.nextOccurrence(Calendar.getInstance(utcTimeZone)).getTime()));
        assertThat(comTaskExecution.getNextExecutionSpecs()).isPresent();
        NextExecutionSpecs nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs().get();
        assertThat(nextExecutionSpecs.getTemporalExpression()).isEqualTo(temporalExpression);
    }

    @Test
    @Transactional
    public void setNextExecutionSpecsTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", inMemoryPersistence.getClock()
                        .instant());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, null);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        assertThat(comTaskExecution.getNextExecutionTimestamp()).isNull();

        // Business method
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        comTaskExecution.getUpdater().createNextExecutionSpecs(temporalExpression).update();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(Instant.ofEpochMilli(temporalExpression.nextOccurrence(Calendar.getInstance(utcTimeZone)).getTime()));
        assertThat(comTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(Instant.ofEpochMilli(temporalExpression.nextOccurrence(Calendar.getInstance(utcTimeZone)).getTime()));
        assertThat(comTaskExecution.getNextExecutionSpecs()).isPresent();
        NextExecutionSpecs nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs().get();
        assertThat(nextExecutionSpecs.getTemporalExpression()).isEqualTo(temporalExpression);
    }

}