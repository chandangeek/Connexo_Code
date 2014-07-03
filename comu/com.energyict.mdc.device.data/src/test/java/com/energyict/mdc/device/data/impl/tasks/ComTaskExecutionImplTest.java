package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComTaskExecutionWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.InMemoryIntegrationPersistence;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecution;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecutionUpdaterRename;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionBuilder;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.assertj.core.api.Condition;
import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the ComTaskExecutionImpl component
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/04/14
 * Time: 14:40
 */
public class ComTaskExecutionImplTest extends AbstractComTaskExecutionImplTest {

    @Test
    @Transactional
    public void createAdHocWithoutViolationsTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");

        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution).isNotNull();
        assertThat(comTaskExecution.getNextExecutionSpecs().isPresent()).isFalse();
        assertThat(comTaskExecution.getDevice().getId()).isEqualTo(device.getId());
        assertThat(comTaskExecution.getConnectionTask()).isNull();
        assertThat(comTaskExecution.useDefaultConnectionTask()).isTrue();
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
        assertThat(comTaskExecution.isScheduled()).isFalse();
        assertThat(comTaskExecution.isExecuting()).isFalse();
        assertThat(comTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isFalse();
    }

    @Test
    @Transactional
    public void createScheduledWithoutViolations() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComSchedule comSchedule = createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "createWithComSchedule", "createWithComSchedule");
        ScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ScheduledComTaskExecution reloadedComTaskExecution = this.reloadScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().isPresent()).isTrue();
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().get().getId()).isEqualTo(comTaskExecution.getNextExecutionSpecs().get().getId());
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
    }

    @Test
    @Transactional
    public void comTaskExecutionDeletedWhenDeviceDeletedTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeletionTest", "DeletionTest");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        long comTaskExecId = reloadedComTaskExecution.getId();

        // Business method
        device.delete();

        // Asserts
        assertThat(inMemoryPersistence.getDeviceDataService().findComTaskExecution(comTaskExecId)).isNull();
    }

    @Test
    @Transactional
    public void removeComTasksTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement1 = createMockedComTaskEnablement(true);
        when(comTaskEnablement1.getId()).thenReturn(1L);
        ComTaskEnablement comTaskEnablement2 = createMockedComTaskEnablement(true, this.protocolDialectConfigurationProperties, COM_TASK_NAME + "2");
        when(comTaskEnablement2.getId()).thenReturn(2L);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement1.getComTask());
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        AdHocComTaskExecutionBuilder adHocComTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement1, protocolDialectConfigurationProperties);
        AdHocComTaskExecution adHocComTaskExecution = adHocComTaskExecutionBuilder.add();
        ScheduledComTaskExecutionBuilder scheduledComTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ScheduledComTaskExecution scheduledComTaskExecution = scheduledComTaskExecutionBuilder.add();
        ManuallyScheduledComTaskExecutionBuilder manuallyScheduledComTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement2, protocolDialectConfigurationProperties, myTemporalExpression);
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = manuallyScheduledComTaskExecutionBuilder.add();
        device.save();
        device.removeComTaskExecution(adHocComTaskExecution);
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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device deviceWithoutComTaskExecutions = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithoutComTaskExecutions", "DeviceWithoutComTaskExecutions");
        Device deviceWithComTaskExecutions = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithComTaskExecutions", "DeviceWithComTaskExecutions");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = deviceWithComTaskExecutions.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(originalDefaultValue);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);

        // Business method
        comTaskExecutionBuilder.useDefaultConnectionTask(testUseDefault);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        assertThat(comTaskExecution.useDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnUpdaterTest() {
        boolean originalDefaultValue = false;
        boolean testUseDefault = !originalDefaultValue;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(originalDefaultValue);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.connectionTask(createASAPConnectionStandardTask(device));
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        AdHocComTaskExecutionUpdaterRename comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.useDefaultConnectionTask(testUseDefault);

        // Business method
        comTaskExecutionUpdater.update();
        device.save();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.getConnectionTask().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderSetsUseDefaultToFalseTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.useDefaultConnectionTask()).isFalse();
    }

    @Test
    @Transactional
    public void setUseDefaultConnectionTaskClearsConnectionTaskTest() {
        boolean useDefaultTrue = true;
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        comTaskExecutionBuilder.useDefaultConnectionTask(useDefaultTrue);    // this call should clear the connectionTask
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        assertThat(comTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(comTaskExecution.getConnectionTask()).isNull();
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        AdHocComTaskExecutionUpdaterRename comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getConnectionTask().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterSetsUseDefaultToFalseTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.connectionTask(connectionTask);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isFalse();
    }

    @Test
    @Transactional
    public void setUseDefaultOnUpdaterClearsConnectionTaskTest() {
        boolean useDefaultTrue = true;
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest", "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.useDefaultConnectionTask(false);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.useDefaultConnectionTask(useDefaultTrue);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT + "}")
    public void setNotToUseDefaultAndNoConnectionTaskSetTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.useDefaultConnectionTask(false);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
    }

    @Test
    @Transactional
    public void setPriorityOnBuilderTest() {
        int myPriority = 514;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "PriorityTester", "PriorityTester");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.priority(myPriority);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnBuilderTest() {
        int myPriority = -123;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.priority(myPriority);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    public void setPriorityOutOfRangeTest() {
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY + 1;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.priority(myPriority);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
    }

    @Test
    @Transactional
    public void setPriorityOnUpdaterTest() {
        int myPriority = 231;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "PriorityUpdater", "PriorityUpdater");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnUpdaterTest() {
        int myPriority = -7859;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);
        comTaskExecutionUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    public void updatePriorityOutOfRangeTest() {
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY + 1;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.priority(myPriority);
        comTaskExecutionUpdater.update();
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnBuilderTest() {
        boolean ignoreOnInbound = true;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.ignoreNextExecutionSpecForInbound(ignoreOnInbound);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isEqualTo(ignoreOnInbound);
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnUpdaterTest() {
        boolean ignoreOnInbound = true;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError", "WithValidationError");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.ignoreNextExecutionSpecForInbound(ignoreOnInbound);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isEqualTo(ignoreOnInbound);
    }

    @Test
    @Transactional
    public void setProtocolDialectOnBuilderTest() {
        ProtocolDialectConfigurationProperties otherDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new OtherComTaskExecutionDialect());
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Dialect", "Dialect");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.protocolDialectConfigurationProperties(otherDialect);

        // Business method
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Asserts
        AdHocComTaskExecution reloadedComTaskExecution = this.reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(otherDialect.getId());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED + "}")
    @Transactional
    public void setNullProtocolDialectTest() {
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Dialect", "Dialect");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.protocolDialectConfigurationProperties(null);
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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Dialect", "Dialect");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        AdHocComTaskExecutionUpdaterRename comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.protocolDialectConfigurationProperties(otherDialect);

        // Business method
        comTaskExecutionUpdater.update();

        // Asserts
        AdHocComTaskExecution reloadedComTaskExecution = this.reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(otherDialect.getId());
    }

    @Test
    @Transactional
    public void makeSuccessfulObsoleteTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithMyNextExecSpec", "WithMyNextExecSpec");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        InMemoryIntegrationPersistence.update("update " + TableSpecs.DDC_COMTASKEXEC.name() + " set comport = " + outboundComPort.getId() + " where id = " + comTaskExecution.getId());

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenConnectionTaskHasComServerFilledInTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        InMemoryIntegrationPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenDefaultConnectionTaskHasComServerFilledInTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest", "ObsoleteTest");
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder.useDefaultConnectionTask(true);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);
        InMemoryIntegrationPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + "where id = " + connectionTask.getId());

        // Business method
        comTaskExecution.makeObsolete();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void isScheduledTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComSchedule comSchedule = createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        ScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
        ScheduledComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        // Asserts
        ScheduledComTaskExecution reloadedComTaskExecution = this.reloadScheduledComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
        assertThat(reloadedComTaskExecution.isAdHoc()).isFalse();
    }


    @Test
    @Transactional
    public void isExecutingByComPortTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Making the comPort filled in
        ((ServerComTaskExecution) comTaskExecution).executionStarted(outboundComPort);

        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);

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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        InMemoryIntegrationPersistence.update("update " + TableSpecs.DDC_COMTASKEXEC.name() + " set " + ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName() + " = 1 where id = " + comTaskExecution.getId());
        InMemoryIntegrationPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);

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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        long future = inMemoryPersistence.getClock().now().getTime() + 1000000000000L;  // let's just hope it won't take that long until this test is finished
        InMemoryIntegrationPersistence.update("update " + TableSpecs.DDC_COMTASKEXEC.name() + " set " + ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName() + " = " + future + " where id = " + comTaskExecution.getId());
        InMemoryIntegrationPersistence.update("update " + TableSpecs.DDC_CONNECTIONTASK.name() + " set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);

        // Business method
        boolean isExecuting = reloadedComTaskExecution.isExecuting();

        // Asserts
        assertThat(isExecuting).isFalse();
    }

    @Test
    @Transactional
    public void notifyConnectionTaskRemovedTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations", "WithoutViolations");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.connectionTask(connectionTask);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        ((ComTaskExecutionImpl) comTaskExecution).connectionTaskRemoved();

        // Asserts
        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.UNIQUE_COMTASKS_PER_DEVICE + "}", strict = false)
    public void duplicateComTaskOnDeviceTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Duplicate", "Duplicate");
        ComTaskExecutionBuilder comTaskExecutionBuilder1 = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder1.add();
        device.save();
        ComTaskExecutionBuilder comTaskExecutionBuilder2 = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder2.add();

        // Business method
        device.save();

        // Asserts: see expected constraint violation rule
    }

    @Test
    @Transactional
    public void duplicateComTaskOnDeviceAfterRemoveTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Duplicate", "Duplicate");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder1 = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution1 = comTaskExecutionBuilder1.add();
        device.save();
        device.removeComTaskExecution(comTaskExecution1);

        AdHocComTaskExecutionBuilder comTaskExecutionBuilder2 = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution2 = comTaskExecutionBuilder2.add();

        // Business method
        device.save();

        // Asserts
        AdHocComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution2);
        assertThat(device.getComTaskExecutions()).hasSize(1);
        assertThat(reloadedComTaskExecution.getId()).isEqualTo(comTaskExecution2.getId());
    }

    @Test
    @Transactional
    public void isObsoleteTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "IsObsolete", "IsObsolete");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder1 = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution1 = comTaskExecutionBuilder1.add();
        device.save();
        device.removeComTaskExecution(comTaskExecution1);

        AdHocComTaskExecutionBuilder comTaskExecutionBuilder2 = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        comTaskExecutionBuilder2.add();

        // Business method
        device.save();

        // Asserts
        List<ComTaskExecution> allComTaskExecutions = inMemoryPersistence.getDeviceDataService().findAllComTaskExecutionsIncludingObsoleteForDevice(device);
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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "UpdateNextExecSpec", "UpdateNextExecSpec");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Business method
        comTaskExecution.updateNextExecutionTimestamp();

        AdHocComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void checkCorrectTimeStampsForScheduledComTaskExecutionWithAsapConnectionTaskTest() {
        freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Date fixedTimeStamp = createFixedTimeStamp(2014, 4, 5, 0, 0, 0, 0);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        ScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
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
        Date nextFromComSchedule = createFixedTimeStamp(2014, 4, 4, 11, 0, 0, 0);
        Date nextFromConnectionTask = createFixedTimeStamp(2014, 4, 5, 0, 0, 0, 0, TimeZone.getTimeZone("UTC"));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask(), new TemporalExpression(TimeDuration.hours(1)));
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        ScheduledConnectionTaskImpl connectionTask = createMinimizeOneDayConnectionStandardTask(device);
        ScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask());
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "PutOnHold", "PutOnHold");
        ScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);
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
        Date fixedTimeStamp = createFixedTimeStamp(2014, 4, 5, 3, 0, 0, 0, TimeZone.getTimeZone("UTC"));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComSchedule comSchedule = this.createComSchedule(comTaskEnablement.getComTask(), temporalExpression);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "TimeChecks", "TimeChecks");
        ScheduledComTaskExecutionBuilder comTaskExecutionBuilder = device.newScheduledComTaskExecution(comSchedule);

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
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "LockTest", "LockTest");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        OutboundComPort outboundComPort = createOutboundComPort();

        // Business method
        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getDeviceDataService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);

        // Asserts
        AdHocComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(lockComTaskExecution).isNotNull();
        assertThat(reloadedComTaskExecution.isExecuting()).isTrue();
        assertThat(reloadedComTaskExecution.getExecutingComPort().getId()).isEqualTo(outboundComPort.getId());
    }

    @Test
    @Transactional
    public void cantLockTwiceTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "LockTest", "LockTest");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        OutboundComPort outboundComPort = createOutboundComPort();
        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getDeviceDataService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);
        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);

        // Business method
        ComTaskExecution notLockedComTaskExecution = inMemoryPersistence.getDeviceDataService().attemptLockComTaskExecution(reloadedComTaskExecution, outboundComPort);

        // Asserts
        assertThat(notLockedComTaskExecution).isNull();
    }

    @Test
    @Transactional
    public void unlockComTaskExecutionTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "UnlockTest", "UnlockTest");
        AdHocComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties);
        AdHocComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        OutboundComPort outboundComPort = createOutboundComPort();

        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getDeviceDataService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);
        ComTaskExecution reloadedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);

        // Business method
        inMemoryPersistence.getDeviceDataService().unlockComTaskExecution(reloadedComTaskExecution);

        // Asserts
        ComTaskExecution notLockedComTaskExecution = reloadAdHocComTaskExecution(device, comTaskExecution);
        assertThat(notLockedComTaskExecution.isExecuting()).isFalse();
        assertThat(notLockedComTaskExecution.getExecutingComPort()).isNull();
    }

}