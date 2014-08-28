package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.masterdata.LogBookType;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 25/03/14
 * Time: 15:50
 */
public class LogBookImpl implements LogBook {

    private final DataModel dataModel;

    private long id;
    private Reference<DeviceImpl> device = ValueReference.absent();
    private Reference<LogBookSpec> logBookSpec = ValueReference.absent();
    private UtcInstant lastReading;
    private UtcInstant modTime;


    @Inject
    public LogBookImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    LogBookImpl initialize(LogBookSpec logBookSpec, DeviceImpl device) {
        this.logBookSpec.set(logBookSpec);
        this.device.set(device);
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    @Override
    public Date getLastLogBook() {
        if (lastReading != null) {
            return lastReading.toDate();
        } else {
            return null;
        }
    }

    private void update() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public ObisCode getDeviceObisCode() {
        return getLogBookSpec().getDeviceObisCode();
    }

    @Override
    public LogBookSpec getLogBookSpec() {
        return logBookSpec.orNull();
    }

    @Override
    public LogBookType getLogBookType() {
        return getLogBookSpec().getLogBookType();
    }

    @Override
    public Date getModTime() {
        return modTime !=null? modTime.toDate():null;
    }

    @Override
    public List<EndDeviceEventRecord> getEndDeviceEvents(Interval interval) {
        return this.device.get().getLogBookDeviceEvents(this, interval);
    }

    abstract static class LogBookUpdater implements LogBook.LogBookUpdater {

        private final LogBookImpl logBook;

        protected LogBookUpdater(LogBookImpl logBook) {
            this.logBook = logBook;
        }

        @Override
        public LogBook.LogBookUpdater setLastLogBookIfLater(Date lastReading) {
            UtcInstant logBookLastReading = this.logBook.lastReading;
            if (lastReading != null && (logBookLastReading == null || lastReading.after(logBookLastReading.toDate()))) {
                this.logBook.lastReading = new UtcInstant(lastReading);
            }
            return this;
        }

        @Override
        public LogBook.LogBookUpdater setLastReadingIfLater(Date createTime) {
            UtcInstant logBookCreateTime = this.logBook.modTime;
            if (createTime != null && (logBookCreateTime == null || createTime.after(logBookCreateTime.toDate()))) {
                this.logBook.modTime = new UtcInstant(createTime);
            }
            return this;

        }

        @Override
        public void update() {
            this.logBook.update();
        }
    }

}