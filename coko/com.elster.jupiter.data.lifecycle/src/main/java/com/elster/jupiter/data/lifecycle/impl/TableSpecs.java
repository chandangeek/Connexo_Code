/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

public enum TableSpecs {
	LFC_CATEGORIES {
		@Override
		void addTo(DataModel dataModel) {
			Table<LifeCycleCategory> table = dataModel.addTable(name(), LifeCycleCategory.class);
			table.map(LifeCycleCategoryImpl.class);
			table.setJournalTableName("LFC_CATEGORIESJRNL");
			Column nameColumn = table.column("KIND").varChar(20).notNull().map("kind").conversion(ColumnConversion.CHAR2ENUM).add();
			table.column("PARTITIONSIZE").number().notNull().map("partitionSize").conversion(ColumnConversion.NUMBER2INT).add();
			table.column("RETENTION").number().notNull().map("retention").conversion(ColumnConversion.NUMBER2INT).add();
			table.addAuditColumns();
			table.primaryKey("PK_LFC_CATEGORIES").on(nameColumn).add();
		}		
	}
	;
	
	abstract void addTo(DataModel dataModel);
}
