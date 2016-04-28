package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceType;
import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.Size;
import java.util.Optional;

public class AllowedCalendarImpl implements AllowedCalendar {
    public enum Fields {
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

    @IsPresent
    private Reference<DeviceType> deviceType = ValueReference.absent();
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    private Reference<Calendar> calendar;

    public AllowedCalendarImpl(Calendar calendar, DeviceType deviceType) {
        this.calendar.set(calendar);
        this.deviceType.set(deviceType);
    }

    @Override
    public boolean isGhost() {
        return false;
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
    public Optional<Calendar> getCalendar() {
        return this.calendar.getOptional();
    }
}
