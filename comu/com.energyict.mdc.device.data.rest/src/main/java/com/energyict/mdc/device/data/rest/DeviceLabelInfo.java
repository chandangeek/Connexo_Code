package com.energyict.mdc.device.data.rest;

import java.time.Instant;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.LabelCategory;

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
        LabelCategory category = deviceLabel.getLabelCategory();
        this.category = new IdWithNameInfo(category.getName(), thesaurus.getString(category.getName(), category.getName()));
        this.comment = deviceLabel.getComment();
        this.creationDate = deviceLabel.getCreationDate();
    }
}
