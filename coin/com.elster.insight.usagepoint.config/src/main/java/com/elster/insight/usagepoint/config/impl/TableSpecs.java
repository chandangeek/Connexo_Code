package com.elster.insight.usagepoint.config.impl;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationRuleSet;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
    UPC_MCVALRULESETUSAGE {
        void addTo(DataModel dataModel) {
            Table<MetrologyConfigurationValidationRuleSetUsage> table = dataModel
                    .addTable(name(), MetrologyConfigurationValidationRuleSetUsage.class);
            table.map(MetrologyConfigurationValidationRuleSetUsageImpl.class);
            table.setJournalTableName("UPC_MCVALRULESETUSAGEJRNL");
            Column validationRuleSetIdColumn = table
                    .column("VALIDATIONRULESETID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            Column metrologyConfigurationIdColumn = table
                    .column("METROLOGYCONFIGID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();

            table.primaryKey("UPC_PK_SETCONFIGUSAGE")
                    .on(validationRuleSetIdColumn, metrologyConfigurationIdColumn)
                    .add();
            table.foreignKey("UPC_FK_RULESET")
                    .references(ValidationRuleSet.class)
                    .onDelete(RESTRICT)
                    .map(MetrologyConfigurationValidationRuleSetUsageImpl.Fields.VALIDATION_RULE_SET.fieldName())
                    .on(validationRuleSetIdColumn)
                    .add();
            table.foreignKey("UPC_FK_METROLOGYCONFIG")
                    .references(MetrologyConfiguration.class)
                    .map(MetrologyConfigurationValidationRuleSetUsageImpl.Fields.METROLOGY_CONFIGURATION.fieldName())
                    .on(metrologyConfigurationIdColumn)
                    .add();
        }
    };

    abstract void addTo(DataModel component);

}