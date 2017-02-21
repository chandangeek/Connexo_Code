/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Optional;

/**
 * @author sva
 * @since 7/04/2016 - 11:22
 */
public class ActivatedBreakerStatusImpl implements ActivatedBreakerStatus {

    public enum Fields {
        BREAKER_STATUS("breakerStatus"),
        DEVICE("device"),
        LAST_CHECKED("lastChecked"),
        INTERVAL("interval"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused")
    private long id;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private BreakerStatus breakerStatus;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<com.energyict.mdc.device.data.Device> device = ValueReference.absent();
    private Instant lastChecked;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Interval interval;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final EventService eventService;
    private final DeviceService deviceDataService;

    @Inject
    public ActivatedBreakerStatusImpl(DataModel dataModel, EventService eventService, DeviceService deviceDataService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.deviceDataService = deviceDataService;
    }

    public ActivatedBreakerStatus init(Device device, BreakerStatus breakerStatus, Interval interval) {
        setDevice(device);
        setBreakerStatus(breakerStatus);
        this.interval = interval;
        return this;
    }

    public static ActivatedBreakerStatus from(DataModel dataModel, Device device, BreakerStatus breakerStatus, Interval interval) {
        return dataModel.getInstance(ActivatedBreakerStatusImpl.class).init(device, breakerStatus, interval);
    }

    @Override
    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    private void doPersist() {
        Save.CREATE.validate(dataModel, this);
        getActivatedBreakerStatus().ifPresent(activatedBreakerStatus -> {
            ((ActivatedBreakerStatusImpl) activatedBreakerStatus).expiredAt(this.getInterval().getStart());
        });
        dataModel.persist(this);
        notifyCreated();
    }

    private Optional<ActivatedBreakerStatus> getActivatedBreakerStatus() {
        return this.deviceDataService.getActiveBreakerStatus(this.getDevice());
    }

    private void expiredAt(Instant end) {
        if (this.isEffectiveAt(end)) {
            this.interval = this.interval.withEnd(end);
            dataModel.update(this);
        }
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        notifyUpdated();
    }

    private void notifyCreated() {
        this.eventService.postEvent(EventType.ACTIVATED_BREAKER_STATUS_CREATED.topic(), this);
    }

    private void notifyUpdated() {
        this.eventService.postEvent(EventType.ACTIVATED_BREAKER_STATUS_UPDATED.topic(), this);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Device getDevice() {
        return device.orNull();
    }

    public void setDevice(Device device) {
        this.device.set(device);
    }

    @Override
    public BreakerStatus getBreakerStatus() {
        return breakerStatus;
    }

    public void setBreakerStatus(BreakerStatus breakerStatus) {
        this.breakerStatus = breakerStatus;
    }

    @Override
    public Instant getLastChecked() {
        return this.lastChecked;
    }

    @Override
    public void setLastChecked(Instant lastChecked) {
        this.lastChecked = lastChecked;
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }
}