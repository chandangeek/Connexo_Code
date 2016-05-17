package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import javax.inject.Inject;

import java.time.Instant;
import java.util.Optional;

public class PassiveEffectiveCalendarImpl implements PassiveEffectiveCalendar{

    public enum Fields {
        ID("id"),
        CALENDAR("allowedCalendar"),
        ACTIVATIONDATE("activationDate"),
        DEVICE("device"),
        COMTASKEXECUTION("comTaskExecution");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private long id;

    private Reference<AllowedCalendar> allowedCalendar = ValueReference.absent();
    private Reference<Device> device = ValueReference.absent();
    private Instant activationDate;
    private Reference<ComTaskExecution> comTaskExecution = ValueReference.absent();

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

    public void setDevice(Device device) {
        this.device.set(device);
    }

    @Override
    public Optional<ComTaskExecution> getComTaskExecution() {
        return comTaskExecution.getOptional();
    }
}
