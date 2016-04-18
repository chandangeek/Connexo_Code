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
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Created by igh on 18/04/2016.
 */
public class CalendarImpl implements Calendar {

    public enum Fields {
        ID("id"),
        NAME("name"),
        MRID("mRID"),
        DESCRIPTION("description"),
        TIMEZONENAME("timeZoneName"),
        CATEGORY("category"),
        DAYTYPES("dayTypes");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }


    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String description;
    private String mRID;
    private String timeZoneName;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private TimeZone timeZone;
    private Reference<Category> category = ValueReference.absent();
    private List<DayType> dayTypes = new ArrayList<>();


    private final DataModel dataModel;
    private final CalendarService calendarService;

    @Inject
    CalendarImpl(CalendarService calendarService, DataModel dataModel) {
        this.calendarService = calendarService;
        this.dataModel = dataModel;
    }

    CalendarImpl init(String name, String description, TimeZone timeZone, Category category) {
        this.name = name;
        this.description = description;
        this.timeZone = timeZone;
        this.category.set(category);
        return this;
    }

    static CalendarImpl from(DataModel dataModel, String description, String name, TimeZone timeZone, Category category) {
        return dataModel.getInstance(CalendarImpl.class).init(name, description, timeZone, category);
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
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
        if (timeZone == null) {
            timeZone =  TimeZone.getTimeZone(ZoneId.of(timeZoneName));
        }
        return timeZone;
    }

    @Override
    public Category getCategory() {
        return this.category.orNull();
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

    @Override
    public void addDayType(String name) {
        DayTypeImpl dayType = dataModel.getInstance(DayTypeImpl.class).init(this, name);
        this.dayTypes.add(dayType);
    }


    @Override
    public void removeDayType(DayType dayType) {
        Objects.requireNonNull(dayType);
        dayTypes.stream()
                .filter(type -> dayType.getId() == type.getId())
                .findFirst()
                .ifPresent(this.dayTypes::remove);
    }



}
