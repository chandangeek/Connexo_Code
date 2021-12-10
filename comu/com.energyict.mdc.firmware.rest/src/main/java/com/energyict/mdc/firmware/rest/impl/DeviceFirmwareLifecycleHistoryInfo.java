/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.time.Instant;

public class DeviceFirmwareLifecycleHistoryInfo {

    private String firmwareVersion;
    private String imageIdentifier;
    private String triggeredBy;
    private Instant uploadedDate;
    private Instant plannedDate;
    private Instant activationDate;
    private String result;
    private Long firmwareTaskId;

    public DeviceFirmwareLifecycleHistoryInfo(DeviceMessage deviceMessage, FirmwareManagementDeviceUtils versionUtils, Thesaurus thesaurus) {
        buildDeviceFirmwareHistoryInfosFrom(deviceMessage, versionUtils, thesaurus);
    }


    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getImageIdentifier() {
        return imageIdentifier;
    }

    public void setImageIdentifier(String imageIdentifier) {
        this.imageIdentifier = imageIdentifier;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Instant getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Instant uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public Instant getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(Instant plannedDate) {
        this.plannedDate = plannedDate;
    }

    public Instant getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Instant activationDate) {
        this.activationDate = activationDate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getFirmwareTaskId() {
        return firmwareTaskId;
    }

    public void setFirmwareTaskId(Long firmwareTaskId) {
        this.firmwareTaskId = firmwareTaskId;
    }

    private void buildDeviceFirmwareHistoryInfosFrom(DeviceMessage deviceMessage, FirmwareManagementDeviceUtils versionUtils, Thesaurus thesaurus) {
        this.setPlannedDate(deviceMessage.getReleaseDate());
        this.setUploadedDate(deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING) ? null : deviceMessage.getModTime());
        this.setResult(DeviceMessageStatusTranslationKeys.translationFor(deviceMessage.getStatus(), thesaurus));
        this.setTriggeredBy(deviceMessage.getUser());
        this.setFirmwareVersion(versionUtils.getFirmwareVersionFromMessage(deviceMessage).map(FirmwareVersion::getFirmwareVersion).orElse(null));
        this.setImageIdentifier(versionUtils.getFirmwareVersionFromMessage(deviceMessage).map(FirmwareVersion::getImageIdentifier).orElse(null));
        this.setActivationDate(versionUtils.getActivationDateFromMessage(deviceMessage).orElse(deviceMessage.getStatus().equals(DeviceMessageStatus.PENDING) ? null : deviceMessage.getModTime()));
        this.setFirmwareTaskId(versionUtils.getFirmwareTask().get().getId());
    }
}
