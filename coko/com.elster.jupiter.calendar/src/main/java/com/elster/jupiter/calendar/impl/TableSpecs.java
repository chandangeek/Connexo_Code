/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.calendar.Status;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INTNULLZERO;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Version.version;

/**
 * Created by igh on 18/04/2016.
 */
public enum TableSpecs {
    CAL_CATEGORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Category> table = dataModel.addTable(name(), Category.class);
            table.since(version(10, 2));
            table.map(CategoryImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar().notNull().map(CategoryImpl.Fields.NAME.fieldName()).add();
            table.primaryKey("CAL_PK_CATEGORY").on(idColumn).add();
        }
    },
    CAL_EVENTSET {
        @Override
        public void addTo(DataModel dataModel) {
            Table<EventSet> table = dataModel.addTable(name(), EventSet.class);
            table.since(version(10, 3));
            table.map(EventSetImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar().notNull().map(EventSetImpl.Fields.NAME.fieldName()).add();
            table.primaryKey("CAL_PK_EVENTSET").on(idColumn).add();
        }
    },
    CAL_CALENDAR {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Calendar> table = dataModel.addTable(name(), Calendar.class);
            table.since(version(10, 2));
            table.map(CalendarImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar().notNull().map(CalendarImpl.Fields.NAME.fieldName()).add();
            Column mRIDColumn = table.column("MRID").varChar().map(CalendarImpl.Fields.MRID.fieldName()).add();
            table.column("DESCRIPTION").varChar().map(CalendarImpl.Fields.DESCRIPTION.fieldName()).add();
            table.column("STARTYEAR").number().notNull().conversion(ColumnConversion.NUMBER2INT).map(CalendarImpl.Fields.STARTYEAR.fieldName()).add();
            table.column("ENDYEAR").number().conversion(ColumnConversion.NUMBER2INT).map(CalendarImpl.Fields.ENDYEAR.fieldName()).add();
            table.column("ABSTRACT_CALENDAR").bool().notNull().map(CalendarImpl.Fields.ABSTRACT_CALENDAR.fieldName()).add();
            table.column("TIMEZONENAME").varChar(80).upTo(version(10, 3)).add();
            Column categoryColumn = table.column(CalendarImpl.Fields.CATEGORY.fieldName()).number().notNull().add();
            table.column("STATUS").number().notNull().map(CalendarImpl.Fields.STATUS.fieldName()).conversion(ColumnConversion.NUMBER2ENUM).since(Version.version(10, 3)).installValue(String.valueOf(Status.INACTIVE.ordinal())).add();
            Column eventSetColumn = table.column("EVENTSET").number().notNull().since(version(10, 3)).add();
            Column obsoleteTime = table.column("OBSOLETETIME")
                    .number()
                    .map(CalendarImpl.Fields.OBSOLETETIME.fieldName())
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .since(version(10, 3))
                    .add();
            table.setJournalTableName("CAL_CALENDARJRNL");
            table.addAuditColumns();
            table.primaryKey("CAL_PK_CALENDAR").on(idColumn).add();
            table.unique("CAL_U_CALENDAR_MRID").on(mRIDColumn).upTo(version(10, 3)).add();
            table.unique("CAL_U_CALENDAR_MRID").on(mRIDColumn, obsoleteTime).since(version(10, 3)).add();
            table.unique("CAL_U_CALENDAR_NAME").on(nameColumn, obsoleteTime).since(version(10, 3)).add();
            table.foreignKey("CAL_CALENDAR_TO_CATEGORY")
                    .references(Category.class)
                    .on(categoryColumn)
                    .map(CalendarImpl.Fields.CATEGORY.fieldName())
                    .add();
            table.foreignKey("CAL_CAL_TO_EVENTSET")
                    .references(EventSet.class)
                    .on(eventSetColumn)
                    .map(CalendarImpl.Fields.EVENTSET.fieldName())
                    .add();
        }
    },
    CAL_EVENT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Event> table = dataModel.addTable(name(), Event.class);
            table.since(version(10, 2));
            table.map(EventImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar().notNull().map(EventImpl.Fields.NAME.fieldName()).add();
            table.column("CODE").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(EventImpl.Fields.CODE.fieldName()).add();
            Column calendarColumn = table.column("calendar").number().notNull().upTo(version(10, 3)).add();
            Column eventSetColumn = table.column(EventImpl.Fields.EVENTSET.fieldName()).number().notNull().since(version(10, 3)).add();
            table.setJournalTableName("CAL_EVENTJRNL");
            table.addAuditColumns();
            table.primaryKey("CAL_PK_EVENT").on(idColumn).add();
            table.foreignKey("CAL_EVENT_TO_CALENDAR")
                    .upTo(version(10, 3))
                    .references(Calendar.class)
                    .map("calendar")
                    .on(calendarColumn)
                    .add();
            table.foreignKey("CAL_EVENT_TO_SET")
                    .since(version(10, 3))
                    .references(EventSet.class)
                    .on(eventSetColumn)
                    .map(EventImpl.Fields.EVENTSET.fieldName())
                    .reverseMap(EventSetImpl.Fields.EVENTS.fieldName())
                    .add();
        }
    },
    CAL_EVENT_OCCURRENCE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<EventOccurrence> table = dataModel.addTable(name(), EventOccurrence.class);
            table.since(version(10, 2));
            table.map(EventOccurrenceImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("HOURS").number().notNull().conversion(ColumnConversion.NUMBER2INT).map(EventOccurrenceImpl.Fields.HOURS.fieldName()).add();
            table.column("MINUTES").number().notNull().conversion(ColumnConversion.NUMBER2INT).map(EventOccurrenceImpl.Fields.MINUTES.fieldName()).add();
            table.column("SECONDS").number().notNull().conversion(ColumnConversion.NUMBER2INT).map(EventOccurrenceImpl.Fields.SECONDS.fieldName()).add();
            Column event = table.column(EventOccurrenceImpl.Fields.EVENT.fieldName()).number().notNull().add();
            table.setJournalTableName("CAL_EVENT_OCCURRENCEJRNL");
            table.addAuditColumns();
            table.primaryKey("CAL_PK_EVT_OCC").on(id).add();
            table.foreignKey("CAL_EVT_OCC_TO_EVT")
                    .references(Event.class)
                    .on(event)
                    .map(EventOccurrenceImpl.Fields.EVENT.fieldName())
                    .add();
        }
    },
    CAL_DAYTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DayType> table = dataModel.addTable(name(), DayType.class);
            table.since(version(10, 2));
            table.map(DayTypeImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar().notNull().map(DayTypeImpl.Fields.NAME.fieldName()).add();
            Column calendarColumn = table.column("calendar").number().notNull().add();
            table.primaryKey("CAL_PK_DAYTYPE").on(idColumn).add();
            table.setJournalTableName("CAL_DAYTYPEJRNL");
            table.addAuditColumns();
            table.foreignKey("CAL_DAYTYPE_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .map(DayTypeImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.DAYTYPES.fieldName())
                    .add();
        }
    },
    ADD_EVT_OCC_TO_DAYTYPE_DEPENDENCY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<?> table = dataModel.getTable(CAL_EVENT_OCCURRENCE.name());
            Column dayTypeColumn = table.column(EventOccurrenceImpl.Fields.DAYTYPE.fieldName()).number().notNull().add();
            table.foreignKey("CAL_EVT_OCC_DAYTYPE")
                    .references(DayType.class)
                    .on(dayTypeColumn)
                    .map(EventOccurrenceImpl.Fields.DAYTYPE.fieldName())
                    .reverseMap(DayTypeImpl.Fields.EVENT_OCCURENCES.fieldName())
                    .composition()
                    .add();
        }
    },
    CAL_PERIOD {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Period> table = dataModel.addTable(name(), Period.class);
            table.since(version(10, 2));
            table.map(PeriodImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar().notNull().map(PeriodImpl.Fields.NAME.fieldName()).add();
            Column calendarColumn = table.column("calendar").number().notNull().add();

            Column mondayColumn = table.column(PeriodImpl.Fields.MONDAY.fieldName()).number().notNull().add();
            Column tuesdayColumn = table.column(PeriodImpl.Fields.TUESDAY.fieldName()).number().notNull().add();
            Column wednesdayColumn = table.column(PeriodImpl.Fields.WEDNESDAY.fieldName()).number().notNull().add();
            Column thursdayColumn = table.column(PeriodImpl.Fields.THURSDAY.fieldName()).number().notNull().add();
            Column fridayColumn = table.column(PeriodImpl.Fields.FRIDAY.fieldName()).number().notNull().add();
            Column saturdayColumn = table.column(PeriodImpl.Fields.SATURDAY.fieldName()).number().notNull().add();
            Column sundayColumn = table.column(PeriodImpl.Fields.SUNDAY.fieldName()).number().notNull().add();

            table.addAuditColumns();
            table.primaryKey("CAL_PK_PERIOD").on(idColumn).add();
            table.foreignKey("CAL_PERIOD_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(PeriodImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.PERIODS.fieldName())
                    //.composition()
                    .add();

            table.foreignKey("CAL_PERIOD_TO_MONDAY")
                    .references(DayType.class)
                    .on(mondayColumn)
                    .map(PeriodImpl.Fields.MONDAY.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_TUESDAY")
                    .references(DayType.class)
                    .on(tuesdayColumn)
                    .map(PeriodImpl.Fields.TUESDAY.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_WEDNESDAY")
                    .references(DayType.class)
                    .on(wednesdayColumn)
                    .map(PeriodImpl.Fields.WEDNESDAY.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_THURSDAY")
                    .references(DayType.class)
                    .on(thursdayColumn)
                    .map(PeriodImpl.Fields.THURSDAY.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_FRIDAY")
                    .references(DayType.class)
                    .on(fridayColumn)
                    .map(PeriodImpl.Fields.FRIDAY.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_SATURDAY")
                    .references(DayType.class)
                    .on(saturdayColumn)
                    .map(PeriodImpl.Fields.SATURDAY.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_SUNDAY")
                    .references(DayType.class)
                    .on(sundayColumn)
                    .map(PeriodImpl.Fields.SUNDAY.fieldName())
                    .add();
        }
    },
    CAL_EXCEPTIONAL_OCC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ExceptionalOccurrence> table = dataModel.addTable(name(), ExceptionalOccurrence.class);
            table.since(version(10, 2));
            table.map(ExceptionalOccurrenceImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();

            table.addDiscriminatorColumn("EXC_OCC_TYPE", "char(3)");

            Column calendarColumn = table.column("calendar").number().notNull().add();

            table.column("DAY").number().notNull().conversion(NUMBER2INTNULLZERO).map(ExceptionalOccurrenceImpl.Fields.DAY.fieldName()).add();
            table.column("MONTH").number().notNull().conversion(NUMBER2INTNULLZERO).map(ExceptionalOccurrenceImpl.Fields.MONTH.fieldName()).add();
            table.column("YEAR").number().conversion(NUMBER2INTNULLZERO).map(ExceptionalOccurrenceImpl.Fields.YEAR.fieldName()).add();

            Column dayTypeColumn = table.column(ExceptionalOccurrenceImpl.Fields.DAYTYPE.fieldName()).number().notNull().add();

            table.primaryKey("CAL_PK_EXC_OCC").on(idColumn).add();
            table.foreignKey("CAL_EXC_OCC_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(ExceptionalOccurrenceImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.EXCEPTIONAL_OCCURRENCES.fieldName())
                    //.composition()
                    .add();
            table.foreignKey("CAL_EXC_OCC_TO_DAYTYPE")
                    .references(DayType.class)
                    .on(dayTypeColumn)
                    .map(ExceptionalOccurrenceImpl.Fields.DAYTYPE.fieldName())
                    .add();
        }
    },
    CAL_PERIOD_TRANSITION_SPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<PeriodTransitionSpec> table = dataModel.addTable(name(), PeriodTransitionSpec.class);
            table.since(version(10, 2));
            table.map(PeriodTransitionSpecImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();

            table.addDiscriminatorColumn("PERIOD_TRANSITION_TYPE", "char(3)");

            Column calendarColumn = table.column("calendar").number().notNull().add();

            table.column("DAY").number().notNull().conversion(NUMBER2INTNULLZERO).map(PeriodTransitionSpecImpl.Fields.DAY.fieldName()).add();
            table.column("MONTH").number().notNull().conversion(NUMBER2INTNULLZERO).map(PeriodTransitionSpecImpl.Fields.MONTH.fieldName()).add();
            table.column("YEAR").number().conversion(NUMBER2INTNULLZERO).map(PeriodTransitionSpecImpl.Fields.YEAR.fieldName()).add();

            Column periodColumn = table.column(PeriodTransitionSpecImpl.Fields.PERIOD.fieldName()).number().notNull().add();

            table.primaryKey("CAL_PK_PERIOD_TRANS_SPEC").on(idColumn).add();
            table.foreignKey("CAL_PERIOD_TS_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(PeriodTransitionSpecImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.PERIOD_TRANSITION_SPECS.fieldName())
                    //.composition()
                    .add();

            table.foreignKey("CAL_PERIOD_TS_TO_PERIOD")
                    .references(Period.class)
                    .on(periodColumn)
                    .map(PeriodTransitionSpecImpl.Fields.PERIOD.fieldName())
                    .add();
        }
    },

    ;

    public abstract void addTo(DataModel component);
}
