/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.gogo;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.tasks.TaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Optional;

@Component(name = "com.energyict.mdc.gogo.FirmwareUtils", service = FirmwareUtils.class,
        property = {"osgi.command.scope=mdc.firmware",
                "osgi.command.function=createFirmwareManagementOptionsFor",
                "osgi.command.function=createFirmwareMessageFor",
                "osgi.command.function=triggerFirmwareTaskFor",
                "osgi.command.function=createFirmwareVersionFor"},
        immediate = true)
@SuppressWarnings("unused")
public class FirmwareUtils {

    private volatile FirmwareService firmwareService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile OrmService ormService;
    private volatile Clock clock;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile DeviceService deviceService;
    private volatile UserService userService;
    private volatile TaskService taskService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile ConnectionTaskService connectionTaskService;

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    /**
     * Creates an options file.
     */
    public void createFirmwareManagementOptionsFor(String deviceTypeName) {
        Optional<DeviceType> deviceTypeOptional = this.deviceConfigurationService.findDeviceTypeByName(deviceTypeName);
        if (deviceTypeOptional.isPresent()) {
            executeTransaction(() -> {
                DeviceType deviceType = deviceTypeOptional.get();
                DataModel dataModel = ormService.getDataModel("FWC").get();
                try (Connection connection = getConnection(dataModel)) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertFirmwareUpgradeOptionsSql())) {
                        preparedStatement.setLong(1, deviceType.getId());
                        preparedStatement.setString(2, "0");
                        preparedStatement.setString(3, "1");
                        preparedStatement.setString(4, "0");
                        preparedStatement.setLong(5, 1);
                        preparedStatement.setLong(6, clock.millis());
                        preparedStatement.setLong(7, clock.millis());
                        preparedStatement.setString(8, "gogo");
                        int numberOfRowsInserted = preparedStatement.executeUpdate();
                        if (numberOfRowsInserted == 1) {
                            System.out.println("Insert complete");
                        } else {
                            System.out.println("Failed to create proper FirmwareManagementOptions for " + deviceTypeName);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } else {
            System.out.println("No DeviceType found with the name '" + deviceTypeName + "'");
        }
    }

    public void createFirmwareVersionFor(String deviceTypeName, String fileLocation, String version) throws IOException {
        Optional<DeviceType> deviceTypeOptional = this.deviceConfigurationService.findDeviceTypeByName(deviceTypeName);
        if (deviceTypeOptional.isPresent()) {
            byte[] firmwareBytes = getFirmwareBytes(fileLocation);
            System.out.println("Size of file: " + firmwareBytes.length);
            executeTransaction(() -> {
                DeviceType deviceType = deviceTypeOptional.get();
                DataModel dataModel = ormService.getDataModel("FWC").get();
                try (Connection connection = getConnection(dataModel)) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertFirmwareVersionSql())) {
                        preparedStatement.setString(1, version);
                        preparedStatement.setLong(2, deviceType.getId());
                        preparedStatement.setString(3, "1");
                        preparedStatement.setString(4, "1");
                        preparedStatement.setBlob(5, new ByteArrayInputStream(firmwareBytes)); // the blob
                        preparedStatement.setLong(6, 1);
                        preparedStatement.setLong(7, clock.millis());
                        preparedStatement.setLong(8, clock.millis());
                        preparedStatement.setString(9, "gogo");
                        int numberOfRowsInserted = preparedStatement.executeUpdate();
                        if (numberOfRowsInserted == 1) {
                            System.out.println("Upload complete");
                        } else {
                            System.out.println("Failed to upload proper FirmwareVersion for " + deviceTypeName);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } else {
            System.out.println("No DeviceType found with the name '" + deviceTypeName + "'");
        }
    }

    public void createFirmwareMessageFor(String deviceName, String firwareVersion) {
        Optional<Device> device = this.deviceService.findDeviceByName(deviceName);
        if (device.isPresent()) {
            Optional<FirmwareVersion> firmwareVersionByVersion = this.firmwareService.getFirmwareVersionByVersionAndType(firwareVersion, FirmwareType.METER, device.get().getDeviceType());
            firmwareVersionByVersion.ifPresent(firmwareVersion -> executeTransaction(() -> {
                Device.DeviceMessageBuilder deviceMessageBuilder = device.get().newDeviceMessage(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);

                DeviceMessageSpec deviceMessageSpec = deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE.dbValue()).get();
                PropertySpec firmwareVersionPropertySpec = deviceMessageSpec.getPropertySpecs()
                        .stream()
                        .filter(propertySpec -> propertySpec.getValueFactory().getValueType().equals(BaseFirmwareVersion.class))
                        .findAny()
                        .get();

                DeviceMessage deviceMessage = deviceMessageBuilder
                        .addProperty(firmwareVersionPropertySpec.getName(), firmwareVersion)
                        .setReleaseDate(clock.instant())
                        .add();
                System.out.println("Create message for " + deviceName + " with id " + deviceMessage.getId());
                return null;
            }));
        } else {
            System.out.println("No Device found with the name '" + deviceName + "'");
        }
    }

    public void triggerFirmwareTaskFor(String deviceName) {
        Optional<Device> device = this.deviceService.findDeviceByName(deviceName);
        if (device.isPresent()) {
            Optional<ComTask> firmwareComTask = this.taskService.findFirmwareComTask();
            if (firmwareComTask.isPresent()) {
                Optional<ComTaskExecution> existingFirmwareComTaskExecution = device.get().getComTaskExecutions().stream()
                        .filter(comTaskExecution -> comTaskExecution.getComTask().getId() == firmwareComTask.get().getId())
                        .findFirst();

                executeTransaction(() -> {
                    if (existingFirmwareComTaskExecution.isPresent()) {
                        System.out.println("Reusing existing FirmwareComTaskExecution");
                        connectionTaskService.findAndLockConnectionTaskById(existingFirmwareComTaskExecution.get().getConnectionTaskId());
                        communicationTaskService.findAndLockComTaskExecutionById(existingFirmwareComTaskExecution.get().getId()).ifPresent(ComTaskExecution::runNow);
                    } else {
                        System.out.println("Creating a new FirmwareComTaskExecution based on the enablement of the config");
                        Optional<ComTaskEnablement> firmwareComTaskEnablement = device.get().getDeviceConfiguration().getComTaskEnablementFor(firmwareComTask.get());
                        if (firmwareComTaskEnablement.isPresent()) {
                            ComTaskExecutionBuilder firmwareComTaskExecutionBuilder = device.get().newFirmwareComTaskExecution(firmwareComTaskEnablement.get());
                            ComTaskExecution firmwareComTaskExecution = firmwareComTaskExecutionBuilder.add();
                            device.get().save();
                            communicationTaskService.findAndLockComTaskExecutionById(firmwareComTaskExecution.getId()).ifPresent(ComTaskExecution::runNow);
                            System.out.println("Properly triggered the firmwareComTask, his next timestamp is " + firmwareComTaskExecution.getNextExecutionTimestamp());
                        } else {
                            System.out.println("There is no 'Firmware management' ComTaskEnablement defined for device " + deviceName);
                        }
                    }
                    return null;
                });
            } else {
                System.out.println("There is no 'Firmware management' ComTask defined, run the 'init FWC' command first.");
            }
        } else {
            System.out.println("No Device found with the name '" + deviceName + "'");
        }
    }

    private String getInsertFirmwareVersionSql() {
        return "insert into FWC_FIRMWAREVERSION values(FWC_FIRMWAREVERSIONID.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private byte[] getFirmwareBytes(String fileLocation) throws IOException {
        Path path = Paths.get(fileLocation);
        return Files.readAllBytes(path);
    }

    private String getInsertFirmwareUpgradeOptionsSql() {
        return "insert into FWC_FIRMWAREUPGRADEOPTIONS values(?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private Connection getConnection(DataModel dataModel) throws SQLException {
        return dataModel.getConnection(true);
    }

    private <T> T executeTransaction(Transaction<T> transaction) {
        setPrincipal();
        try {
            return transactionService.execute(transaction);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            clearPrincipal();
        }
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private Principal getPrincipal() {
        return this.userService.findUser("admin", userService.getRealm()).get();
    }
}
