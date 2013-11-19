package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.AssociationMapping;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

public enum TableSpecs {

    VAL_VALIDATIONRULESET {
        void describeTable(Table table) {
            table.setJournalTableName("VAL_VALIDATIONRULESETJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column mRIDColumn = table.addColumn("MRID", "varchar2(80)", false, NOCONVERSION, "mRID");
            table.addColumn("NAME", "varchar2(80)", false, NOCONVERSION, "name");
            table.addColumn("ALIASNAME", "varchar2(80)", false, NOCONVERSION, "aliasName");
            table.addColumn("DESCRIPTION", "varchar2(256)", false, NOCONVERSION, "description");
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
            table.addColumn("IMPLEMENTATION", "varchar2(80)", false, NOCONVERSION, "implementation");
            Column ruleSetIdColumn = table.addColumn("RULESETID", "number", true, NUMBER2LONG, "ruleSetId");
            table.addColumn("POSITION", "number", true, NUMBER2INT, "position");
            table.addPrimaryKeyConstraint("VAL_PK_VALIDATIONRULE", idColumn);
            table.addForeignKeyConstraint("VAL_FK_RULE", "VAL_VALIDATIONRULESET", CASCADE, new AssociationMapping("ruleSet", "rules", "position"), ruleSetIdColumn);
        }
    },
    VAL_VALIDATIONRULEPROPS {
        @Override
        void describeTable(Table table) {
            Column nameColumn = table.addColumn("NAME", "varchar2(80)", true, NOCONVERSION, "name");
            table.addColumn("VALUE", "number", true, NOCONVERSION, "value");
            Column ruleIdColumn = table.addColumn("RULEID", "number", true, NUMBER2LONG, "ruleId");
            table.addPrimaryKeyConstraint("VAL_PK_VALRULEPROPS", ruleIdColumn, nameColumn);
            table.addForeignKeyConstraint("VAL_FK_RULEPROPS", "VAL_VALIDATIONRULE", CASCADE, new AssociationMapping("rule"), ruleIdColumn);
        }
    },
    VAL_MA_VALIDATION {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addColumn("ID", "number", true, NUMBER2LONG, "id");
            Column ruleSetIdColumn = table.addColumn("RULESETID", "number", false, NUMBER2LONG, "ruleSetId");
            table.addColumn("LASTRUN", "number", false, NUMBER2UTCINSTANT, "lastRun");
            table.addPrimaryKeyConstraint("VAL_PK_MA_VALIDATION", idColumn);
            table.addForeignKeyConstraint("VAL_FK_MA_VALIDATION_MA", "MTR", "MTR_METERACTIVATION", DeleteRule.RESTRICT, "meterActivation", idColumn);
            table.addForeignKeyConstraint("VAL_FK_MA_VALIDATION_VRS", VAL_VALIDATIONRULESET.name(), DeleteRule.RESTRICT, new AssociationMapping("ruleSet"), ruleSetIdColumn);
        }
    },
    VAL_CH_VALIDATION {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addColumn("ID", "number", true, NUMBER2LONG, "id");
            Column meterActivationValidationColumn = table.addColumn("MAV_ID", "number", false, NUMBER2LONG, "meterActivationValidationId");
            table.addColumn("LASTCHECKED", "number", false, NUMBER2UTCINSTANT, "lastChecked");
            table.addPrimaryKeyConstraint("VAL_PK_CH_VALIDATION", idColumn);
            table.addForeignKeyConstraint("VAL_FK_CH_VALIDATION_CH", "MTR", "MTR_CHANNEL", DeleteRule.RESTRICT, "channel", idColumn);
            table.addForeignKeyConstraint("VAL_FK_CH_VALIDATION_MA_VAL", VAL_MA_VALIDATION.name(), DeleteRule.CASCADE, new AssociationMapping("meterActivationValidation"), meterActivationValidationColumn);
        }
    },

    VAL_READINGTYPEINVALRULE {
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            Column ruleIdColumn = table.addColumn("RULEID", "number", true, NUMBER2LONG, "ruleId");
            Column readingTypeMRIDColumn = table.addColumn("READINGTYPEMRID","varchar2(80)",true,NOCONVERSION,"readingTypeMRID");
            table.addPrimaryKeyConstraint("VAL_PK_RTYPEINVALRULE", idColumn);
            table.addForeignKeyConstraint("VAL_FK_RTYPEINVALRULE_RULE", VAL_VALIDATIONRULE.name(), DeleteRule.CASCADE, new AssociationMapping("rule", "readingTypesInRule"),ruleIdColumn);
            table.addForeignKeyConstraint("VAL_FK_RTYPEINVALRULE_RTYPE", "MTR", "MTR_READINGTYPE", DeleteRule.RESTRICT, "readingType",readingTypeMRIDColumn);
        }
    };


    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);

}