package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.orm.Column;
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
                    .composition()
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
            table.primaryKey("CAL_PK_DAYTYPE").on(idColumn).add();
            table.foreignKey("CAL_DAYTYPE_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(PeriodImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.PERIODS.fieldName())
                    .composition()
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

            table.primaryKey("CAL_PK_EXC_OCC").on(idColumn).add();
            table.foreignKey("CAL_EXC_OCC_TO_CALENDAR")
                    .references(Calendar.class)
                    .on(calendarColumn)
                    .onDelete(CASCADE)
                    .map(ExceptionalOccurrenceImpl.Fields.CALENDAR.fieldName())
                    .reverseMap(CalendarImpl.Fields.EXCEPTIONAL_OCCURRENCES.fieldName())
                    .composition()
                    .add();
        }
    }
    ;

    public abstract void addTo(DataModel component);
}
