package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import static com.elster.jupiter.orm.Version.version;

import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleTemplate;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 */

public enum TableSpecs {


    CLR_COMMANDRULETEMPLATE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<CommandRuleTemplate> table = dataModel.addTable(name(), CommandRuleTemplate.class);
            table.since(version(10, 3));
            table.map(CommandRuleTemplateImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.addAuditColumns();
            table.setJournalTableName("CLR_CMDRULETEMPLATEJRNL");
            table.column("NAME").varChar(Table.NAME_LENGTH).notNull().map(CommandRuleTemplateImpl.Fields.NAME.fieldName()).add();
            table.column("DAYLIMIT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleTemplateImpl.Fields.DAYLIMIT.fieldName()).add();
            table.column("WEEKLIMIT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleTemplateImpl.Fields.WEEKLIMIT.fieldName()).add();
            table.column("MONTHLIMIT").number().notNull().conversion(ColumnConversion.NUMBER2LONG).map(CommandRuleTemplateImpl.Fields.MONTHLIMIT.fieldName()).add();

            table.primaryKey("PK_CLR_CMDRULETEMPLATE").on(idColumn).add();
        }
    },

    CLR_COMMANDRULE {
        @Override
        public void addTo(DataModel dataModel) {
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
            Column commandRuleTemplate = table.column("COMMANDRULETEMPLATEID").number().add();

            table.primaryKey("PK_CLR_COMMANDRULE").on(idColumn).add();
            table.unique("CLR_U_COMMANDRULE_NAME").on(nameColumn).add();

            table.foreignKey("FK_CLR_COMMAND_RULE_TAMPLATE")
                    .on(commandRuleTemplate)
                    .references(CommandRuleTemplate.class)
                    .map(CommandRuleImpl.Fields.COMMANDRULETEMPLATE.fieldName())
                    .add();
        }
    },

    CLR_COMMANDINRULE {
        @Override
        void addTo(DataModel dataModel) {
            Table<CommandInRule> table = dataModel.addTable(name(), CommandInRule.class);
            table.since(version(10,3));
            table.map(CommandInRuleImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column("COMMAND").number().conversion(ColumnConversion.NUMBER2INT).map(CommandInRuleImpl.Fields.COMMANDID.fieldName()).notNull().add();
            Column commandRule = table.column("COMMANDRULEID").number().notNull().add();
            Column commandRuleTemplate = table.column("COMMANDRULETEMPLATEID").number().add();

            table.primaryKey("PK_CLR_CMDINRULE").on(idColumn).add();
            table.foreignKey("FK_CLR_CMDINRULE_CMDRULE").
                    on(commandRule)
                    .references(CLR_COMMANDRULE.name())
                    .map(CommandInRuleImpl.Fields.COMMANDRULE.fieldName())
                    .reverseMap(CommandRuleImpl.Fields.COMMANDS.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_CLR_CMDINRULE_CMDRULETMPLTE").
                    on(commandRuleTemplate)
                    .references(CLR_COMMANDRULETEMPLATE.name())
                    .map(CommandInRuleImpl.Fields.COMMANDRULETEMPLATE.fieldName())
                    .reverseMap(CommandRuleTemplateImpl.Fields.COMMANDS.fieldName())
                    .composition()
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);
}
