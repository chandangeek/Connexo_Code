package com.energyict.mdc.device.data.validation.rest.impl;

import com.energyict.mdc.device.data.validation.ValidationOverview;

/**
 * Created by Lucian on 6/18/2015.
 */
public class ValidationSummaryInfo {

    public String mrid;
    public String serialNumber;
    public String deviceType;
    public String deviceConfig;

    public ValidationSummaryInfo(ValidationOverview validationOverview) {
        this.mrid = validationOverview.getMrid();
        this.serialNumber = validationOverview.getSerialNumber();
        this.deviceType = validationOverview.getDeviceType();
        this.deviceConfig = validationOverview.getDeviceConfig();
    }
}
