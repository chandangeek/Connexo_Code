package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {

    VAL_VALIDATIONRULESET {
        void describeTable(Table table) {
            table.setJournalTableName("VAL_VALIDATIONRULESETJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
            table.column("NAME").type("varchar2(80)").map("name").add();
            table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
            table.addAuditColumns();
            table.primaryKey("VAL_PK_VALIDATIONRULESET").on(idColumn).add();
            table.unique("VAL_U_VALIDATIONRULESET").on(mRIDColumn).add();
        }
    },
    VAL_VALIDATIONRULE {
        @Override
        void describeTable(Table table) {
            table.setJournalTableName("VAL_VALIDATIONRULEJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
            table.column("ACTION").type("number").notNull().conversion(NUMBER2ENUM).map("action").add();
            table.column("IMPLEMENTATION").type("varchar2(80)").map("implementation").add();
            Column ruleSetIdColumn = table.column("RULESETID").type("number").notNull().conversion(NUMBER2LONG).map("ruleSetId").add();
            table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
            table.primaryKey("VAL_PK_VALIDATIONRULE").on(idColumn).add();
            table.foreignKey("VAL_FK_RULE").references("VAL_VALIDATIONRULESET").onDelete(CASCADE).map("ruleSet").reverseMap("rules").reverseMapOrder("position").on(ruleSetIdColumn).add();
        }
    },
    VAL_VALIDATIONRULEPROPS {
        @Override
        void describeTable(Table table) {
            Column nameColumn = table.column("NAME").type("varchar2(80)").notNull().map("name").add();
            table.addQuantityColumns("VALUE", true, "value");
            Column ruleIdColumn = table.column("RULEID").type("number").notNull().conversion(NUMBER2LONG).map("ruleId").add();
            table.primaryKey("VAL_PK_VALRULEPROPS").on(ruleIdColumn, nameColumn).add();
            table.foreignKey("VAL_FK_RULEPROPS").references("VAL_VALIDATIONRULE").onDelete(CASCADE).map("rule").on(ruleIdColumn).add();
        }
    },
    VAL_MA_VALIDATION {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.column("ID").type("number").notNull().conversion(NUMBER2LONG).map("id").add();
            Column ruleSetIdColumn = table.addColumn("RULESETID", "number", false, NUMBER2LONG, "ruleSetId");
            table.addColumn("LASTRUN", "number", false, NUMBER2UTCINSTANT, "lastRun");
            table.primaryKey("VAL_PK_MA_VALIDATION").on(idColumn).add();
            table.foreignKey("VAL_FK_MA_VALIDATION_MA").references("MTR", "MTR_METERACTIVATION").onDelete(RESTRICT).map("meterActivation").on(idColumn).add();
            table.foreignKey("VAL_FK_MA_VALIDATION_VRS").references(VAL_VALIDATIONRULESET.name()).onDelete(DeleteRule.RESTRICT).map("ruleSet").on(ruleSetIdColumn).add();
        }
    },
    VAL_CH_VALIDATION {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.column("ID").type("number").notNull().conversion(NUMBER2LONG).map("id").add();
            Column meterActivationValidationColumn = table.addColumn("MAV_ID", "number", false, NUMBER2LONG, "meterActivationValidationId");
            table.addColumn("LASTCHECKED", "number", false, NUMBER2UTCINSTANT, "lastChecked");
            table.primaryKey("VAL_PK_CH_VALIDATION").on(idColumn).add();
            table.foreignKey("VAL_FK_CH_VALIDATION_CH").references("MTR", "MTR_CHANNEL").onDelete(RESTRICT).map("channel").on(idColumn).add();
            table.foreignKey("VAL_FK_CH_VALIDATION_MA_VAL").references(VAL_MA_VALIDATION.name()).onDelete(DeleteRule.CASCADE).map("meterActivationValidation").on(meterActivationValidationColumn).add();
        }
    },

    VAL_READINGTYPEINVALRULE {
        void describeTable(Table table) {
            Column ruleIdColumn = table.column("RULEID").type("number").notNull().conversion(NUMBER2LONG).map("ruleId").add();
            Column readingTypeMRIDColumn = table.column("READINGTYPEMRID").type("varchar2(80)").notNull().map("readingTypeMRID").add();
            table.primaryKey("VAL_PK_RTYPEINVALRULE").on(ruleIdColumn, readingTypeMRIDColumn).add();
            table.foreignKey("VAL_FK_RTYPEINVALRULE_RULE").references(VAL_VALIDATIONRULE.name()).onDelete(DeleteRule.CASCADE).map("rule").reverseMap("readingTypesInRule").on(ruleIdColumn).add();
            table.foreignKey("VAL_FK_RTYPEINVALRULE_RTYPE").references("MTR", "MTR_READINGTYPE").onDelete(RESTRICT).map("readingType").on(readingTypeMRIDColumn);
        }
    };


    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);

}