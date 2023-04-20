/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface FirmwareManagementDeviceUtils {
    Device getDevice();

    Optional<DeviceMessage> getUploadMessageForActivationMessage(DeviceMessage activationMessage);

    Optional<DeviceMessage> getActivationMessageForUploadMessage(DeviceMessage uploadMessage);

    Optional<ProtocolSupportedFirmwareOptions> getUploadOptionFromMessage(DeviceMessage message);

    Optional<Instant> getActivationDateFromMessage(DeviceMessage message);

    Optional<FirmwareVersion> getFirmwareVersionFromMessage(DeviceMessage message);

    boolean messageContainsActiveFirmwareVersion(DeviceMessage message);

    boolean isFirmwareUploadTaskBusy();

    boolean verifyFirmwareVersionTaskIsBusy();

    boolean isFirmwareUploadTaskFailed();

    boolean verifyFirmwareVersionTaskIsFailed();

    String translate(String key);

    Optional<ComTaskExecution> getFirmwareComTaskExecution();

    Optional<ComTaskExecution> lockFirmwareComTaskExecution();

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

    boolean isReadOutAfterLastFirmwareUpgrade();
}
