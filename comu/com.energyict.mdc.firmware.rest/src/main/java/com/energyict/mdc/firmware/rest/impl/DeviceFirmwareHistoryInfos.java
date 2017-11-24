/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import java.time.Instant;

public class DeviceFirmwareHistoryInfos {

    private String version;
    private String triggerdBy;
    private Instant uploadedOn;
    private Instant activationDate;
    private DeviceMessageStatus result;

    public Long getFirmwareTaskId() {
        return firmwareTaskId;
    }

    public void setFirmwareTaskId(Long firmwareTaskId) {
        this.firmwareTaskId = firmwareTaskId;
    }

    private Long firmwareTaskId;

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

    public DeviceMessageStatus getResult() {
        return result;
    }

    public void setResult(DeviceMessageStatus result) {
        this.result = result;
    }
}
