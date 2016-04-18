package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Created by igh on 18/04/2016.
 */
public class CalendarImpl implements Calendar {

    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String description;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private final DataModel dataModel;
    private final CalendarService calendarService;

    @Inject
    CalendarImpl(CalendarService calendarService, DataModel dataModel) {
        this.calendarService = calendarService;
        this.dataModel = dataModel;
    }


    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
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

    @Override
    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Calendar)) {
            return false;
        }
        Calendar that = (Calendar) o;
        return id == that.getId();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void save() {
        if (this.getId() > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public TimeZone getTimeZone() {
        return null;
    }

    @Override
    public Category getCategory() {
        return null;
    }

    @Override
    public List<Event> getEvents() {
        return null;
    }

    @Override
    public List<Period> getPeriods() {
        return null;
    }

    @Override
    public List<DayType> getDayTypes() {
        return null;
    }

    @Override
    public List<ExceptionalOccurrence> getExceptionalOccurrences() {
        return null;
    }

    @Override
    public List<? extends PeriodTransitionSpec> getPeriodTransitionSpecs() {
        return null;
    }

    @Override
    public List<PeriodTransition> getTransitions() {
        return null;
    }
}
