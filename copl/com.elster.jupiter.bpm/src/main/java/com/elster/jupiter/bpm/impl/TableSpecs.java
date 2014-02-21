package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmEngine;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

public enum TableSpecs {
	BPM_ENGINE {
		void addTo(DataModel dataModel) {
			Table<BpmEngine> table = dataModel.addTable(name(), BpmEngine.class);
			table.map(BpmEngineImpl.class);
			Column nameColumn = table.column("ENGINE_NAME").varChar(32).notNull().map("name").add();
			table.column("ENGINE_LOCATION").varChar(1024).notNull().map("location").add();
            table.addAuditColumns();
			table.primaryKey("BPM_PK_BPMDIRECTORY").on(nameColumn).add();
		}
	};

	abstract void addTo(DataModel dataModel);
	
}