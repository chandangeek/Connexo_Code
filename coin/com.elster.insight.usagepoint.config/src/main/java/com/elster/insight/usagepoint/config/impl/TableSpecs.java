package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
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
	};
	
	abstract void addTo(DataModel component);	
}