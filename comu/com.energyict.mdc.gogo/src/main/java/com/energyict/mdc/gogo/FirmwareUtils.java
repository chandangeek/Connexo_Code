package com.energyict.mdc.gogo;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
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

/**
 * Copyrights EnergyICT
 * Date: 3/17/15
 * Time: 2:23 PM
 */
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

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
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

    public void createFirmwareMessageFor(String mridOfDevice, String firwareVersion) {
        Optional<Device> device = this.deviceService.findByUniqueMrid(mridOfDevice);
        if (device.isPresent()) {
            Optional<FirmwareVersion> firmwareVersionByVersion = this.firmwareService.getFirmwareVersionByVersionAndType(firwareVersion, FirmwareType.METER, device.get().getDeviceType());
            firmwareVersionByVersion.ifPresent(firmwareVersion -> executeTransaction(() -> {
                Device.DeviceMessageBuilder deviceMessageBuilder = device.get().newDeviceMessage(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);
                DeviceMessage<Device> deviceMessage = deviceMessageBuilder
                        .addProperty(DeviceMessageConstants.firmwareUpdateFileAttributeName, firmwareVersion)
                        .setReleaseDate(clock.instant())
                        .add();
                System.out.println("Create message for " + mridOfDevice + " with id " + deviceMessage.getId());
                return null;
            }));
        } else {
            System.out.println("No Device found with the mrid '" + mridOfDevice + "'");
        }
    }

    public void triggerFirmwareTaskFor(String mridOfDevice) {
        Optional<Device> device = this.deviceService.findByUniqueMrid(mridOfDevice);
        if (device.isPresent()) {
            Optional<ComTask> firmwareComTask = this.taskService.findFirmwareComTask();
            if (firmwareComTask.isPresent()) {
                Optional<ComTaskExecution> existingFirmwareComTaskExecution = device.get().getComTaskExecutions().stream()
                        .filter(comTaskExecution -> comTaskExecution.getComTasks().stream().filter(comTask -> comTask.getId() == firmwareComTask.get().getId()).count() > 0).findFirst();

                executeTransaction(() -> {
                    if (existingFirmwareComTaskExecution.isPresent()) {
                        System.out.println("Reusing existing FirmwareComTaskExecution");
                        ComTaskExecution firmwareComTaskExecution = existingFirmwareComTaskExecution.get();
                        firmwareComTaskExecution.runNow();
                        System.out.println("Properly triggered the firmwareComTask, his next timestamp is " + firmwareComTaskExecution.getNextExecutionTimestamp());
                    } else {
                        System.out.println("Creating a new FirmwareComTaskExecution based on the enablement of the config");
                        Optional<ComTaskEnablement> firmwareComTaskEnablement = device.get().getDeviceConfiguration().getComTaskEnablementFor(firmwareComTask.get());
                        if (firmwareComTaskEnablement.isPresent()) {
                            ComTaskExecutionBuilder<FirmwareComTaskExecution> firmwareComTaskExecutionBuilder = device.get().newFirmwareComTaskExecution(firmwareComTaskEnablement.get());
                            FirmwareComTaskExecution firmwareComTaskExecution = firmwareComTaskExecutionBuilder.add();
                            device.get().save();
                            firmwareComTaskExecution.runNow();
                            System.out.println("Properly triggered the firmwareComTask, his next timestamp is " + firmwareComTaskExecution.getNextExecutionTimestamp());
                        } else {
                            System.out.println("There is no 'Firmware management' ComTaskEnablement defined for device " + mridOfDevice);
                        }
                    }
                    return null;
                });
            } else {
                System.out.println("There is no 'Firmware management' ComTask defined, run the 'init FWC' command first.");
            }
        } else {
            System.out.println("No Device found with the mrid '" + mridOfDevice + "'");
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
        return this.userService.findUser("admin").get();
    }
}
