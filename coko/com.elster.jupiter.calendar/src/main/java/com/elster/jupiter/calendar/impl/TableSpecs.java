package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

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
    };





    public abstract void addTo(DataModel component);
}
