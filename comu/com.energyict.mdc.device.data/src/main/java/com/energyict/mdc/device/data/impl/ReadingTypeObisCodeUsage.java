package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

public class ReadingTypeObisCodeUsage {

    @IsPresent
    private Reference<ReadingType> readingType = ValueReference.absent();
    @IsPresent
    private Reference<Device> device = ValueReference.absent();
    private ObisCode obisCode;
    private DataModel dataModel;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    ReadingTypeObisCodeUsage(DataModel dataModel) {
        this.dataModel = dataModel;
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

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
        update();
    }

    public ObisCode getObisCode() {
        return this.obisCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadingTypeObisCodeUsage that = (ReadingTypeObisCodeUsage) o;
        return this.getDevice().getId() == that.getDevice().getId() &&
                this.getReadingType().getMRID().equalsIgnoreCase(that.getReadingType().getMRID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, readingType);
    }

}
