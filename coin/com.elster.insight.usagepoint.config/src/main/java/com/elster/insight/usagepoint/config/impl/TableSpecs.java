package com.elster.insight.usagepoint.config.impl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

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
	UPC_USGPNTMETROLOGYCONFIG() {
	    void addTo(DataModel dataModel) {
	        Table<UsagePointMetrologyConfiguration> table = dataModel.addTable(name(), UsagePointMetrologyConfiguration.class);
	        table.map(UsagePointMetrologyConfigurationImpl.class);
            Column usagePointIdColumn = table.column("USAGEPOINTID").number().notNull().conversion(NUMBER2LONG).add();
            Column metrologyConfigIdColumn = table.column("METROLOGYCONFIGID").number().notNull().conversion(NUMBER2LONG).add();
            table.addAuditColumns();
	        table.primaryKey("UPC_PK_USGPNTMETROLOGYCONFIG").on(usagePointIdColumn).add();
	        table.foreignKey("UPC_FK_USGPNTMETROLOGYCONFIGUP").on(usagePointIdColumn).references(UsagePoint.class).onDelete(DeleteRule.RESTRICT).map("usagePoint").add();
	        table.foreignKey("UPC_FK_USGPNTMETROLOGYCONFIGMC").on(metrologyConfigIdColumn).references(UPC_METROLOGYCONFIG.name()).onDelete(DeleteRule.RESTRICT).map("metrologyConfiguration").add();
	    }
	};
	
	abstract void addTo(DataModel component);	
}