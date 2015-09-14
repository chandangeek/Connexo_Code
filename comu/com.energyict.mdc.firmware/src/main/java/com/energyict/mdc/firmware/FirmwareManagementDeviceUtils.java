package com.energyict.mdc.firmware;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.tasks.ComTask;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 04.09.15
 * Time: 10:35
 */
public interface FirmwareManagementDeviceUtils {
    Optional<DeviceMessage<Device>> getUploadMessageForActivationMessage(DeviceMessage<Device> activationMessage);

    Optional<DeviceMessage<Device>> getActivationMessageForUploadMessage(DeviceMessage<Device> uploadMessage);

    Optional<ProtocolSupportedFirmwareOptions> getUploadOptionFromMessage(DeviceMessage<Device> message);

    Optional<Instant> getActivationDateFromMessage(DeviceMessage<Device> message);

    Optional<FirmwareVersion> getFirmwareVersionFromMessage(DeviceMessage<Device> message);

    boolean messageContainsActiveFirmwareVersion(DeviceMessage<Device> message);

    boolean firmwareUploadTaskIsBusy();

    boolean verifyFirmwareVersionTaskIsBusy();

    boolean firmwareUploadTaskIsFailed();

    boolean verifyFirmwareVersionTaskIsFailed();

    String translate(String key);

    Optional<ComTaskExecution> getFirmwareComTaskExecution();

    Optional<ComTask> getFirmwareTask();

    Optional<ComTaskExecution> getComTaskExecutionToCheckTheFirmwareVersion();

    Optional<ComTaskEnablement> getComTaskEnablementToCheckTheFirmwareVersion();

    Optional<ComTask> getFirmwareCheckTask();

    List<DeviceMessage<Device>> getFirmwareMessages();

    List<DeviceMessage<Device>> getPendingFirmwareMessages();

    Instant getCurrentInstant();

    boolean cancelPendingFirmwareUpdates(FirmwareType firmwareType);

    boolean isPendingMessage(DeviceMessage<Device> upgradeMessage);
}
