package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.InMemoryIntegrationPersistence;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionBuilder;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import java.util.Date;
import java.util.TimeZone;

import org.junit.*;

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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "createManuallyScheduled", "createManuallyScheduled");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder =
                device.newManuallyScheduledComTaskExecution(
                        comTaskEnablement,
                        this.protocolDialectConfigurationProperties,
                        new TemporalExpression(TimeDuration.days(1)));
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().isPresent()).isTrue();
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().get().getId()).isEqualTo(comTaskExecution.getNextExecutionSpecs().get().getId());
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
    }

    @Test
    @Transactional
    public void manualSchedulingCreatesNextExecSpecTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "NextExecSpecCreate", "NextExecSpecCreate");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, myTemporalExpression);
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
    public void nextExecSpecIsDeletedAfterComTaskExecutionDeletedTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "NextExecSpecDelete", "NextExecSpecDelete");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, myTemporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, myTemporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);

        // Business method
        comTaskExecutionBuilder.useDefaultConnectionTask(true);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        assertThat(comTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(comTaskExecution.getConnectionTask()).isNull();
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        boolean originalDefaultValue = false;
        boolean testUseDefault = !originalDefaultValue;
        ComTaskEnablement comTaskEnablement = enableComTask(originalDefaultValue);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.useDefaultConnectionTask()).isFalse();
        assertThat(comTaskExecution.getConnectionTask().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setUseDefaultConnectionTaskClearsConnectionTaskTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(false);
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        comTaskExecutionBuilder.useDefaultConnectionTask(true);    // this call should clear the connectionTask
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(comTaskExecution.getConnectionTask()).isNull();
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isFalse();
        assertThat(reloadedComTaskExecution.getConnectionTask().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setUseDefaultOnUpdaterClearsConnectionTaskTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
        comTaskExecutionBuilder.useDefaultConnectionTask(false);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.useDefaultConnectionTask(true);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT + "}")
    public void setNotToUseDefaultAndNoConnectionTaskSetTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "PriorityTester", "PriorityTester");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "PriorityUpdater", "PriorityUpdater");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED + "}")
    @Transactional
    public void setNullProtocolDialectTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Dialect", "Dialect");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, null, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Dialect", "Dialect");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithMyNextExecSpec", "WithMyNextExecSpec");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenDefaultConnectionTaskHasComServerFilledInTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
        comTaskExecutionBuilder.useDefaultConnectionTask(true);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);
        inMemoryPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + "where id = " + connectionTask.getId());

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void isScheduledTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
        ManuallyScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ManuallyScheduledComTaskExecution reloadedComTaskExecution = this.reloadManuallyScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DUPLICATE_COMTASK_SCHEDULING + "}", strict = false)
    public void comTaskAlreadyScheduledViaComScheduleTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Duplicate", "Duplicate");
        ScheduledComTaskExecutionBuilder scheduledComTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        scheduledComTaskExecutionBuilder.add();
        device.save();

        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder1 = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Date fixedTimeStamp = createFixedTimeStamp(2014, 4, 4, 11, 0, 0, 0);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device, TimeDuration.minutes(5));
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Date nextFromComSchedule = createFixedTimeStamp(2014, 4, 4, 11, 0, 0, 0);
        Date nextFromConnectionTask = createFixedTimeStamp(2014, 4, 5, 0, 0, 0, 0);
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        ScheduledConnectionTaskImpl connectionTask = createMinimizeOneDayConnectionStandardTask(device);
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, temporalExpression);
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
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "PutOnHold", "PutOnHold");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, this.protocolDialectConfigurationProperties, new TemporalExpression(TimeDuration
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
        Date fixedTimeStamp = createFixedTimeStamp(2014, 4, 5, 3, 0, 0, 0, TimeZone.getTimeZone("UTC"));
        ComTaskEnablement comTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        ManuallyScheduledComTaskExecutionBuilder comTaskExecutionBuilder =
                device.newManuallyScheduledComTaskExecution(
                        comTaskEnablement,
                        this.protocolDialectConfigurationProperties,
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