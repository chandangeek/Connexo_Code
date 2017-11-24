/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import java.time.Instant;

public class DeviceFirmwareHistoryInfos {

    private String version;
    private String triggerdBy;
    private Instant uploadedOn;
    private Instant activationDate;
    private String result;
    private Long firmwareTaskId;

    public DeviceFirmwareHistoryInfos(DeviceMessage deviceMessage, FirmwareManagementDeviceUtils versionUtils, Thesaurus thesaurus) {
        buildFrom(deviceMessage, versionUtils, thesaurus);
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTriggerdBy() {
        return triggerdBy;
    }

    public void setTriggerdBy(String triggerdBy) {
        this.triggerdBy = triggerdBy;
    }

    public Instant getUploadedOn() {
        return uploadedOn;
    }

    public void setUploadedOn(Instant uploadedOn) {
        this.uploadedOn = uploadedOn;
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

    private void buildFrom(DeviceMessage deviceMessage, FirmwareManagementDeviceUtils versionUtils, Thesaurus thesaurus) {
        this.setUploadedOn(deviceMessage.getCreationDate());
        this.setResult(DeviceMessageStatusTranslationKeys.translationFor(deviceMessage.getStatus(), thesaurus));
        this.setTriggerdBy(deviceMessage.getUser());
        this.setVersion(versionUtils.getFirmwareVersionFromMessage(deviceMessage).map(FirmwareVersion::getFirmwareVersion).orElse(null));
        this.setActivationDate(versionUtils.getActivationDateFromMessage(deviceMessage).orElse(null));
        this.setFirmwareTaskId(versionUtils.getFirmwareTask().get().getId());
    }
}
