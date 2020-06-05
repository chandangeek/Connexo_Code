/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.device.config.LogBookSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.device.data.impl.configchange.ServerLogBookForConfigChange;

import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class LogBookImpl implements ServerLogBookForConfigChange {

    private final DataModel dataModel;
    private final Clock clock;

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
    private Instant lastEventOccurrence;    // last event time stamp (data until)
    private Instant latestEventAddition;    // last reading (next reading block start)
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    public LogBookImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
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
    public Optional<Instant> getLastLogBook() {
        return Optional.ofNullable(this.lastEventOccurrence);
    }

    @Override
    public Date getLastReading() {
        return lastEventOccurrence == null ? null : Date.from(lastEventOccurrence);
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
    public Optional<Instant> getLatestEventAdditionDate() {
        return Optional.ofNullable(latestEventAddition);
    }

    @Override
    public List<EndDeviceEventRecord> getEndDeviceEvents(Range<Instant> interval) {
        EndDeviceEventRecordFilterSpecification filter = new EndDeviceEventRecordFilterSpecification();
        filter.range = interval;
        return getEndDeviceEventsByFilter(filter);
    }

    @Override
    public List<EndDeviceEventRecord> getEndDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter) {
        if (filter == null){
            return Collections.emptyList();
        }
        filter.logBookId = this.getId();
        return this.device.get().getDeviceEventsByFilter(filter);
    }

    @Override
    public void setNewLogBookSpec(LogBookSpec logBookSpec) {
        this.logBookSpec.set(logBookSpec);
        Instant now = Instant.now(clock);
        if(this.latestEventAddition != null && this.latestEventAddition.isAfter(now)) {
            this.latestEventAddition = now;
        }
        this.dataModel.update(this, "logBookSpec", "latestEventAddition");
    }

    abstract static class LogBookUpdater implements LogBook.LogBookUpdater {

        private final LogBookImpl logBook;

        protected LogBookUpdater(LogBookImpl logBook) {
            this.logBook = logBook;
        }

        @Override
        public LogBook.LogBookUpdater setLastLogBookIfLater(Instant lastReading) {
            Instant logBookLastReading = this.logBook.lastEventOccurrence;
            if (lastReading != null && (logBookLastReading == null || lastReading.isAfter(logBookLastReading))) {
                this.logBook.lastEventOccurrence = lastReading;
                warnIfInFuture(lastReading, "last reading");
            }
            return this;
        }

        @Override
        public LogBook.LogBookUpdater setLastReadingIfLater(Instant createTime) {
            Instant logBookCreateTime = this.logBook.latestEventAddition;
            if (createTime != null && (logBookCreateTime == null || createTime.isAfter(logBookCreateTime))) {
                this.logBook.latestEventAddition = createTime;
                warnIfInFuture(createTime, "creation");
            }
            return this;

        }

        @Override
        public LogBook.LogBookUpdater setLastReading(Instant createTime) {
            if (createTime != null ) {
                this.logBook.latestEventAddition = createTime;
                warnIfInFuture(createTime, "creation");
            }
            return this;
        }

        @Override
        public void update() {
            this.logBook.update();
        }

        private void warnIfInFuture(Instant createTime, String parameterName) {
            if (createTime.isAfter(Instant.now())) {
                Logger.getLogger(getClass().getName()).warning("received logbook " + parameterName + " date in future: " + createTime);
            }
        }
    }

}