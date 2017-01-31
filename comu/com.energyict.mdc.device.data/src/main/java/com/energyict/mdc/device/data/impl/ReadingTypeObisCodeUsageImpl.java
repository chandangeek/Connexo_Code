/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

public class ReadingTypeObisCodeUsageImpl implements ReadingTypeObisCodeUsage {

    enum Fields {
        READINGTYPE("readingType"),
        device("device"),
        OBISCODESTRING("obisCodeString"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<ReadingType> readingType = ValueReference.absent();
    @IsPresent
    private Reference<Device> device = ValueReference.absent();
    private String obisCodeString;
    private DataModel dataModel;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    ReadingTypeObisCodeUsageImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public ReadingTypeObisCodeUsageImpl initialize(Device device, ReadingType readingType, ObisCode obisCode) {
        this.device.set(device);
        this.readingType.set(readingType);
        this.obisCodeString = obisCode.toString();
        return this;
    }

    public ReadingType getReadingType() {
        return readingType.get();
    }

    public Device getDevice() {
        return device.get();
    }

    public void update() {
        dataModel.update(this);
    }

    public ObisCode getObisCode() {
        return this.obisCodeString!=null ? ObisCode.fromString(obisCodeString) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadingTypeObisCodeUsageImpl that = (ReadingTypeObisCodeUsageImpl) o;
        return this.getDevice().getId() == that.getDevice().getId() &&
                this.getReadingType().getMRID().equalsIgnoreCase(that.getReadingType().getMRID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, readingType);
    }

}
