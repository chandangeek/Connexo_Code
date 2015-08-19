package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComTaskExecutionWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.scheduling.model.ComSchedule;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the ComTaskExecutionImpl component
 * <p>
 * Copyrights EnergyICT
 * Date: 14/04/14
 * Time: 14:40
 */
public class ComTaskExecutionImplTest extends AbstractComTaskExecutionImplTest {

    @Test
    @Transactional
    public void createAdHocWithoutViolationsTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");

        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution).isNotNull();
        assertThat(comTaskExecution.getNextExecutionSpecs().isPresent()).isFalse();
        assertThat(comTaskExecution.getDevice().getId()).isEqualTo(device.getId());
        assertThat(comTaskExecution.getConnectionTask()).isNull();
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isTrue();
        assertThat(comTaskExecution.getExecutingComPort()).isNull();
        assertThat(comTaskExecution.getCurrentTryCount()).isEqualTo(1);
        assertThat(comTaskExecution.getExecutionStartedTimestamp()).isNull();
        assertThat(comTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(comTaskExecution.getNextExecutionTimestamp()).isNull();
        assertThat(comTaskExecution.getMaxNumberOfTries()).isEqualTo(maxNrOfTries);
        assertThat(comTaskExecution.getObsoleteDate()).isNull();
        assertThat(comTaskExecution.getPlannedPriority()).isEqualTo(comTaskEnablementPriority);
        assertThat(comTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(protocolDialectConfigurationProperties.getId());
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createWithComSchedule", "createWithComSchedule");
        ComTaskExecutionBuilder<ScheduledComTaskExecution> comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ScheduledComTaskExecution reloadedComTaskExecution = this.reloadScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().isPresent()).isTrue();
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().get().getId()).isEqualTo(comTaskExecution.getNextExecutionSpecs().get().getId());
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
        assertThat(reloadedComTaskExecution.usesSharedSchedule()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK_SCHEDULING + "}")
    public void scheduleTwice() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule1 = createComSchedule("ComSchedule1", comTaskEnablement.getComTask());
        ComSchedule comSchedule2 = createComSchedule("ComSchedule2", comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createWithComSchedule", "createWithComSchedule");
        ComTaskExecutionBuilder<ScheduledComTaskExecution> builder1 = device.newScheduledComTaskExecution(comSchedule1);
        builder1.add();
        ComTaskExecutionBuilder<ScheduledComTaskExecution> builder2 = device.newScheduledComTaskExecution(comSchedule2);
        builder2.add();

        // Business method
        device.save();

        // Asserts: see expected ExpectedConstraintViolation
    }

    @Test
    @Transactional
    public void comTaskExecutionDeletedWhenDeviceDeletedTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeletionTest", "DeletionTest");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
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
        ComTaskEnablement comTaskEnablement2 = enableComTask(true, this.protocolDialectConfigurationProperties, COM_TASK_NAME + "2");
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement1.getComTask());
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        ComTaskExecutionBuilder<ScheduledComTaskExecution> scheduledComTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ScheduledComTaskExecution scheduledComTaskExecution = scheduledComTaskExecutionBuilder.add();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement2, myTemporalExpression);
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = manuallyScheduledComTaskExecutionBuilder.add();
        device.save();
        device.removeComTaskExecution(scheduledComTaskExecution);
        device.removeComTaskExecution(manuallyScheduledComTaskExecution);

        // Business method
        device.save();

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test(expected = CannotDeleteComTaskExecutionWhichIsNotFromThisDevice.class)
    @Transactional
    public void removeComTaskWhichIsNotFromDeviceXTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device deviceWithoutComTaskExecutions = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithoutComTaskExecutions", "DeviceWithoutComTaskExecutions");
        Device deviceWithComTaskExecutions = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "DeviceWithComTaskExecutions", "DeviceWithComTaskExecutions");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = deviceWithComTaskExecutions.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        deviceWithComTaskExecutions.save();

        // Business method
        deviceWithoutComTaskExecutions.removeComTaskExecution(comTaskExecution);

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnBuilderTest() {
        boolean originalDefaultValue = false;
        boolean testUseDefault = !originalDefaultValue;
        ComTaskEnablement comTaskEnablement = enableComTask(originalDefaultValue);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);

        // Business method
        comTaskExecutionBuilder.useDefaultConnectionTask(testUseDefault);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnUpdaterTest() {
        boolean originalDefaultValue = false;
        boolean testUseDefault = !originalDefaultValue;
        ComTaskEnablement comTaskEnablement = enableComTask(originalDefaultValue);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.connectionTask(createASAPConnectionStandardTask(device));
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.useDefaultConnectionTask(testUseDefault);

        // Business method
        comTaskExecutionUpdater.update();
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void runNowWithMinimizeConnectionsTest() {

        Instant frozenClock = freezeClock(2014, Calendar.AUGUST, 28, 1, 11, 23, 0);
        Instant plannedNextExecutionTimeStamp = createFixedTimeStamp(2014, Calendar.AUGUST, 29, 0, 0, 0, 0);

        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        device.save();

        ScheduledConnectionTaskImpl connectionTask = createMinimizeOneDayConnectionStandardTask(device);
        ComTaskExecution comTaskExecution = device.newScheduledComTaskExecution(comSchedule).connectionTask(connectionTask).add();
        device.save();

        assertThat(comTaskExecution.getNextExecutionTimestamp()).isEqualTo(plannedNextExecutionTimeStamp);
        assertThat(connectionTask.getNextExecutionTimestamp()).isEqualTo(plannedNextExecutionTimeStamp);

        comTaskExecution.runNow();

        ComTaskExecution reloadedComTaskExecution = inMemoryPersistence.getCommunicationTaskService().findComTaskExecution(comTaskExecution.getId()).get();
        ScheduledConnectionTask reloadedScheduledConnectionTask = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(connectionTask.getId()).get();

        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(frozenClock);
        assertThat(reloadedScheduledConnectionTask.getNextExecutionTimestamp()).isEqualTo(frozenClock);
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderSetsUseDefaultToFalseTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.usesDefaultConnectionTask()).isFalse();
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterSetsUseDefaultToFalseTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isFalse();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT + "}")
    public void setNotToUseDefaultAndNoConnectionTaskSetTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.useDefaultConnectionTask(false);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
    }

    @Test
    @Transactional
    public void setPriorityOnBuilderTest() {
        int myPriority = 514;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "PriorityTester", "PriorityTester");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.priority(myPriority);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnBuilderTest() {
        int myPriority = -123;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.priority(myPriority);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void setPriorityOutOfRangeTest() {
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY + 1;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.priority(myPriority);
        comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts: see ExpectedConstraintViolation rule
    }

    @Test
    @Transactional
    public void setPriorityOnUpdaterTest() {
        int myPriority = 231;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "PriorityUpdater", "PriorityUpdater");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnUpdaterTest() {
        int myPriority = -7859;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);
        comTaskExecutionUpdater.update();
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnBuilderTest() {
        boolean ignoreOnInbound = true;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.ignoreNextExecutionSpecForInbound(ignoreOnInbound);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isEqualTo(ignoreOnInbound);
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnUpdaterTest() {
        boolean ignoreOnInbound = true;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.ignoreNextExecutionSpecForInbound(ignoreOnInbound);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isEqualTo(ignoreOnInbound);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{field.required}", property = "protocolDialectConfigurationProperties")
    @Transactional
    public void setNullProtocolDialectTest() {
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = enableComTask(true, null, COM_TASK_NAME);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Dialect", "Dialect");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void setProtocolDialectOnUpdaterTest() {
        ProtocolDialectConfigurationProperties otherDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new OtherComTaskExecutionDialect());
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Dialect", "Dialect");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.protocolDialectConfigurationProperties(otherDialect);

        // Business method
        comTaskExecutionUpdater.update();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(otherDialect.getId());
    }

    @Test
    @Transactional
    public void makeSuccessfulObsoleteTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithMyNextExecSpec", "WithMyNextExecSpec");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test(expected = ComTaskExecutionIsAlreadyObsoleteException.class)
    @Transactional
    public void makeObsoleteTwiceTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        comTaskExecution.makeObsolete();

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenInUseTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        inMemoryPersistence.update("update " + TableSpecs.DDC_COMTASKEXEC.name() + " set comport = " + outboundComPort.getId() + " where id = " + comTaskExecution.getId());

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenConnectionTaskHasComServerFilledInTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void isScheduledTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        device.save();
        ComTaskExecutionBuilder<ScheduledComTaskExecution> comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ScheduledComTaskExecution reloadedComTaskExecution = this.reloadScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesSharedSchedule()).isTrue();
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
    }


    @Test
    @Transactional
    public void isExecutingByComPortTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Making the comPort filled in
        ((ServerComTaskExecution) comTaskExecution).executionStarted(outboundComPort);

        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);

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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        inMemoryPersistence.update("update " + TableSpecs.DDC_COMTASKEXEC.name() + " set " + ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName() + " = 1 where id = " + comTaskExecution.getId());
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);

        // Business method
        boolean isExecuting = reloadedComTaskExecution.isExecuting();

        // Asserts
        assertThat(isExecuting).isTrue();
    }

    @Test
    @Transactional
    public void isNotExecutionBecauseComServeIsFilledInOnConnectionTaskButNextIsNotPassedYetTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        long future = inMemoryPersistence.getClock().instant().toEpochMilli() + 1_000_000_000_000L;  // let's just hope it won't take that long until this test is finished
        inMemoryPersistence.update("update " + TableSpecs.DDC_COMTASKEXEC.name() + " set " + ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName() + " = " + future + " where id = " + comTaskExecution.getId());
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);

        // Business method
        boolean isExecuting = reloadedComTaskExecution.isExecuting();

        // Asserts
        assertThat(isExecuting).isFalse();
    }

    @Test
    @Transactional
    public void notifyConnectionTaskRemovedTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        ((ComTaskExecutionImpl) comTaskExecution).connectionTaskRemoved();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK_SCHEDULING + "}", strict = false)
    public void duplicateComTaskOnDeviceTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Duplicate", "Duplicate");
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Duplicate", "Duplicate");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder1 = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution1 = comTaskExecutionBuilder1.add();
        device.save();
        device.removeComTaskExecution(comTaskExecution1);

        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder2 = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution2 = comTaskExecutionBuilder2.add();

        // Business method
        device.save();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution2);
        assertThat(device.getComTaskExecutions()).hasSize(1);
        assertThat(reloadedComTaskExecution.getId()).isEqualTo(comTaskExecution2.getId());
    }

    @Test
    @Transactional
    public void isObsoleteTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "IsObsolete", "IsObsolete");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder1 = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution1 = comTaskExecutionBuilder1.add();
        device.save();
        device.removeComTaskExecution(comTaskExecution1);

        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder2 = device.newAdHocComTaskExecution(comTaskEnablement);
        comTaskExecutionBuilder2.add();

        // Business method
        device.save();

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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "UpdateNextExecSpec", "UpdateNextExecSpec");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        comTaskExecution.updateNextExecutionTimestamp();

        ManuallyScheduledComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void checkCorrectTimeStampsForScheduledComTaskExecutionWithAsapConnectionTaskTest() {
        freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Instant fixedTimeStamp = createFixedTimeStamp(2014, 4, 5, 0, 0, 0, 0);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        device.save();
        ComTaskExecutionBuilder<ScheduledComTaskExecution> comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ScheduledComTaskExecution reloadedComTaskExecution = this.reloadScheduledComTaskExecution(device, comTaskExecution);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createMinimizeOneDayConnectionStandardTask(device);
        ComTaskExecutionBuilder<ScheduledComTaskExecution> comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ScheduledComTaskExecution reloadedComTaskExecution = this.reloadScheduledComTaskExecution(device, comTaskExecution);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "PutOnHold", "PutOnHold");
        device.save();
        ComTaskExecutionBuilder<ScheduledComTaskExecution> comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ScheduledComTaskExecution reloadedComTaskExecution = this.reloadScheduledComTaskExecution(device, comTaskExecution);

        // Business method
        reloadedComTaskExecution.putOnHold();

        // Asserts
        ScheduledComTaskExecution onHoldComTaskExecution = this.reloadScheduledComTaskExecution(device, comTaskExecution);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        device.save();
        ComTaskExecutionBuilder<ScheduledComTaskExecution> comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);

        // Business methods
        ScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        ScheduledComTaskExecution reloadedComTaskExecution = this.reloadScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastSuccessfulCompletionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void lockComTaskExecutionTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "LockTest", "LockTest");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        OutboundComPort outboundComPort = createOutboundComPort();

        // Business method
        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getCommunicationTaskService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(lockComTaskExecution).isNotNull();
        assertThat(reloadedComTaskExecution.isExecuting()).isTrue();
        assertThat(reloadedComTaskExecution.getExecutingComPort().getId()).isEqualTo(outboundComPort.getId());
    }

    @Test
    @Transactional
    public void cantLockTwiceTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "LockTest", "LockTest");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        OutboundComPort outboundComPort = createOutboundComPort();
        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getCommunicationTaskService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);
        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);

        // Business method
        ComTaskExecution notLockedComTaskExecution = inMemoryPersistence.getCommunicationTaskService().attemptLockComTaskExecution(reloadedComTaskExecution, outboundComPort);

        // Asserts
        assertThat(notLockedComTaskExecution).isNull();
    }

    @Test
    @Transactional
    public void unlockComTaskExecutionTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "UnlockTest", "UnlockTest");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        OutboundComPort outboundComPort = createOutboundComPort();

        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getCommunicationTaskService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);
        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);

        // Business method
        inMemoryPersistence.getCommunicationTaskService().unlockComTaskExecution(reloadedComTaskExecution);

        // Asserts
        ComTaskExecution notLockedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(notLockedComTaskExecution.isExecuting()).isFalse();
        assertThat(notLockedComTaskExecution.getExecutingComPort()).isNull();
    }

}