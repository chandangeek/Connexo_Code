package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
    UPC_MCVALRULESETUSAGE {
        void addTo(DataModel dataModel) {
            Table<MetrologyConfigurationValidationRuleSetUsage> table = dataModel
                    .addTable(name(), MetrologyConfigurationValidationRuleSetUsage.class);
            table.map(MetrologyConfigurationValidationRuleSetUsageImpl.class);
            table.setJournalTableName("UPC_MCVALRULESETUSAGEJRNL");
            Column validationRule = table
                    .column("VALIDATIONRULESETID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            Column metrologyConfiguration = table
                    .column("METROLOGYCONFIGID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");

            table.primaryKey("UPC_PK_SETCONFIGUSAGE")
                    .on(validationRule, metrologyConfiguration, intervalColumns.get(0))
                    .add();
            table.foreignKey("UPC_FK_RULESET")
                    .references(ValidationRuleSet.class)
                    .onDelete(RESTRICT)
                    .map(MetrologyConfigurationValidationRuleSetUsageImpl.Fields.VALIDATION_RULE_SET.fieldName())
                    .on(validationRule)
                    .add();
            table.foreignKey("UPC_FK_METROLOGYCONFIG")
                    .references(MetrologyConfiguration.class)
                    .map(MetrologyConfigurationValidationRuleSetUsageImpl.Fields.METROLOGY_CONFIGURATION.fieldName())
                    .on(metrologyConfiguration)
                    .add();
        }
    };

    abstract void addTo(DataModel component);

}