package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Created by igh on 19/04/2016.
 */
public class EventImpl implements Event {

    public enum Fields {
        ID("id"),
        NAME("name"),
        CODE("code"),
        CALENDAR("calendar");

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.EVENT_NAME_FIELD_TOO_LONG + "}")
    private String name;
    private long code;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<Calendar> calendar = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private final ServerCalendarService calendarService;

    @Inject
    EventImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public EventImpl init(Calendar calendar, String name, long code) {
        this.name = name;
        this.calendar.set(calendar);
        this.code = code;
        return this;
    }

    @Override
    public long getCode() {
        return code;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Calendar getCalendar() {
        return this.calendar.orNull();
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void delete() {
        if (id == 0) {
            return;
        }
        calendarService.getDataModel().remove(this);
    }

    void save() {
        Save.CREATE.save(calendarService.getDataModel(), this, Save.Create.class);
    }


}
