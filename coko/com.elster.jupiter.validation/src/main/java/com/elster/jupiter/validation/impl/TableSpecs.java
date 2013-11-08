package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.*;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.*;

public enum TableSpecs {

    VAL_VALIDATIONRULESET {
        void describeTable(Table table) {
            table.setJournalTableName("VAL_VALIDATIONRULESETJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column mRIDColumn = table.addColumn("MRID", "varchar2(80)", false, NOCONVERSION , "mRID");
            table.addColumn("NAME", "varchar2(80)", false, NOCONVERSION , "name");
            table.addColumn("ALIASNAME", "varchar2(80)", false, NOCONVERSION , "aliasName");
            table.addColumn("DESCRIPTION", "varchar2(256)", false, NOCONVERSION , "description");
            table.addAuditColumns();
            table.addPrimaryKeyConstraint("VAL_PK_VALIDATIONRULESET", idColumn);
            table.addUniqueConstraint("VAL_U_VALIDATIONRULESET", mRIDColumn);
        }
    };

    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);

}