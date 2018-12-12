/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.RelativePeriodCategoryUsage;

import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    TME_RELATIVEPERIOD() {
        @Override
        void addTo(DataModel dataModel) {
            Table<RelativePeriod> table = dataModel.addTable(name(), RelativePeriod.class);
            table.map(RelativePeriodImpl.class);
            table.setJournalTableName("TME_RELATIVEPERIODJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").map("name").varChar().notNull().add();
            table.column("START_DATE").map("from.relativeDate").varChar(256).notNull().add();
            table.column("END_DATE").map("to.relativeDate").varChar(256).notNull().add();
            table.addAuditColumns();

            table.primaryKey("TME_PK_RELATIVE_PERIOD").on(idColumn).add();
            table.unique("TME_UQ_R_PERIOD_NAME").on(nameColumn).add();
        }
    },
    TME_RELATIVEPERIODCATEGORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<RelativePeriodCategory> table = dataModel.addTable(name(), RelativePeriodCategory.class);
            table.map(RelativePeriodCategoryImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column name = table.column("NAME").map("name").varChar().notNull().add();
            table.addAuditColumns();
            table.unique("TME_UK_RELATIVEPERIODCATEGORY").on(name).add();
            table.primaryKey("TME_PK_RELATIVEPERIODCATEGORY").on(idColumn).add();
        }
    },
    TME_PERIODCATEGORYUSAGE() {
        @Override
        void addTo(DataModel dataModel) {
            Table<RelativePeriodCategoryUsage> table = dataModel.addTable(name(), RelativePeriodCategoryUsage.class);
            table.map(RelativePeriodCategoryUsageImpl.class);
            Column relativePeriodIdColumn = table.column("RELATIVEPERIODID").number().notNull().add();
            Column relativePeriodCategoryIdColumn = table.column("RELATIVEPERIODCATEGORYID").number().notNull().add();
            table.setJournalTableName("TME_PERIODCATEGORYUSAGEJRNL").since(version(10, 2));
            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table.primaryKey("TME_PK_CATEGORY_USAGE").on(relativePeriodIdColumn, relativePeriodCategoryIdColumn).add();
            table.foreignKey("TME_FK_RELATIVEPERIOD").
                    on(relativePeriodIdColumn).
                    references(TME_RELATIVEPERIOD.name()).
                    map("relativePeriod").
                    reverseMap("relativePeriodCategoryUsages").
                    composition().
                    add();
            table.foreignKey("TME_FK_RELATIVEPERIODCATEGORY").
                    on(relativePeriodCategoryIdColumn).
                    references(TME_RELATIVEPERIODCATEGORY.name()).
                    map("relativePeriodCategory").
                    add();
        }
    };

    abstract void addTo(DataModel component);

}