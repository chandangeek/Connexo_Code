package com.elster.insight.usagepoint.config.impl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationRuleSet;

public enum TableSpecs {
    UPC_METROLOGYCONFIG {
        void addTo(DataModel dataModel) {
            Table<MetrologyConfiguration> table = dataModel.addTable(name(), MetrologyConfiguration.class);
            table.map(MetrologyConfigurationImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column(MetrologyConfigurationImpl.Fields.NAME.name()).varChar().notNull().map(MetrologyConfigurationImpl.Fields.NAME.fieldName()).add();
            table.column(MetrologyConfigurationImpl.Fields.ACTIVE.name()).bool().map(MetrologyConfigurationImpl.Fields.ACTIVE.fieldName()).notNull().add();
            table.addAuditColumns();
            table.unique("UPC_UK_METROLOGYCONFIGURATION").on(name).add();
            table.primaryKey("UPC_PK_METROLOGYCONFIGURATION").on(id).add();
        }
    },
    UPC_UPMC {
        void addTo(DataModel dataModel) {
            Table<UsagePointMetrologyConfiguration> table = dataModel
                    .addTable(name(), UsagePointMetrologyConfiguration.class);
            table.map(UsagePointMetrologyConfigurationImpl.class);
            Column id = table.addAutoIdColumn();
            Column usagePointIdColumn = table
                    .column("USAGEPOINTID")
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            Column metrologyConfigIdColumn = table
                    .column("METROLOGYCONFIGID")
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            table.addAuditColumns();
            table.primaryKey("UPC_PK_UPMC").on(id).add();
            table.unique("UPC_UK_UPMCUP").on(usagePointIdColumn).add();
            table.foreignKey("UPC_FK_UPMCUP")
                    .on(usagePointIdColumn)
                    .references(UsagePoint.class)
                    .onDelete(DeleteRule.RESTRICT)
                    .map("usagePoint")
                    .add();
            table.foreignKey("UPC_FK_UPMCMC")
                    .on(metrologyConfigIdColumn)
                    .references(UPC_METROLOGYCONFIG.name())
                    .onDelete(DeleteRule.RESTRICT)
                    .map("metrologyConfiguration")
                    .add();
        }
    },
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
                    .on(validationRuleSetIdColumn, metrologyConfigurationIdColumn).add();
            table.foreignKey("UPC_FK_RULESET")
                    .references(ValidationRuleSet.class)
                    .onDelete(RESTRICT)
                    .map("validationRuleSet")
                    .on(validationRuleSetIdColumn)
                    .add();
            table.foreignKey("UPC_FK_METROLOGYCONFIG")
                    .references("UPC_METROLOGYCONFIG")
                    .reverseMap("metrologyConfValidationRuleSetUsages")
                    .composition().map("metrologyConfiguration")
                    .on(metrologyConfigurationIdColumn)
                    .add();
        }
    },
    UPC_M_CONFIG_CPS_USAGES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<MetrologyConfigurationCustomPropertySetUsage> table = dataModel.addTable(name(), MetrologyConfigurationCustomPropertySetUsage.class);
            table.map(MetrologyConfigurationCustomPropertySetUsageImpl.class);
            Column metrologyConfig = table.column(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.METROLOGY_CONFIG.name()).number().notNull().add();
            Column customPropertySet = table.column(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.CUSTOM_PROPERTY_SET.name()).number().notNull().add();
            table.column(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.POSITION.name()).number().notNull().conversion(NUMBER2INT).map(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.POSITION.fieldName()).add();
            table.primaryKey("PK_M_CONFIG_CPS_USAGE").on(metrologyConfig, customPropertySet).add();
            table.foreignKey("FK_MCPS_USAGE_TO_CONFIG")
                    .references(UPC_METROLOGYCONFIG.name())
                    .on(metrologyConfig)
                    .onDelete(CASCADE)
                    .map(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.METROLOGY_CONFIG.fieldName())
                    .reverseMap(MetrologyConfigurationImpl.Fields.CUSTOM_PROPERTY_SETS.fieldName())
                    .reverseMapOrder(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.POSITION.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_MCAS_USAGE_TO_CPS")
                    .references(RegisteredCustomPropertySet.class)
                    .on(customPropertySet)
                    .onDelete(CASCADE)
                    .map(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.CUSTOM_PROPERTY_SET.fieldName())
                    .add();
        }
    },
    ;

    abstract void addTo(DataModel component);
}