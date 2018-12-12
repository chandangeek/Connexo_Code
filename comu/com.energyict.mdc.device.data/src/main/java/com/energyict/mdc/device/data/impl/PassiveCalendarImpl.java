/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.data.PassiveCalendar;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import java.time.Instant;
import java.util.Optional;

class PassiveCalendarImpl implements PassiveCalendar {

    public enum Fields {
        ID("id"),
        CALENDAR("allowedCalendar"),
        ACTIVATIONDATE("activationDate"),
        DEVICE("device"),
        DEVICEMESSAGE("deviceMessage");

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
    private Reference<AllowedCalendar> allowedCalendar = ValueReference.absent();
    private Instant activationDate;
    private Reference<DeviceMessage> deviceMessage = ValueReference.absent();

    @Override
    public AllowedCalendar getAllowedCalendar() {
        return this.allowedCalendar.orNull();
    }

    public void setAllowedCalendar(AllowedCalendar allowedCalendar) {
        this.allowedCalendar.set(allowedCalendar);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Instant getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Instant activationDate) {
        this.activationDate = activationDate;
    }

    @Override
    public Optional<DeviceMessage> getDeviceMessage() {
        return deviceMessage.getOptional();
    }

    @Override
    public void setDeviceMessage(DeviceMessage deviceMessage) {
        this.deviceMessage.set(deviceMessage);
    }

}