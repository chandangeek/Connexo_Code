package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.Optional;

class AllowedCalendarImpl implements AllowedCalendar {
    public enum Fields {
        ID("id"),
        DEVICETYPE("deviceType"),
        CALENDAR("calendar"),
        NAME("name");

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
    @IsPresent
    private Reference<DeviceType> deviceType = ValueReference.absent();
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent
    private Reference<Calendar> calendar = ValueReference.absent();
    private DataModel dataModel;

    @Inject
    AllowedCalendarImpl (DataModel dataModel) {
        this.dataModel = dataModel;
    }

    AllowedCalendarImpl initialize(Calendar calendar, DeviceType deviceType) {
        this.calendar.set(calendar);
        this.deviceType.set(deviceType);
        return this;
    }

    AllowedCalendarImpl initialize(String ghostName, DeviceType deviceType) {
        this.name = ghostName;
        this.deviceType.set(deviceType);
        return this;
    }

    @Override
    public boolean isGhost() {
        return !calendar.isPresent();
    }

    @Override
    public String getName() {
        if (this.isGhost()) {
            return name;
        } else {
            return this.calendar.getOptional()
                    .get()
                    .getName();
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Optional<Calendar> getCalendar() {
        return this.calendar.getOptional();
    }

    void replaceGhostBy(Calendar newCalendar) {
        calendar.set(newCalendar);
        name = null;
        dataModel.update(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AllowedCalendarImpl that = (AllowedCalendarImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}