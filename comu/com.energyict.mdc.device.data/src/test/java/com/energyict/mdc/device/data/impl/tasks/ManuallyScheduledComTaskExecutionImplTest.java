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
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.scheduling.model.ComSchedule;
import org.junit.Test;

import java.time.Instant;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ManuallyScheduledComTaskExecutionImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (13:44)
 */
public class ManuallyScheduledComTaskExecutionImplTest extends AbstractComTaskExecutionImplTest {

    @Test
    @Transactional
    public void createManuallyScheduledWithoutViolations() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createManuallyScheduled", "createManuallyScheduled");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder =
                device.newManuallyScheduledComTaskExecution(
                        comTaskEnablement,
                        new TemporalExpression(TimeDuration.days(1)));
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createManuallyScheduled", "createManuallyScheduled");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder =
                device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "NextExecSpecCreate", "NextExecSpecCreate");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, myTemporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionSpecs().isPresent()).isTrue();
        long nextExecutionSpecId = comTaskExecution.getNextExecutionSpecs().get().getId();
        assertThat(inMemoryPersistence.getSchedulingService().findNextExecutionSpecs(nextExecutionSpecId)).isNotNull();
    }

    @Test
    @Transactional
    public void adhocSchedulingDoesNotCreateNextExecSpecTest() {
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "NextExecSpecCreate", "NextExecSpecCreate");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.getNextExecutionSpecs().isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void nextExecSpecIsDeletedAfterComTaskExecutionDeletedTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "NextExecSpecDelete", "NextExecSpecDelete");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, myTemporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        long nextExecutionSpecId = comTaskExecution.getNextExecutionSpecs().get().getId();

        // Business method
        ((ComTaskExecutionImpl) comTaskExecution).delete();

        // Asserts
        assertThat(inMemoryPersistence.getSchedulingService().findNextExecutionSpecs(nextExecutionSpecId)).isNull();
    }

    @Test
    @Transactional
    public void removeComTaskTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, myTemporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        ComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        device.removeComTaskExecution(reloadedComTaskExecution);

        // Business method
        device.save();

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(false);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);

        // Business method
        comTaskExecutionBuilder.useDefaultConnectionTask(true);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        device.save();
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.connectionTask(createASAPConnectionStandardTask(device));
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.useDefaultConnectionTask(testUseDefault);

        // Business method
        comTaskExecutionUpdater.update();
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        assertThat(connectionTask.getNextExecutionTimestamp()).isNull();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesDefaultConnectionTask()).isFalse();
        assertThat(reloadedComTaskExecution.getConnectionTask().get().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT + "}")
    public void setNotToUseDefaultAndNoConnectionTaskSetTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.useDefaultConnectionTask(false);

        // Business methods
        comTaskExecutionBuilder.add();
        device.save();

        // Asserts: see expected contraint violation rule
    }

    @Test
    @Transactional
    public void setPriorityOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        int myPriority = 514;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "PriorityTester", "PriorityTester");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.priority(myPriority);

        // Business methods
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        int myPriority = -123;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "PriorityUpdater", "PriorityUpdater");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);

        // Business method
        comTaskExecutionUpdater.update();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        int myPriority = -7859;
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.ignoreNextExecutionSpecForInbound(true);

        // Business methods
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isTrue();
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.ignoreNextExecutionSpecForInbound(false);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.ignoreNextExecutionSpecForInbound(true);

        // Business method
        comTaskExecutionUpdater.update();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isTrue();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{field.required}", property = "protocolDialectConfigurationProperties")
    @Transactional
    public void setNullProtocolDialectTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true, null, COM_TASK_NAME);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Dialect", "Dialect");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Dialect", "Dialect");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithMyNextExecSpec", "WithMyNextExecSpec");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        OutboundComPort outboundComPort = createOutboundComPort();
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.usesSharedSchedule()).isFalse();
        assertThat(reloadedComTaskExecution.isScheduledManually()).isTrue();
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK_SCHEDULING + "}", strict = false)
    public void comTaskAlreadyScheduledViaComScheduleTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Duplicate", "Duplicate");
        ComTaskExecutionBuilder<ScheduledComTaskExecution> scheduledComTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        scheduledComTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder1 = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device, TimeDuration.minutes(5));
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createMinimizeOneDayConnectionStandardTask(device);
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
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
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "PutOnHold", "PutOnHold");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, new TemporalExpression(TimeDuration
                .days(1)));
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);

        // Business method
        reloadedComTaskExecution.putOnHold();

        // Asserts
        ManuallyScheduledComTaskExecution onHoldComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(onHoldComTaskExecution.isOnHold()).isTrue();
        assertThat(onHoldComTaskExecution.getPlannedNextExecutionTimestamp()).isNotNull();
    }

    @Test
    @Transactional
    public void manuallyScheduledNextExecutionSpecWithOffsetTest() {
        freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Instant fixedTimeStamp = createFixedTimeStamp(2014, 4, 5, 3, 0, 0, 0, TimeZone.getTimeZone("UTC"));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder =
                device.newManuallyScheduledComTaskExecution(
                        comTaskEnablement,
                        new TemporalExpression(
                                TimeDuration.days(1),
                                TimeDuration.hours(3)));

        // Business methods
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastSuccessfulCompletionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
    }

}