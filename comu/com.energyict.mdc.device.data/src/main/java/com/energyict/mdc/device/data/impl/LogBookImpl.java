package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
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

    enum FieldNames {
        LATEST_EVENT_OCCURRENCE_IN_METER("lastEventOccurrence"),
        LATEST_EVENT_CREATED_IN_DB("latestEventAddition");

        FieldNames(String name) {
            this.name = name;
        }

        private final String name;

        public String fieldName() {
            return name;
        }
    }

    private long id;
    private Reference<DeviceImpl> device = ValueReference.absent();
    private Reference<LogBookSpec> logBookSpec = ValueReference.absent();
    private UtcInstant lastEventOccurrence;
    private UtcInstant latestEventAddition;


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
        if (lastEventOccurrence != null) {
            return lastEventOccurrence.toDate();
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
    public Date getLatestEventAdditionDate() {
        return latestEventAddition !=null? latestEventAddition.toDate():null;
    }

    @Override
    public List<EndDeviceEventRecord> getEndDeviceEvents(Interval interval) {
        EndDeviceEventRecordFilterSpecification filter = new EndDeviceEventRecordFilterSpecification();
        filter.interval = interval;
        return getEndDeviceEventsByFilter(filter);
    }
    
    @Override
    public List<EndDeviceEventRecord> getEndDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter) {
        return this.device.get().getLogBookDeviceEventsByFilter(this, filter);
    }

    abstract static class LogBookUpdater implements LogBook.LogBookUpdater {

        private final LogBookImpl logBook;

        protected LogBookUpdater(LogBookImpl logBook) {
            this.logBook = logBook;
        }

        @Override
        public LogBook.LogBookUpdater setLastLogBookIfLater(Date lastReading) {
            UtcInstant logBookLastReading = this.logBook.lastEventOccurrence;
            if (lastReading != null && (logBookLastReading == null || lastReading.after(logBookLastReading.toDate()))) {
                this.logBook.lastEventOccurrence = new UtcInstant(lastReading);
            }
            return this;
        }

        @Override
        public LogBook.LogBookUpdater setLastReadingIfLater(Date createTime) {
            UtcInstant logBookCreateTime = this.logBook.latestEventAddition;
            if (createTime != null && (logBookCreateTime == null || createTime.after(logBookCreateTime.toDate()))) {
                this.logBook.latestEventAddition = new UtcInstant(createTime);
            }
            return this;

        }

        @Override
        public void update() {
            this.logBook.update();
        }
    }

}