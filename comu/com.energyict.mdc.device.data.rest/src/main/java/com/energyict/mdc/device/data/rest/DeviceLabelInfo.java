package com.energyict.mdc.device.data.rest;

import java.time.Instant;

import com.elster.jupiter.favorites.DeviceLabel;
import com.energyict.mdc.common.rest.IdWithNameInfo;

public class DeviceLabelInfo {

    public IdWithNameInfo category;
    public String comment;
    public Instant creationDate;
    
    public DeviceLabelInfo() {
    }
    
    public DeviceLabelInfo(DeviceLabel deviceLabel) {
        this.category = new IdWithNameInfo(deviceLabel.getLabelCategory().getName(), deviceLabel.getLabelCategory().getTranlatedName());
        this.comment = deviceLabel.getComment();
        this.creationDate = deviceLabel.getCreationDate();
    }
}
