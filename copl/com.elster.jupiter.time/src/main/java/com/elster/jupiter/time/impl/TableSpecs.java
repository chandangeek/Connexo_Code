package com.elster.jupiter.time.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.time.RelativePeriod;

public enum TableSpecs {
    TME_RELATIVEPERIOD() {
        @Override
        void addTo(DataModel dataModel) {
            Table<RelativePeriod> table = dataModel.addTable(name(), RelativePeriod.class);
            table.map(RelativePeriodImpl.class);
            table.setJournalTableName("TME_RELATIVEPERIODJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").map("name").varChar(80).notNull().add();
            table.column("FROM").map("from.relativeDate").varChar(256).notNull().add();
            table.column("TO").map("to.relativeDate").varChar(256).notNull().add();
            table.addAuditColumns();

            table.primaryKey("RELATIVE_PERIOD_PK_NAME").on(idColumn).add();
        }
    };

    abstract void addTo(DataModel component);
}
