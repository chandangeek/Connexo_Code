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
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.impl.configchange.ServerLogBookForConfigChange;
import com.energyict.mdc.masterdata.LogBookType;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LogBookImpl implements ServerLogBookForConfigChange {

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
    private Instant lastEventOccurrence;    // last event time stamp (data unitil)
    private Instant latestEventAddition;    // last reading (next reading block start)
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

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
    public Optional<Instant> getLastLogBook() {
        return Optional.ofNullable(this.lastEventOccurrence);
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
        this.dataModel.update(this, "logBookSpec");
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
            }
            return this;
        }

        @Override
        public LogBook.LogBookUpdater setLastReadingIfLater(Instant createTime) {
            Instant logBookCreateTime = this.logBook.latestEventAddition;
            if (createTime != null && (logBookCreateTime == null || createTime.isAfter(logBookCreateTime))) {
                this.logBook.latestEventAddition = createTime;
            }
            return this;

        }

        @Override
        public LogBook.LogBookUpdater setLastReading(Instant createTime) {
            this.logBook.latestEventAddition = createTime;
            return this;
        }

        @Override
        public void update() {
            this.logBook.update();
        }
    }

}