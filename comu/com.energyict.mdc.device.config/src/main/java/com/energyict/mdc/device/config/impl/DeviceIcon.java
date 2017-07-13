/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Blob;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.FileBlob;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import com.energyict.mdc.device.config.DeviceType;

import com.google.common.io.ByteStreams;

import javax.inject.Inject;
import javax.validation.constraints.Max;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

public class DeviceIcon {

    private final DataModel dataModel;
    @Max(value = 500, message = "{" + MessageSeeds.Keys.MAX_FILE_SIZE_EXCEEDED_KB + "}", groups = {Save.Create.class, Save.Update.class})
    @SuppressWarnings("unused") // For validation only
    private BigDecimal deviceIconSizeKb = BigDecimal.ZERO;
    private Blob deviceIcon = FileBlob.empty();
    private long id;
    @IsPresent
    private Reference<DeviceType> deviceType = ValueReference.absent();

    @Inject
    public DeviceIcon(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DeviceIcon initialize(DeviceType deviceType, InputStream inputStream) {
        this.deviceIcon = FileBlob.from(inputStream);
        this.deviceIconSizeKb = sizeInKb();
        this.deviceType.set(deviceType);
        return this;
    }

    private BigDecimal sizeInKb() {
        BigDecimal byteTokBScaler = BigDecimal.valueOf(1024);
        return BigDecimal.valueOf(this.deviceIcon.length()).divide(byteTokBScaler, 3, BigDecimal.ROUND_CEILING);
    }

    public void save() {
        Save.CREATE.validate(dataModel, this);
        Save.CREATE.save(dataModel, this);
    }

    public void delete() {
        this.dataModel.remove(this);
    }

    public byte[] getBlob() {
        try {
            return ByteStreams.toByteArray(deviceIcon.getBinaryStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid inputstream");
        }
    }
}
