/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.loadprofilenextreading;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.time.ZonedDateTime;

public class DeviceLoadProfileNextReadingRecord extends FileImportRecord {

    private String loadProfileOBIS ;  // load profile OBIS code
    private ZonedDateTime nextReadingBlockDateTime;   // next reading block date

    public void setLoadProfileOBIS(String loadProfile) {
        this.loadProfileOBIS = loadProfile;
    }

    public String getLoadProfilesOBIS() {
        return this.loadProfileOBIS;
    }

    public ZonedDateTime getLoadProfileNextReadingBlockDateTime() {
        return this.nextReadingBlockDateTime;
    }

    public void setLoadProfileNextReadingBlockDateTime(ZonedDateTime nextReadingBlock) {
        this.nextReadingBlockDateTime = nextReadingBlock;
    }
}
