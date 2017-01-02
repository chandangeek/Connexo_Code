package com.energyict.mdc.firmware;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 04.09.15
 * Time: 10:35
 */
public interface FirmwareManagementDeviceUtils {
    Optional<DeviceMessage> getUploadMessageForActivationMessage(DeviceMessage activationMessage);

    Optional<DeviceMessage> getActivationMessageForUploadMessage(DeviceMessage uploadMessage);

    Optional<ProtocolSupportedFirmwareOptions> getUploadOptionFromMessage(DeviceMessage message);

    Optional<Instant> getActivationDateFromMessage(DeviceMessage message);

    Optional<FirmwareVersion> getFirmwareVersionFromMessage(DeviceMessage message);

    boolean messageContainsActiveFirmwareVersion(DeviceMessage message);

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

    List<DeviceMessage> getFirmwareMessages();

    List<DeviceMessage> getPendingFirmwareMessages();

    Instant getCurrentInstant();

    boolean cancelPendingFirmwareUpdates(FirmwareType firmwareType);

    boolean isPendingMessage(DeviceMessage upgradeMessage);

    boolean firmwareTaskIsScheduled();
}
