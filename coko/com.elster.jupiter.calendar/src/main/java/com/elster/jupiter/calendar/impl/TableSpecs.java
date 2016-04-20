package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransitionSpec;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INTNULLZERO;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

/**
 * Created by igh on 18/04/2016.
 */
public enum TableSpecs {
    CAL_CATEGORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Category> table = dataModel.addTable(name(), Category.class);
            table.map(CategoryImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map(CategoryImpl.Fields.NAME.fieldName()).add();
            table.primaryKey("CAL_PK_CATEGORY").on(idColumn).add();
        }
    },
    CAL_CALENDAR {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Calendar> table = dataModel.addTable(name(), Calendar.class);
            table.map(CalendarImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map(CalendarImpl.Fields.NAME.fieldName()).add();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map(CalendarImpl.Fields.MRID.fieldName()).add();
            table.column("DESCRIPTION").varChar(NAME_LENGTH).map(CalendarImpl.Fields.DESCRIPTION.fieldName()).add();
            table.column("TIMEZONENAME").varChar(NAME_LENGTH).notNull(). map(CalendarImpl.Fields.TIMEZONENAME.fieldName()).add();
            Column categoryColumn = table.column(CalendarImpl.Fields.CATEGORY.fieldName()).number().notNull().add();
            table.addAuditColumns();
            table.primaryKey("CAL_PK_CALENDAR").on(idColumn).add();
            table.unique("CAL_U_CALENDAR_MRID").on(mRIDColumn).add();
            table.foreignKey("CAL_CALENDAR_TO_CATEGORY")
                    .references(Category.class)
                    .on(categoryColumn)
                    .map(CalendarImpl.Fields.CATEGORY.fieldName())
                    .add();
        }
    },
    CAL_DAYTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DayType> table = dataModel.addTable(name(), DayType.class);
            table.map(DayTypeImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map(DayTypeImpl.Fields.NAME.fieldName()).add();
            Column calendarColumn = table.column("calendar").number().notNull().add();
            table.primaryKey("CAL_PK_DAYTYPE").on(idColumn).add();
            table.addAuditColumns();
            table.foreignKey("CAL_DAYTYPE_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(DayTypeImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.DAYTYPES.fieldName())
                    .composition()
                    .add();
        }
    },
    CAL_PERIOD {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Period> table = dataModel.addTable(name(), Period.class);
            table.map(PeriodImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map(PeriodImpl.Fields.NAME.fieldName()).add();
            Column calendarColumn = table.column("calendar").number().notNull().add();

            Column mondayColumn = table.column(PeriodImpl.Fields.MONDAY_DAYTYPE.fieldName()).number().notNull().add();
            Column tuesdayColumn = table.column(PeriodImpl.Fields.TUESDAY_DAYTYPE.fieldName()).number().notNull().add();
            Column wednesdayColumn = table.column(PeriodImpl.Fields.WEDNESDAY_DAYTYPE.fieldName()).number().notNull().add();
            Column thursdayColumn = table.column(PeriodImpl.Fields.THURSDAY_DAYTYPE.fieldName()).number().notNull().add();
            Column fridayColumn = table.column(PeriodImpl.Fields.FRIDAY_DAYTYPE.fieldName()).number().notNull().add();
            Column saturdayColumn = table.column(PeriodImpl.Fields.SATURDAY_DAYTYPE.fieldName()).number().notNull().add();
            Column sundayColumn = table.column(PeriodImpl.Fields.SUNDAY_DAYTYPE.fieldName()).number().notNull().add();

            table.addAuditColumns();
            table.primaryKey("CAL_PK_PERIOD").on(idColumn).add();
            table.foreignKey("CAL_PERIOD_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(PeriodImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.PERIODS.fieldName())
                    .composition()
                    .add();

            table.foreignKey("CAL_PERIOD_TO_MONDAY")
                    .references(DayType.class)
                    .on(mondayColumn)
                    .map(PeriodImpl.Fields.MONDAY_DAYTYPE.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_TUESDAY")
                    .references(DayType.class)
                    .on(tuesdayColumn)
                    .map(PeriodImpl.Fields.TUESDAY_DAYTYPE.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_WEDNESDAY")
                    .references(DayType.class)
                    .on(wednesdayColumn)
                    .map(PeriodImpl.Fields.WEDNESDAY_DAYTYPE.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_THURSDAY")
                    .references(DayType.class)
                    .on(thursdayColumn)
                    .map(PeriodImpl.Fields.THURSDAY_DAYTYPE.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_FRIDAY")
                    .references(DayType.class)
                    .on(fridayColumn)
                    .map(PeriodImpl.Fields.FRIDAY_DAYTYPE.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_SATURDAY")
                    .references(DayType.class)
                    .on(saturdayColumn)
                    .map(PeriodImpl.Fields.SATURDAY_DAYTYPE.fieldName())
                    .add();
            table.foreignKey("CAL_PERIOD_TO_SUNDAY")
                    .references(DayType.class)
                    .on(sundayColumn)
                    .map(PeriodImpl.Fields.SUNDAY_DAYTYPE.fieldName())
                    .add();
        }
    }
    ,
    CAL_EXCEPTIONAL_OCC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ExceptionalOccurrence> table = dataModel.addTable(name(), ExceptionalOccurrence.class);
            table.map(ExceptionalOccurrenceImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            Column calendarColumn = table.column("calendar").number().notNull().add();

            table.column("DAY").type("number").notNull().conversion(NUMBER2INTNULLZERO).map(ExceptionalOccurrenceImpl.Fields.DAY.fieldName()).add();
            table.column("MONTH").type("number").notNull().conversion(NUMBER2INTNULLZERO).map(ExceptionalOccurrenceImpl.Fields.MONTH.fieldName()).add();
            table.column("YEAR").type("number").notNull().conversion(NUMBER2INTNULLZERO).map(ExceptionalOccurrenceImpl.Fields.YEAR.fieldName()).add();

            Column dayTypeColumn = table.column(ExceptionalOccurrenceImpl.Fields.DAYTYPE.fieldName()).number().notNull().add();

            table.primaryKey("CAL_PK_EXC_OCC").on(idColumn).add();
            table.foreignKey("CAL_EXC_OCC_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(ExceptionalOccurrenceImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.EXCEPTIONAL_OCCURRENCES.fieldName())
                    .composition()
                    .add();
            table.foreignKey("CAL_EXC_OCC_TO_DAYTYPE")
                    .references(DayType.class)
                    .map(ExceptionalOccurrenceImpl.Fields.DAYTYPE.fieldName())
                    .add();
        }
    },
    CAL_PERIOD_TRANSITION_SPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<PeriodTransitionSpec> table = dataModel.addTable(name(), PeriodTransitionSpec.class);
            table.map(PeriodTransitionSpecImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            Column calendarColumn = table.column("calendar").number().notNull().add();

            table.column("DAY").type("number").notNull().conversion(NUMBER2INTNULLZERO).map(PeriodTransitionSpecImpl.Fields.DAY.fieldName()).add();
            table.column("MONTH").type("number").notNull().conversion(NUMBER2INTNULLZERO).map(PeriodTransitionSpecImpl.Fields.MONTH.fieldName()).add();
            table.column("YEAR").type("number").notNull().conversion(NUMBER2INTNULLZERO).map(PeriodTransitionSpecImpl.Fields.YEAR.fieldName()).add();

            table.primaryKey("CAL_PK_PERIOD_TRANS_SPEC").on(idColumn).add();
            table.foreignKey("CAL_PERIOD_TS_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(PeriodTransitionSpecImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.PERIOD_TRANSITION_SPECS.fieldName())
                    .composition()
                    .add();
        }
    },
    CAL_EVENT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Event> table = dataModel.addTable(name(), Event.class);
            table.map(EventImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map(EventImpl.Fields.NAME.fieldName()).add();
            table.column("CODE").type("number").notNull().conversion(ColumnConversion.NUMBER2LONG).map(EventImpl.Fields.CODE.fieldName()).add();
            Column calendarColumn = table.column("calendar").number().notNull().add();
            table.addAuditColumns();
            table.primaryKey("CAL_PK_EVENT").on(idColumn).add();
            table.foreignKey("CAL_EVENT_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(EventImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.EVENTS.fieldName())
                    .composition()
                    .add();
        }
    },
    ;

    public abstract void addTo(DataModel component);
}
