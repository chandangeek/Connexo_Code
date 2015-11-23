package com.elster.jupiter.time.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.RelativePeriodCategoryUsage;

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
            //table.foreignKey("TME_FK_RELATIVEPERIODUSAGE").references("TME_RELATIVEPERIODCATEGORYUSAGE").map("relativePeriod").on(idColumn).add();
        }
    },
    TME_RELATIVEPERIODCATEGORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<RelativePeriodCategory> table = dataModel.addTable(name(), RelativePeriodCategory.class);
            table.map(RelativePeriodCategoryImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column name = table.column("NAME").map("name").varChar(80).notNull().add();
            table.addAuditColumns();
            table.unique("TME_UK_RELATIVEPERIODCATEGORY").on(name).add();
            table.primaryKey("TME_PK_RELATIVEPERIODCATEGORY").on(idColumn).add();
            //table.foreignKey("TME_FK_RELATIVEPERIODCATEGORYUSAGE").references("TME_RELATIVEPERIODCATEGORYUSAGE").map("relativePeriodCategory").on(idColumn).add();
        }
    },
    TME_PERIODCATEGORYUSAGE() {
        @Override
        void addTo(DataModel dataModel) {
            Table<RelativePeriodCategoryUsage> table = dataModel.addTable(name(), RelativePeriodCategoryUsage.class);
            table.map(RelativePeriodCategoryUsage.class);
            Column relativePeriodIdColumn = table.column("RELATIVEPERIODID").number().notNull().add();
            Column relativePeriodCategoryIdColumn = table.column("RELATIVEPERIODCATEGORYID").number().notNull().add();
            table.primaryKey("TME_PK_CATEGORY_USAGE").on(relativePeriodIdColumn, relativePeriodCategoryIdColumn).add();
            table.foreignKey("TME_FK_RELATIVEPERIOD").
                    on(relativePeriodIdColumn).
                    references(TME_RELATIVEPERIOD.name()).
                    map("relativePeriod").
                    reverseMap("relativePeriodCategoryUsages").
                    composition().
                    onDelete(DeleteRule.CASCADE).
                    add();
            table.foreignKey("TME_FK_RELATIVEPERIODCATEGORY").
                    on(relativePeriodCategoryIdColumn).
                    references(TME_RELATIVEPERIODCATEGORY.name()).
                    map("relativePeriodCategory").
                    add();
        }

/*        @Override
        void addTo(DataModel dataModel) {
            Table<RelativePeriodCategoryUsage> table = dataModel.addTable(name(), RelativePeriodCategoryUsage.class);
            table.map(RelativePeriodCategoryUsageImpl.class);
            table.setJournalTableName("DTC_RELATIVEPERIODCATEGORYUSAGEJRNL");
            Column relativePeriodIdColumn =
                    table.column("RELATIVEPERIODID").type("number").notNull().conversion(NUMBER2LONG).map("relativePeriodId").add();
            Column relativePeriodCategoryIdColumn =
                    table.column("RELATIVEPERIODCATEGORYID").type("number").notNull().conversion(NUMBER2LONG).map("relativePeriodCategoryId").add();

            table.primaryKey("TME_PK_RELATIVE_PERIOD_CATEGORY_USAGE").on(relativePeriodIdColumn, relativePeriodCategoryIdColumn).add();
            //table.foreignKey("TME_FK_RELATIVEPERIOD").references("TME_RELATIVEPERIOD").map("relativePeriod").on(relativePeriodIdColumn).add();
           // table.foreignKey("TME_FK_RELATIVEPERIODCATEGORY").references("TME_RELATIVEPERIODCATEGORY").map("relativePeriodCategory").on(relativePeriodCategoryIdColumn).add();
        }*/

    };


    abstract void addTo(DataModel component);
}
