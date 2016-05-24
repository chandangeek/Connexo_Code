package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationRuleSet;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
    UPC_MC_VALRULESET {
        void addTo(DataModel dataModel) {
            Table<MetrologyContractValidationRuleSetUsage> table = dataModel
                    .addTable(name(), MetrologyContractValidationRuleSetUsage.class);
            table.map(MetrologyContractValidationRuleSetUsageImpl.class);
            table.setJournalTableName("UPC_MC_VALRULESETJRNL");
            Column validationRuleSet = table
                    .column("VALIDATIONRULESETID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            Column metrologyContract = table
                    .column("METROLOGYCONTRACTID")
                    .type("number")
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();

            table.primaryKey("UPC_PK_RULESETCONTRACT")
                    .on(validationRuleSet, metrologyContract)
                    .add();
            table.foreignKey("UPC_FK_RULESET")
                    .references(ValidationRuleSet.class)
                    .onDelete(RESTRICT)
                    .map(MetrologyContractValidationRuleSetUsageImpl.Fields.VALIDATION_RULE_SET.fieldName())
                    .on(validationRuleSet)
                    .add();
            table.foreignKey("UPC_FK_METROLOGYCONTRACT")
                    .references(MetrologyContract.class)
                    .map(MetrologyContractValidationRuleSetUsageImpl.Fields.METROLOGY_CONTRACT.fieldName())
                    .on(metrologyContract)
                    .add();
        }
    };

    abstract void addTo(DataModel component);

}