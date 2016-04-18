package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

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
            table.column("NAME")
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map("name")
                    .add();
            table.primaryKey("CAL_PK_CATEGORY").on(idColumn).add();
        }
    };


    public abstract void addTo(DataModel component);
}
