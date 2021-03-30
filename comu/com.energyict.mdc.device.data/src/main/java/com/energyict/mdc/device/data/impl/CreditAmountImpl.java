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
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

public class CreditAmountImpl implements CreditAmount {
    public enum Fields {
        CREDIT_TYPE("creditType"),
        CREDIT_AMOUNT("creditAmount"),
        DEVICE("device"),
        FIRST_CHECKED("firstChecked"),
        LAST_CHECKED("lastChecked");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private String creditType;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private BigDecimal creditAmount;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Instant firstChecked;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Instant lastChecked;

    private final DataModel dataModel;
    private final EventService eventService;
    private final DeviceService deviceService;
    private final Clock clock;

    @Inject
    public CreditAmountImpl(DataModel dataModel, EventService eventService, DeviceService deviceService, Clock clock) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.deviceService = deviceService;
        this.clock = clock;
    }

    public CreditAmount init(Device device, String creditType, BigDecimal creditAmount) {
        setDevice(device);
        setCreditType(creditType);
        setCreditAmount(creditAmount);
        return this;
    }

    public static CreditAmount from(DataModel dataModel, Device device, String creditType, BigDecimal creditAmount) {
        return dataModel.getInstance(CreditAmountImpl.class).init(device, creditType, creditAmount);
    }

    @Override
    public void save() {
        if (lastChecked == null) {
            lastChecked = clock.instant();
        }
        Optional<CreditAmount> previous = deviceService.getCreditAmount(getDevice(), lastChecked);
        Optional<CreditAmount> matchingPrevious = previous
                .filter(credit -> credit.matches(creditType, creditAmount));
        if (matchingPrevious.isPresent()) {
            firstChecked = matchingPrevious.get().getFirstChecked();
            lastChecked = max(lastChecked, matchingPrevious.get().getLastChecked());
            Save.UPDATE.save(dataModel, this);
        } else {
            firstChecked = lastChecked;
            Save.CREATE.save(dataModel, this);
        }
        this.eventService.postEvent(
                previous.isPresent() ? EventType.CREDIT_AMOUNT_UPDATED.topic() : EventType.CREDIT_AMOUNT_CREATED.topic(),
                this);
    }

    private static Instant max(Instant i1, Instant i2) {
        return i1.isBefore(i2) ? i2 : i1;
    }

    @Override
    public boolean matches(String type, BigDecimal amount) {
        return getCreditType().equalsIgnoreCase(type)
                && getCreditAmount().equals(amount);
    }

    @Override
    public Device getDevice() {
        return device.get();
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

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    @Override
    public Instant getFirstChecked() {
        return firstChecked;
    }

    @Override
    public Instant getLastChecked() {
        return lastChecked;
    }

    @Override
    public void setLastChecked(Instant lastChecked) {
        this.lastChecked = lastChecked;
    }
}
