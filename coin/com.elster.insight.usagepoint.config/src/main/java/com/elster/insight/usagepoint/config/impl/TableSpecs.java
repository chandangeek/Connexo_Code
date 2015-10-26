package com.elster.insight.usagepoint.config.impl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

import com.elster.insight.usagepoint.config.MetrologyConfValidationRuleSetUsage;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationRuleSet;

public enum TableSpecs {
	UPC_METROLOGYCONFIG () {
		void addTo(DataModel dataModel) {
			Table<MetrologyConfiguration> table = dataModel.addTable(name(), MetrologyConfiguration.class);
			table.map(MetrologyConfigurationImpl.class);
			Column id = table.addAutoIdColumn();
			Column name = table.column("NAME").varChar().notNull().map("name").add();
			table.addAuditColumns();
			table.unique("UPC_UK_METROLOGYCONFIGURATION").on(name).add();
			table.primaryKey("UPC_PK_METROLOGYCONFIGURATION").on(id).add();
		}
	},
	UPC_UPMC() {
	    void addTo(DataModel dataModel) {
	        Table<UsagePointMetrologyConfiguration> table = dataModel.addTable(name(), UsagePointMetrologyConfiguration.class);
	        table.map(UsagePointMetrologyConfigurationImpl.class);
	        Column id = table.addAutoIdColumn();
            Column usagePointIdColumn = table.column("USAGEPOINTID").number().notNull().conversion(NUMBER2LONG).add();
            Column metrologyConfigIdColumn = table.column("METROLOGYCONFIGID").number().notNull().conversion(NUMBER2LONG).add();
            table.addAuditColumns();
	        table.primaryKey("UPC_PK_UPMC").on(id).add();
	        table.unique("UPC_UK_UPMCUP").on(usagePointIdColumn).add();
	        table.foreignKey("UPC_FK_UPMCUP").on(usagePointIdColumn).references(UsagePoint.class).onDelete(DeleteRule.RESTRICT).map("usagePoint").add();
	        table.foreignKey("UPC_FK_UPMCMC").on(metrologyConfigIdColumn).references(UPC_METROLOGYCONFIG.name()).onDelete(DeleteRule.RESTRICT).map("metrologyConfiguration").add();
	    }
	},
    UPC_MCVALRULESETUSAGE() {
        void addTo(DataModel dataModel) {
            Table<MetrologyConfValidationRuleSetUsage> table = dataModel.addTable(name(), MetrologyConfValidationRuleSetUsage.class);
            table.map(MetrologyConfValidationRuleSetUsageImpl.class);
            table.setJournalTableName("UPC_MCVALRULESETUSAGEJRNL");
            Column validationRuleSetIdColumn =
                    table.column("VALIDATIONRULESETID").type("number").notNull().conversion(NUMBER2LONG).map("validationRuleSetId").add();
            Column metrologyConfigurationIdColumn =
                    table.column("METROLOGYCONFIGID").type("number").notNull().conversion(NUMBER2LONG).map("metrologyConfigurationId").add();

            table.primaryKey("UPC_PK_SETCONFIGUSAGE").on(validationRuleSetIdColumn, metrologyConfigurationIdColumn).add();
            table.foreignKey("UPC_FK_RULESET").references(ValidationRuleSet.class).onDelete(RESTRICT).map("validationRuleSet").on(validationRuleSetIdColumn).add();
            table.foreignKey("UPC_FK_METROLOGYCONFIG").references("UPC_METROLOGYCONFIG").reverseMap("metrologyConfValidationRuleSetUsages").composition().map("metrologyConfiguration").on(metrologyConfigurationIdColumn).add();
        }
    };
	
	abstract void addTo(DataModel component);	
}