package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.impl.DeviceVersionInfo;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.LabelCategory;

import java.time.Instant;

public class DeviceLabelInfo extends DeviceVersionInfo {

    public IdWithNameInfo category;
    public String comment;
    public Instant creationDate;
    
    public DeviceLabelInfo() {
    }
    
    public DeviceLabelInfo(DeviceLabel deviceLabel) {
        this.category = new IdWithNameInfo();
        this.category.id = deviceLabel.getLabelCategory().getName();
        fillCommonInfo(deviceLabel);

    }
    
    public DeviceLabelInfo(DeviceLabel deviceLabel, Thesaurus thesaurus) {
        LabelCategory category = deviceLabel.getLabelCategory();
        this.category = new IdWithNameInfo(category.getName(), thesaurus.getString(category.getName(), category.getName()));
        fillCommonInfo(deviceLabel);
    }

    private void fillCommonInfo(DeviceLabel deviceLabel) {
        this.comment = deviceLabel.getComment();
        this.creationDate = deviceLabel.getCreationDate();
        Device device = deviceLabel.getDevice();
        this.mRID = device.getmRID();
        this.version = device.getVersion();
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        this.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
    }
}
