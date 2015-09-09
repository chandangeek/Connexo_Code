package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecutionUpdater;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Tests the FirmwareComTaskExecutionImpl object
 */
public class FirmwareComTaskExecutionImplTest extends AbstractComTaskExecutionImplTest {

    private FirmwareComTaskExecution createFirmwareComTaskExecutionWithoutViolation(Device device) {
        ComTaskEnablement firmwareComTaskEnablement = enableFirmwareComTask();
        ComTaskExecutionBuilder<FirmwareComTaskExecution> comTaskExecutionBuilder = device.newFirmwareComTaskExecution(firmwareComTaskEnablement);
        return comTaskExecutionBuilder.add();
    }

    @Test
    @Transactional
    public void createWithoutViolationsTest() {

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createWithoutViolationsTest", "createWithoutViolationsTest");
        FirmwareComTaskExecution firmwareComTaskExecution = createFirmwareComTaskExecutionWithoutViolation(device);

        // Business method
        device.save();

        // Asserts
        FirmwareComTaskExecution reloadedFirmwareComTaskExecution = this.reloadFirmwareComTaskExecution(device, firmwareComTaskExecution);
        assertThat(reloadedFirmwareComTaskExecution.getNextExecutionSpecs().isPresent()).isFalse();
        assertThat(reloadedFirmwareComTaskExecution.isAdHoc()).isTrue();
        assertThat(reloadedFirmwareComTaskExecution.usesSharedSchedule()).isFalse();
        assertThat(reloadedFirmwareComTaskExecution.isScheduledManually()).isFalse();
    }

    @Test
    @Transactional
    public void nextExecutionSpecIsEmptyTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "nextExecutionSpecIsEmptyTest", "nextExecutionSpecIsEmptyTest");
        FirmwareComTaskExecution firmwareComTaskExecution = createFirmwareComTaskExecutionWithoutViolation(device);

        // Business method
        device.save();

        // asserts
        assertThat(firmwareComTaskExecution.getNextExecutionSpecs().isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void removeComTaskTest() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "removeComTaskTest", "removeComTaskTest");
        FirmwareComTaskExecution firmwareComTaskExecution = createFirmwareComTaskExecutionWithoutViolation(device);

        device.save();
        FirmwareComTaskExecution reloadedFirmwareComTaskExecution = reloadFirmwareComTaskExecution(device, firmwareComTaskExecution);
        device.removeComTaskExecution(reloadedFirmwareComTaskExecution);

        device.save();

        // Asserts
        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIRMWARE_COMTASKEXEC_NEEDS_FIRMAWARE_COMTASKENABLEMENT + "}")
    public void createWithOtherComTaskEnablementTest() {
        ComTaskEnablement basicCheckComTaskEnablement = enableComTask(true);
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "createWithOtherComTaskEnablementTest", "createWithOtherComTaskEnablementTest");
        ComTaskExecutionBuilder<FirmwareComTaskExecution> comTaskExecutionBuilder = device.newFirmwareComTaskExecution(basicCheckComTaskEnablement);
        FirmwareComTaskExecution firmwareComTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();
    }

    @Test
    @Transactional
    public void updateProtocolDialectTest() {
        ComTaskEnablement firmwareComTaskEnablement = enableFirmwareComTask();

        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "updateProtocolDialectTest", "updateProtocolDialectTest");
        ComTaskExecutionBuilder<FirmwareComTaskExecution> comTaskExecutionBuilder = device.newFirmwareComTaskExecution(firmwareComTaskEnablement);
        FirmwareComTaskExecution firmwareComTaskExecution = comTaskExecutionBuilder.add();

        // Business method
        device.save();

        ProtocolDialectConfigurationProperties otherDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new OtherComTaskExecutionDialect());

        FirmwareComTaskExecution reloadedFirmwareComTaskExecution = this.reloadFirmwareComTaskExecution(device, firmwareComTaskExecution);
        FirmwareComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(reloadedFirmwareComTaskExecution);
        comTaskExecutionUpdater.protocolDialectConfigurationProperties(otherDialect);
        FirmwareComTaskExecution updatedComTaskExecution = comTaskExecutionUpdater.update();

        FirmwareComTaskExecution reloadedUpdatedComTaskExecution = this.reloadFirmwareComTaskExecution(device, updatedComTaskExecution);

        //Asserts
        assertThat(reloadedUpdatedComTaskExecution.getProtocolDialectConfigurationProperties()).isNotNull();
        assertThat(reloadedUpdatedComTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(otherDialect.getId());
    }

    protected FirmwareComTaskExecution reloadFirmwareComTaskExecution(Device device, FirmwareComTaskExecution comTaskExecution) {
        Device reloadedDevice = getReloadedDevice(device);
        for (ComTaskExecution taskExecution : reloadedDevice.getComTaskExecutions()) {
            if (comTaskExecution.getId() == taskExecution.getId()) {
                return (FirmwareComTaskExecution) taskExecution;
            }
        }
        fail("FirmwareComTaskExecution with id " + comTaskExecution.getId() + " not found after reloading device " + device.getName());
        return null;
    }

    private ComTaskEnablement enableFirmwareComTask() {
        ComTask firmwareComTask = inMemoryPersistence.getTaskService().findFirmwareComTask().get();
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        ComTaskEnablementBuilder builder = this.deviceConfiguration.enableComTask(firmwareComTask, this.securityPropertySet, configDialect);
        builder.useDefaultConnectionTask(true);
        builder.setPriority(this.comTaskEnablementPriority);
        return builder.add();
    }
}