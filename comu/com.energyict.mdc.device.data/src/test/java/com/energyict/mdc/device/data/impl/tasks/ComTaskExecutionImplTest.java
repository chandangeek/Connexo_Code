/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.CannotSetMultipleComSchedulesWithSameComTask;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionTrigger;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.scheduling.model.ComSchedule;

import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComTaskExecutionImplTest extends AbstractComTaskExecutionImplTest {

    @Test
    @Transactional
    public void createAdHocWithoutViolationsTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        assertThat(comTaskExecution).isNotNull();
        assertThat(comTaskExecution.getNextExecutionSpecs().isPresent()).isFalse();
        assertThat(comTaskExecution.getDevice().getId()).isEqualTo(device.getId());
        assertThat(comTaskExecution.getConnectionTask()).isEmpty();
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(comTaskExecution.getExecutingComPort()).isNull();
        assertThat(comTaskExecution.getCurrentTryCount()).isEqualTo(1);
        assertThat(comTaskExecution.getExecutionStartedTimestamp()).isNull();
        assertThat(comTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isNull();
        assertThat(comTaskExecution.getMaxNumberOfTries()).isEqualTo(maxNrOfTries);
        assertThat(comTaskExecution.getObsoleteDate()).isNull();
        assertThat(comTaskExecution.getPlannedPriority()).isEqualTo(comTaskEnablementPriority);
        assertThat(comTaskExecution.isAdHoc()).isTrue();
        assertThat(comTaskExecution.usesSharedSchedule()).isFalse();
        assertThat(comTaskExecution.isExecuting()).isFalse();
        assertThat(comTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isFalse();
    }

    @Test
    @Transactional
    public void createScheduledWithoutViolations() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createWithComSchedule", "createWithComSchedule", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().isPresent()).isTrue();
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().get().getId()).isEqualTo(comTaskExecution.getNextExecutionSpecs().get().getId());
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
        assertThat(reloadedComTaskExecution.usesSharedSchedule()).isTrue();
    }

    @Test
    @Transactional
    @Expected(CannotSetMultipleComSchedulesWithSameComTask.class)
    public void scheduleTwice() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule1 = createComSchedule("ComSchedule1", comTaskEnablement.getComTask());
        ComSchedule comSchedule2 = createComSchedule("ComSchedule2", comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "createWithComSchedule", "createWithComSchedule", Instant.now());
        ComTaskExecutionBuilder builder1 = device.newScheduledComTaskExecution(comSchedule1);
        builder1.add();
        ComTaskExecutionBuilder builder2 = device.newScheduledComTaskExecution(comSchedule2);
        builder2.add();
        device.save();
        // Asserts: see expected ExpectedConstraintViolation
    }

    @Test
    @Transactional
    public void comTaskExecutionDeletedWhenDeviceDeletedTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeletionTest", "DeletionTest", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        long comTaskExecId = reloadedComTaskExecution.getId();

        // Business method
        device.delete();

        // Asserts
        assertThat(inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecId).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void removeComTasksTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement1 = enableComTask(true);
        ComTaskEnablement comTaskEnablement2 = enableComTask(true, COM_TASK_NAME + "2");
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement1.getComTask());
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        ComTaskExecutionBuilder scheduledComTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ComTaskExecution scheduledComTaskExecution = scheduledComTaskExecutionBuilder.add();
        ComTaskExecutionBuilder manuallyScheduledComTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement2, myTemporalExpression);
        ComTaskExecution manuallyScheduledComTaskExecution = manuallyScheduledComTaskExecutionBuilder.add();

        device.removeComTaskExecution(scheduledComTaskExecution);
        device.removeComTaskExecution(manuallyScheduledComTaskExecution);

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test
    @Transactional
    public void reschedulePreviouslyRemovedComTaskTest() {

        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        ComTaskExecutionBuilder scheduledComTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ComTaskExecution scheduledComTaskExecution = scheduledComTaskExecutionBuilder.add();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).hasSize(1);
        reloadedDevice.removeComTaskExecution(scheduledComTaskExecution);
        reloadedDevice.save();
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();

        reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
        reloadedDevice.newScheduledComTaskExecution(comSchedule).add();

        // Asserts
        assertThat(reloadedDevice.getComTaskExecutions()).hasSize(1);
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnBuilderTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(false);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "BuilderTest", "BuilderTest", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);

        // Business method
        comTaskExecutionBuilder.useDefaultConnectionTask(true);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isTrue();
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnUpdaterTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(false);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.connectionTask(createASAPConnectionStandardTask(device));
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.useDefaultConnectionTask(true);

        // Business method
        comTaskExecutionUpdater.update();
        // device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isTrue();
    }

    @Test
    @Transactional
    public void runNowWithMinimizeConnectionsTest() {

        Instant frozenClock = freezeClock(2014, Calendar.AUGUST, 28, 1, 11, 23, 0);
        Instant plannedNextExecutionTimeStamp = createFixedTimeStamp(2014, Calendar.AUGUST, 29, 0, 0, 0, 0);

        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", frozenClock);

        ScheduledConnectionTaskImpl connectionTask = createMinimizeOneDayConnectionStandardTask(device);
        ComTaskExecution comTaskExecution = device.newScheduledComTaskExecution(comSchedule).connectionTask(connectionTask).add();

        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(plannedNextExecutionTimeStamp);

        comTaskExecution.runNow();

        ComTaskExecution reloadedComTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        reloadedComTaskExecution.runNow();
        reloadedComTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        ScheduledConnectionTask reloadedScheduledConnectionTask = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(frozenClock);
        assertThat(reloadedScheduledConnectionTask.getNextExecutionTimestamp()).isEqualTo(frozenClock);
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderTest() {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "BuilderTest", "BuilderTest", Instant.now());

        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        assertThat(comTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderSetsUseDefaultToFalseTest() {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "BuilderTest", "BuilderTest", Instant.now());
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isFalse();
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterTest() {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "BuilderTest", "BuilderTest", Instant.now());

        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterSetsUseDefaultToFalseTest() {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "BuilderTest", "BuilderTest", Instant.now());

        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.getComTaskExecutionUpdater(comTaskExecution).connectionTask(connectionTask).update();

        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isFalse();
    }

    @Test
    @Transactional
    public void setPriorityOnBuilderTest() {
        int myPriority = 514;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "PriorityTester", "PriorityTester", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.priority(myPriority);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnBuilderTest() {
        int myPriority = -123;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.priority(myPriority);

        // Business method
        comTaskExecutionBuilder.add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void setPriorityOutOfRangeTest() {
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY + 1;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.priority(myPriority);
        comTaskExecutionBuilder.add();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void setPriorityOnUpdaterTest() {
        int myPriority = 231;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "PriorityUpdater", "PriorityUpdater", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnUpdaterTest() {
        int myPriority = -7859;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);
        comTaskExecutionUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void updatePriorityOutOfRangeTest() {
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY + 1;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);
        comTaskExecutionUpdater.update();
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnBuilderTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.ignoreNextExecutionSpecForInbound(true);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isTrue();
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnUpdaterTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithValidationError", "WithValidationError", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.ignoreNextExecutionSpecForInbound(true);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isTrue();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{field.required}", property = "protocolDialectConfigurationProperties")
    @Transactional
    public void setNullProtocolDialectTest() {
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = enableComTask(true, COM_TASK_NAME);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Dialect", "Dialect", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.add();

        // Asserts: see expected constraint violation rule
    }

//    @Test
//    @Transactional
//    public void setProtocolDialectOnUpdaterTest() {
//        ProtocolDialectConfigurationProperties otherDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new OtherPartialConnectionTaskDialect());
//        deviceConfiguration.save();
//        ComTaskEnablement comTaskEnablement = enableComTask(true);
//        Device device = inMemoryPersistence.getDeviceService()
//                .newDevice(deviceConfiguration, "Dialect", "Dialect", Instant.now());
//
//        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
//        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
//
//        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
//        comTaskExecutionUpdater.protocolDialectConfigurationProperties(otherDialect);
//
//        // Business method
//        comTaskExecutionUpdater.update();
//
//        // Asserts
//        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
//        assertThat(reloadedComTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(otherDialect.getId());
//    }

    @Test
    @Transactional
    public void makeSuccessfulObsoleteTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithMyNextExecSpec", "WithMyNextExecSpec", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.removeComTaskExecution(comTaskExecution);

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test(expected = ComTaskExecutionIsAlreadyObsoleteException.class)
    @Transactional
    public void makeObsoleteTwiceTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        device.removeComTaskExecution(comTaskExecution);

        // Business method
        device.removeComTaskExecution(comTaskExecution);

        // Asserts: see expected exception rule
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenInUseTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
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
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest", Instant.now());

        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
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
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesSharedSchedule()).isTrue();
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
    }


    @Test
    @Transactional
    public void isExecutingByComPortTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Making the comPort filled in
        ((ServerComTaskExecution) comTaskExecution).executionStarted(outboundComPort);

        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);

        // Business method
        boolean isExecuting = reloadedComTaskExecution.isExecuting();

        // Asserts
        assertThat(isExecuting).isTrue();
    }

    @Test
    @Transactional
    public void isExecutingByComServerOnConnectionTaskTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        inMemoryPersistence.update("update " + TableSpecs.DDC_COMTASKEXEC.name() + " set " + ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName() + " = 1 where id = " + comTaskExecution.getId());
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);

        // Business method
        boolean isExecuting = reloadedComTaskExecution.isExecuting();

        // Asserts
        assertThat(isExecuting).isTrue();
    }

    @Test
    @Transactional
    public void isExecutingBecauseInboundComTasksMarkedAsAlwaysExecuteForInboundTest() {
        InboundComPort inboundComPort = createInboundComPort();
        ComServer comServer = inboundComPort.getComServer();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        device.save();
        comTaskEnablement.setIgnoreNextExecutionSpecsForInbound(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, null);
        InboundConnectionTaskImpl connectionTask = createInboundConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Note: for this test, ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP field should remain null
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);

        // Business method
        boolean isExecuting = reloadedComTaskExecution.isExecuting();

        // Asserts
        assertThat(isExecuting).isTrue();
    }

    @Test
    @Transactional
    public void isNotExecutingBecauseOutboundComTaskMarkedAsAlwaysExecuteForInboundTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        device.save();
        comTaskEnablement.setIgnoreNextExecutionSpecsForInbound(true);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Note: for this test, ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP field should remain null
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);

        // Business method
        boolean isExecuting = reloadedComTaskExecution.isExecuting();

        // Asserts
        assertThat(isExecuting).isFalse();
    }

    @Test
    @Transactional
    public void isNotExecutingBecauseInboundComTasksNotMarkedAsAlwaysExecuteForInboundTest() {
        InboundComPort inboundComPort = createInboundComPort();
        ComServer comServer = inboundComPort.getComServer();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        device.save();
        comTaskEnablement.setIgnoreNextExecutionSpecsForInbound(false);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, null);
        InboundConnectionTaskImpl connectionTask = createInboundConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Note: for this test, ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP field should remain null
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);

        // Business method
        boolean isExecuting = reloadedComTaskExecution.isExecuting();

        // Asserts
        assertThat(isExecuting).isFalse();
    }

    @Test
    @Transactional
    public void isNotExecutionBecauseComServeIsFilledInOnConnectionTaskButNextIsNotPassedYetTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        long future = inMemoryPersistence.getClock().instant().toEpochMilli() + 1_000_000_000_000L;  // let's just hope it won't take that long until this test is finished
        inMemoryPersistence.update("update " + TableSpecs.DDC_COMTASKEXEC.name() + " set " + ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName() + " = " + future + " where id = " + comTaskExecution
                .getId());
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);

        // Business method
        boolean isExecuting = reloadedComTaskExecution.isExecuting();

        // Asserts
        assertThat(isExecuting).isFalse();
    }

    @Test
    @Transactional
    public void notifyConnectionTaskRemovedTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        ((ComTaskExecutionImpl) comTaskExecution).connectionTaskRemoved();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getConnectionTask()).isEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK + "}", strict = false)
    public void duplicateComTaskOnDeviceTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Duplicate", "Duplicate", Instant.now());
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder1 = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder1.add();
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder2 = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder2.add();

        // Business method
        device.save();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void duplicateComTaskOnDeviceAfterRemoveTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Duplicate", "Duplicate", Instant.now());
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder1 = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution1 = comTaskExecutionBuilder1.add();
        device.save();
        device.removeComTaskExecution(comTaskExecution1);

        ComTaskExecutionBuilder comTaskExecutionBuilder2 = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution2 = comTaskExecutionBuilder2.add();

        // Business method
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution2);
        assertThat(device.getComTaskExecutions()).hasSize(1);
        assertThat(reloadedComTaskExecution.getId()).isEqualTo(comTaskExecution2.getId());
    }

    @Test
    @Transactional
    public void isObsoleteTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "IsObsolete", "IsObsolete", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder1 = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution1 = comTaskExecutionBuilder1.add();

        device.removeComTaskExecution(comTaskExecution1);

        ComTaskExecutionBuilder comTaskExecutionBuilder2 = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder2.add();

        // Asserts
        List<ComTaskExecution> allComTaskExecutions = inMemoryPersistence.getCommunicationTaskService().findAllComTaskExecutionsIncludingObsoleteForDevice(device);
        assertThat(allComTaskExecutions).hasSize(2);
        assertThat(allComTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution value) {
                return value.isObsolete();
            }
        });
    }

    @Test
    @Transactional
    public void updateNextExecutionTimeStampOnAdHocTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "UpdateNextExecSpec", "UpdateNextExecSpec", Instant.now());
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        comTaskExecution.updateNextExecutionTimestamp();

        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void checkCorrectTimeStampsForScheduledComTaskExecutionWithAsapConnectionTaskTest() {
        freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Instant fixedTimeStamp = createFixedTimeStamp(2014, 4, 5, 0, 0, 0, 0);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "TimeChecks", "TimeChecks", fixedTimeStamp);

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

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
    public void checkCorrectTimeStampsForScheduledComTaskExecutionWithMinimizeConnectionTaskTest() {
        freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Instant nextFromComSchedule = createFixedTimeStamp(2014, 4, 4, 11, 0, 0, 0);
        Instant nextFromConnectionTask = createFixedTimeStamp(2014, 4, 5, 0, 0, 0, 0, TimeZone.getTimeZone("UTC"));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask(), new TemporalExpression(TimeDuration.hours(1)));
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "TimeChecks", "TimeChecks", nextFromComSchedule);

        ScheduledConnectionTaskImpl connectionTask = createMinimizeOneDayConnectionStandardTask(device);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

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
    public void putScheduledOnHoldTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "PutOnHold", "PutOnHold", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

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
    public void scheduledNextExecutionSpecWithOffsetTest() {
        freezeClock(2014, 4, 4, 10, 12, 32, 123);
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.days(1), TimeDuration.hours(3));
        Instant fixedTimeStamp = createFixedTimeStamp(2014, 4, 5, 3, 0, 0, 0, TimeZone.getTimeZone("UTC"));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask(), temporalExpression);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "TimeChecks", "TimeChecks", fixedTimeStamp);

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);

        // Business methods
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastSuccessfulCompletionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void lockComTaskExecutionTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "LockTest", "LockTest", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        OutboundComPort outboundComPort = createOutboundComPort();

        // Business method
        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getCommunicationTaskService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(lockComTaskExecution).isNotNull();
        assertThat(reloadedComTaskExecution.isExecuting()).isTrue();
        assertThat(reloadedComTaskExecution.getExecutingComPort().getId()).isEqualTo(outboundComPort.getId());
    }

    @Test
    @Transactional
    public void cantLockTwiceTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "LockTest", "LockTest", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        OutboundComPort outboundComPort = createOutboundComPort();
        inMemoryPersistence.getCommunicationTaskService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);

        // Business method
        ComTaskExecution notLockedComTaskExecution = inMemoryPersistence.getCommunicationTaskService().attemptLockComTaskExecution(reloadedComTaskExecution, outboundComPort);

        // Asserts
        assertThat(notLockedComTaskExecution).isNull();
    }

    @Test
    @Transactional
    public void unlockComTaskExecutionTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "UnlockTest", "UnlockTest", Instant.now());

        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        OutboundComPort outboundComPort = createOutboundComPort();

        inMemoryPersistence.getCommunicationTaskService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);
        ComTaskExecution reloadedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);

        // Business method
        inMemoryPersistence.getCommunicationTaskService().unlockComTaskExecution(reloadedComTaskExecution);

        // Asserts
        ComTaskExecution notLockedComTaskExecution = reloadComTaskExecution(device, comTaskExecution);
        assertThat(notLockedComTaskExecution.isExecuting()).isFalse();
        assertThat(notLockedComTaskExecution.getExecutingComPort()).isNull();
    }

    @Test
    @Transactional
    public void addNewComTaskExecutionTriggerTest() {
        Instant triggerTimeStamp_1 = createFixedTimeStamp(2016, 6, 1, 20, 0, 0, 0);
        Instant triggerTimeStamp_2 = createFixedTimeStamp(2016, 6, 1, 8, 0, 0, 0);

        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.days(1));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        comTaskExecution.addNewComTaskExecutionTrigger(triggerTimeStamp_1);
        comTaskExecution.addNewComTaskExecutionTrigger(triggerTimeStamp_2);

        // Asserts
        assertThat(comTaskExecution.getComTaskExecutionTriggers().size()).isEqualTo(2);
        assertThat(comTaskExecution.getComTaskExecutionTriggers().stream().map(ComTaskExecutionTrigger::getComTaskExecution).distinct().collect(Collectors.toList())).contains(comTaskExecution);
        assertThat(comTaskExecution.getComTaskExecutionTriggers()
                .stream()
                .map(ComTaskExecutionTrigger::getTriggerTimeStamp)
                .collect(Collectors.toList())).contains(triggerTimeStamp_1, triggerTimeStamp_2);
    }

    @Test
    @Transactional
    public void addDuplicateComTaskExecutionTriggerTest() {
        Instant triggerTimeStamp = createFixedTimeStamp(2016, 6, 1, 20, 0, 0, 0);

        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.days(1));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", Instant.now());
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        comTaskExecution.addNewComTaskExecutionTrigger(triggerTimeStamp);
        comTaskExecution.addNewComTaskExecutionTrigger(triggerTimeStamp);

        // Asserts
        assertThat(comTaskExecution.getComTaskExecutionTriggers().size()).isEqualTo(1);
        assertThat(comTaskExecution.getComTaskExecutionTriggers().get(0).getComTaskExecution()).isEqualTo(comTaskExecution);
        assertThat(comTaskExecution.getComTaskExecutionTriggers().get(0).getTriggerTimeStamp()).isEqualTo(triggerTimeStamp);
    }

    @Test
    @Transactional
    public void recalculateNextAndPlannedExecutionTimestampWithEarlierTriggerTest() {
        freezeClock(2016, 6, 1, 10, 5, 0, 0);
        Instant earlierTriggerTimeStamp = createFixedTimeStamp(2016, 6, 1, 20, 0, 0, 0);
        Instant laterTriggerTimeStamp = createFixedTimeStamp(2016, 6, 10, 0, 0, 0, 0);
        Instant nextExecutionAccordingToSchedule = createFixedTimeStamp(2016, 6, 2, 0, 0, 0, 0);

        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.days(1));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", earlierTriggerTimeStamp);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method & asserts
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(nextExecutionAccordingToSchedule);
        comTaskExecution.addNewComTaskExecutionTrigger(earlierTriggerTimeStamp);
        comTaskExecution.addNewComTaskExecutionTrigger(laterTriggerTimeStamp);
        ((ComTaskExecutionImpl) comTaskExecution).recalculateNextAndPlannedExecutionTimestamp();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(earlierTriggerTimeStamp);
    }

    @Test
    @Transactional
    public void recalculateNextAndPlannedExecutionTimestampWithTriggerNoNextExecutionSpecsTest() {
        freezeClock(2016, 6, 1, 10, 5, 0, 0);
        Instant earlierTriggerTimeStamp = createFixedTimeStamp(2016, 6, 1, 20, 0, 0, 0);
        Instant laterTriggerTimeStamp = createFixedTimeStamp(2016, 6, 10, 0, 0, 0, 0);

        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", earlierTriggerTimeStamp);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, null);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method & asserts
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(null);
        comTaskExecution.addNewComTaskExecutionTrigger(earlierTriggerTimeStamp);
        comTaskExecution.addNewComTaskExecutionTrigger(laterTriggerTimeStamp);
        ((ComTaskExecutionImpl) comTaskExecution).recalculateNextAndPlannedExecutionTimestamp();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(earlierTriggerTimeStamp);
    }

    @Test
    @Transactional
    public void recalculateNextAndPlannedExecutionTimestampWithoutEarlierTriggerTest() {
        freezeClock(2016, 6, 1, 10, 5, 0, 0);
        Instant triggerTimeStamp = createFixedTimeStamp(2016, 6, 1, 0, 0, 0, 0);
        Instant nextExecutionAccordingToSchedule = createFixedTimeStamp(2016, 6, 2, 0, 0, 0, 0);

        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.days(1));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations", triggerTimeStamp);
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method & asserts
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(nextExecutionAccordingToSchedule);
        comTaskExecution.addNewComTaskExecutionTrigger(triggerTimeStamp);
        ((ComTaskExecutionImpl) comTaskExecution).recalculateNextAndPlannedExecutionTimestamp();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(triggerTimeStamp);
    }
}