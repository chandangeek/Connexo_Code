/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.time.Instant;

class ActiveEffectiveCalendarImpl implements ServerActiveEffectiveCalendar {

    public enum Fields {
        CALENDAR("allowedCalendar"),
        DEVICE("device"),
        INTERVAL("interval"),
        LASTVERIFIEDDATE("lastVerifiedDate");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private DataModel dataModel;
    @IsPresent
    private Reference<AllowedCalendar> allowedCalendar = ValueReference.absent();
    @IsPresent
    private Reference<Device> device = ValueReference.absent();
    private Interval interval;
    private Instant lastVerifiedDate;

    @Inject
    ActiveEffectiveCalendarImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public ServerActiveEffectiveCalendar initialize(Interval effectivityInterval, Device device, AllowedCalendar allowedCalendar, Instant lastVerified) {
        setDevice(device);
        setInterval(effectivityInterval);
        setAllowedCalendar(allowedCalendar);
        setLastVerifiedDate(lastVerified);
        return this;
    }

    @Override
    public AllowedCalendar getAllowedCalendar() {
        return this.allowedCalendar.orNull();
    }

    public void setAllowedCalendar(AllowedCalendar allowedCalendar) {
        this.allowedCalendar.set(allowedCalendar);
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    @Override
    public Instant getLastVerifiedDate() {
        return lastVerifiedDate;
    }

    public void setLastVerifiedDate(Instant lastVerifiedDate) {
        this.lastVerifiedDate = lastVerifiedDate;
    }

    @Override
    public void updateLastVerifiedDate(Instant lastVerifiedDate) {
        this.setLastVerifiedDate(lastVerifiedDate);
        this.dataModel.update(this);
    }

    public void setDevice(Device device) {
        this.device.set(device);
    }

    @Override
    public void close(Instant closingDate) {
        if (!isEffectiveAt(closingDate)) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(closingDate);
        this.dataModel.update(this);
    }

}