package com.energyict.mdc.device.data.rest;

import java.time.Instant;

import com.elster.jupiter.favorites.DeviceLabel;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;

public class DeviceLabelInfo {

    public IdWithNameInfo category;
    public String comment;
    public Instant creationDate;
    
    public DeviceLabelInfo() {
    }
    
    public DeviceLabelInfo(DeviceLabel deviceLabel) {
        this.category = new IdWithNameInfo();
        this.category.id = deviceLabel.getLabelCategory().getName();
        this.comment = deviceLabel.getComment();
        this.creationDate = deviceLabel.getCreationDate();
    }
    
    public DeviceLabelInfo(DeviceLabel deviceLabel, Thesaurus thesaurus) {
        String categoryName = deviceLabel.getLabelCategory().getName();
        this.category = new IdWithNameInfo(categoryName, thesaurus.getString(categoryName, categoryName));
        this.comment = deviceLabel.getComment();
        this.creationDate = deviceLabel.getCreationDate();
    }
}
