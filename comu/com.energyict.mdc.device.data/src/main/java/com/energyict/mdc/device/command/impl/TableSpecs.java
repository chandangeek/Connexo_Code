/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Encrypter;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;
import com.energyict.mdc.device.command.ICommandRuleCounter;

import static com.elster.jupiter.orm.Version.version;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 */

public enum TableSpecs {


    CLR_CMDRULEPENDINGUPDATE {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<CommandRulePendingUpdate> table = dataModel.addTable(name(), CommandRulePendingUpdate.class);
            table.since(version(10, 3));
            table.map(CommandRulePendingUpdateImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.addAuditColumns();
            table.setJournalTableName("CLR_CMDRULEPENDINGUPDATE_JRNL");
            table.column("NAME").varChar(Table.NAME_LENGTH).notNull().map(CommandRulePendingUpdateImpl.Fields.NAME.fieldName()).add();
            table.column("DAYLIMIT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRulePendingUpdateImpl.Fields.DAYLIMIT.fieldName()).add();
            table.column("WEEKLIMIT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRulePendingUpdateImpl.Fields.WEEKLIMIT.fieldName()).add();
            table.column("MONTHLIMIT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRulePendingUpdateImpl.Fields.MONTHLIMIT.fieldName()).add();
            table.column("ACTIVE").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map(CommandRulePendingUpdateImpl.Fields.ACTIVE.fieldName()).add();
            table.column("ISACTIVATION").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map(CommandRulePendingUpdateImpl.Fields.ISACTIVATION.fieldName()).add();
            table.column("ISDEACTIVATION").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map(CommandRulePendingUpdateImpl.Fields.ISDEACTIVATION.fieldName()).add();
            table.column("ISREMOVAL").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map(CommandRulePendingUpdateImpl.Fields.ISREMOVAL.fieldName()).add();
            table.column("NUMBEROFCOMMANDS").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRulePendingUpdateImpl.Fields.NUMBEROFCOMMANDS.fieldName()).add();
            table.addMessageAuthenticationCodeColumn(encrypter);

            table.primaryKey("PK_CLR_CMDRULETPU").on(idColumn).add();
        }
    },

    CLR_COMMANDRULE {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<CommandRule> table = dataModel.addTable(name(), CommandRule.class);
            table.since(version(10, 3));
            table.map(CommandRuleImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.addAuditColumns();
            table.setJournalTableName("CLR_COMMANDRULEJRNL");
            Column nameColumn = table.column("NAME").varChar(Table.NAME_LENGTH).notNull().map(CommandRuleImpl.Fields.NAME.fieldName()).add();
            table.column("DAYLIMIT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleImpl.Fields.DAYLIMIT.fieldName()).add();
            table.column("WEEKLIMIT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleImpl.Fields.WEEKLIMIT.fieldName()).add();
            table.column("MONTHLIMIT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleImpl.Fields.MONTHLIMIT.fieldName()).add();
            table.column("ACTIVE").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map(CommandRuleImpl.Fields.ACTIVE.fieldName()).add();
            table.column("NUMBEROFCOMMANDS").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleImpl.Fields.NUMBEROFCOMMANDS.fieldName()).add();
            Column commandRuleTemplate = table.column("COMMANDRULETEMPLATEID").number().add();
            Column monitor = table.column("MONITOR").number().add();
            table.addMessageAuthenticationCodeColumn(encrypter);

            table.primaryKey("PK_CLR_COMMANDRULE").on(idColumn).add();
            table.unique("CLR_U_COMMANDRULE_NAME").on(nameColumn).add();

            table.foreignKey("FK_CLR_COMMAND_PU")
                    .on(commandRuleTemplate)
                    .references(CommandRulePendingUpdate.class)
                    .map(CommandRuleImpl.Fields.COMMANDRULETEMPLATE.fieldName())
                    .add();

            table.foreignKey("FK_CLR_COMMAND_RULE_MONITOR")
                    .on(monitor)
                    .references(Monitor.class)
                    .map(CommandRuleImpl.Fields.MONITOR.fieldName())
                    .add();
        }
    },

    CLR_COMMANDINRULE {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<CommandInRule> table = dataModel.addTable(name(), CommandInRule.class);
            table.since(version(10,3));
            table.map(CommandInRuleImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column("COMMAND").number().conversion(ColumnConversion.NUMBER2INT).map(CommandInRuleImpl.Fields.COMMANDID.fieldName()).notNull().add();
            Column commandRule = table.column("COMMANDRULEID").number().add();
            Column commandRuleTemplate = table.column("COMMANDRULETEMPLATEID").number().add();
            table.addMessageAuthenticationCodeColumn(encrypter);

            table.primaryKey("PK_CLR_CMDINRULE").on(idColumn).add();
            table.foreignKey("FK_CLR_CMDINRULE_CMDRULE").
                    on(commandRule)
                    .references(CLR_COMMANDRULE.name())
                    .map(CommandInRuleImpl.Fields.COMMANDRULE.fieldName())
                    .reverseMap(CommandRuleImpl.Fields.COMMANDS.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .composition()
                    .add();
            table.foreignKey("FK_CLR_CMDINRULE_CMDRULETMPLTE").
                    on(commandRuleTemplate)
                    .references(CLR_CMDRULEPENDINGUPDATE.name())
                    .map(CommandInRuleImpl.Fields.COMMANDRULEPENDINGUPDATE.fieldName())
                    .reverseMap(CommandRulePendingUpdateImpl.Fields.COMMANDS.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .composition()
                    .add();
        }
    },

    CLR_COMMAND_RULE_STATS {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<CommandRuleStats> table = dataModel.addTable(name(), CommandRuleStats.class);
            table.since(version(10,3));
            table.map(CommandRuleStats.class);

            Column idColumn = table.column("ID").number().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleStats.Fields.ID.fieldName()).notNull().add();
            table.column("NR_OF_COMMAND_RULES").number().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleStats.Fields.NR_OF_COMMAND_RULES.fieldName()).notNull().add();
            table.column("NR_OF_COUNTERS").number().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleStats.Fields.NR_OF_COUNTERS.fieldName()).notNull().add();
            table.addMessageAuthenticationCodeColumn(encrypter);

            table.primaryKey("PK_COMMANDRULESTATS").on(idColumn).add();
        }
    },

    CLR_COMMAND_RULE_COUNTER {
        @Override
        void addTo(DataModel dataModel,Encrypter encrypter) {
            Table<ICommandRuleCounter> table = dataModel.addTable(name(), ICommandRuleCounter.class);
            table.since(version(10,3));
            table.map(CommandRuleCounter.class);

            Column idColumn = table.addAutoIdColumn();
            table.column("FROMTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(CommandRuleCounter.Fields.FROM.fieldName()).notNull().add();
            table.column("TOTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(CommandRuleCounter.Fields.TO.fieldName()).notNull().add();
            table.column("COUNT").number().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleCounter.Fields.COUNT.fieldName()).notNull().add();
            Column commandRule = table.column("COMMANDRULEID").number().notNull().add();
            table.addMessageAuthenticationCodeColumn(encrypter);

            table.primaryKey("PK_CLR_CMDRULECOUNTER").on(idColumn).add();
            table.foreignKey("FK_CLR_COUNTER_RULE").
                    on(commandRule)
                    .references(CLR_COMMANDRULE.name())
                    .map(CommandRuleCounter.Fields.COMMANDRULE.fieldName())
                    .reverseMap(CommandRuleImpl.Fields.COUNTERS.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .composition()
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel, Encrypter encrypter);
}
