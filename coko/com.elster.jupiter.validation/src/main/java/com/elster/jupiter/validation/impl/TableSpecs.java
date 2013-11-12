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
    },
    VAL_VALIDATIONRULE {
        @Override
        void describeTable(Table table) {
            table.setJournalTableName("VAL_VALIDATIONRULEJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.addColumn("ACTIVE", "char(1)", true, CHAR2BOOLEAN, "active");
            table.addColumn("ACTION", "number", true, NUMBER2ENUM, "action");
            table.addColumn("IMPLEMENTATION", "varchar2(80)", false, NOCONVERSION , "implementation");
            Column ruleSetIdColumn = table.addColumn("RULESETID", "number", true , NUMBER2LONG, "ruleSetId");
            table.addColumn("POSITION","number",true,NUMBER2INT,"position");
            table.addPrimaryKeyConstraint("VAL_PK_VALIDATIONRULE", idColumn);
            table.addForeignKeyConstraint("VAL_FK_RULE", "VAL_VALIDATIONRULESET", CASCADE , new AssociationMapping("ruleSet", "rules", "position"), ruleSetIdColumn);
        }
    };

    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);

}