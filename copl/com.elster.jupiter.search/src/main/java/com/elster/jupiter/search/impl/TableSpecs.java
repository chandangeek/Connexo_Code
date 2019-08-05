package com.elster.jupiter.search.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;/*
import com.elster.jupiter.search.impl.records.SearchCriteriaImpl;
import com.elster.jupiter.search.share.entity.SearchCriteria;
*/
import com.elster.jupiter.search.SearchCriteria;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    DYN_SEARCHCRITERIA {
        @Override
        public void addTo(DataModel dataModel) {
           Table<SearchCriteria> table = dataModel.addTable(name(), SearchCriteria.class);
            table.map(SearchCriteriaImpl.class);
            table.since(version(10, 7));
            table.cache();
            Column key = table.column("NAME").map("name").varChar(NAME_LENGTH).notNull().add();
            Column userName = table.column("USERNAME").map("userName").varChar(NAME_LENGTH).notNull().add();
            table.primaryKey("PK_DYN_SEARCHCRITERIA").on(key, userName).add();
            table.column("CRITERIA").map("criteria").varChar(NAME_LENGTH).notNull().add();
            table.column("DOMAIN").map("domain").varChar(NAME_LENGTH).notNull().add();
            table.addAutoIdColumn();

        }
    };

    public abstract void addTo(DataModel dataModel);
}
