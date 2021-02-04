/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public class CreditAmountImpl implements CreditAmount {
    public enum Fields {
        CREDIT_TYPE("creditType"),
        CREDIT_AMOUNT("creditAmount"),
        DEVICE("device"),
        LAST_CHECKED("lastChecked"),
        INTERVAL("interval");

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
    private String creditType;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private BigDecimal creditAmount;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
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
    public CreditAmountImpl(DataModel dataModel, EventService eventService, DeviceService deviceDataService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.deviceDataService = deviceDataService;
    }

    public CreditAmount init(Device device, String creditType, BigDecimal creditAmount, Interval interval) {
        setDevice(device);
        setCreditType(creditType);
        setCreditAmount(creditAmount);
        this.interval = interval;
        return this;
    }

    public static CreditAmount from(DataModel dataModel, Device device, String creditType, BigDecimal creditAmount, Interval interval) {
        return dataModel.getInstance(CreditAmountImpl.class).init(device, creditType, creditAmount, interval);
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
        dataModel.persist(this);
        notifyCreated();
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        notifyUpdated();
    }

    private void notifyCreated() {
        this.eventService.postEvent(EventType.CREDIT_AMOUNT_CREATED.topic(), this);
    }

    private void notifyUpdated() {
        this.eventService.postEvent(EventType.CREDIT_AMOUNT_UPDATED.topic(), this);
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
    public String getCreditType() {
        return creditType;
    }

    public void setCreditType(String creditType) {
        this.creditType = creditType;
    }

    @Override
    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditType) {
        this.creditAmount = creditAmount;
    }

    @Override
    public Instant getLastChecked() {
        return this.lastChecked;
    }

    @Override
    public void setLastChecked(Instant lastChecked) {
        this.lastChecked = lastChecked;
    }

}
