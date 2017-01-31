/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUMPLUSONE;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INTNULLZERO;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

public enum TableSpecs {
	IDS_VAULT {
		@Override
		public void  addTo(DataModel dataModel) {
			Table<Vault> table = dataModel.addTable(name(),Vault.class);
			table.map(VaultImpl.class);
			table.cache();
			Column componentName = table.column("COMPONENT").varChar(3).notNull().map("componentName").add();
			Column idColumn = table.column("ID").number().notNull().conversion(NUMBER2LONG).map("id").add();
			table.column("DESCRIPTION").varChar(NAME_LENGTH).notNull().map("description").add();
			table.column("MINTIME").number().notNull().conversion(NUMBER2INSTANT).map("minTime").add();
			table.column("MAXTIME").number().conversion(NUMBER2INSTANT).map("maxTime").add();
			table.column("SLOTCOUNT").number().conversion(NUMBER2INT).map("slotCount").add();
			table.column("TEXTSLOTCOUNT").number().conversion(NUMBER2INT).map("textSlotCount").add();
			table.column("LOCALTIME").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("localTime").add();
			table.column("REGULAR").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("regular").add();
			table.column("JOURNAL").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("journal").add();
			table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
			table.column("RETENTIONDAYS").number().notNull().conversion(NUMBER2INT).map("retentionDays").add();
			table.column("PARTITIONED").bool().map("partitioned").add();
			table.addAuditColumns();
			table.primaryKey("IDS_PK_VAULTS").on(componentName, idColumn).add();
		}
	},
	IDS_RECORDSPEC {
		@Override
		public void  addTo(DataModel dataModel) {
			Table<RecordSpec> table = dataModel.addTable(name(),RecordSpec.class);
			table.map(RecordSpecImpl.class);
			table.cache();
			Column componentName = table.column("COMPONENT").varChar(3).notNull().map("componentName").add();
			Column idColumn = table.column("ID").number().notNull().conversion(NUMBER2LONG).map("id").add();
			Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
			table.addAuditColumns();
			table.primaryKey("IDS_PKRECORDSPECS").on(componentName, idColumn).add();
			table.unique("IDS_U_RECORDSPECS").on(componentName, nameColumn).add();
		}
	},
	IDS_FIELDSPEC {
		@Override
		public void  addTo(DataModel dataModel) {
			Table<FieldSpec> table = dataModel.addTable(name(),FieldSpec.class);
			table.map(FieldSpecImpl.class);
			Column componentName = table.column("COMPONENT").varChar(3).notNull().add();
			Column recordSpecIdColumn = table.column("RECORDSPECID").number().notNull().conversion(NUMBER2LONG).add();
			Column positionColumn = table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();
			Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
			table.column("FIELDTYPE").number().notNull().conversion(NUMBER2ENUM).map("fieldType").add();
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.primaryKey("IDS_PK_FIELDSPECS").on(componentName, recordSpecIdColumn , positionColumn ).add();
			table.unique("IDS_U_FIELDSPECS").on(componentName, recordSpecIdColumn , nameColumn ).add();
			table.foreignKey("IDS_FK_FIELDSPECS").on(componentName, recordSpecIdColumn).references(IDS_RECORDSPEC.name()).onDelete(CASCADE).
				map("recordSpec").reverseMap("fieldSpecs").reverseMapOrder("position").composition().add();
		}
	},
	IDS_TIMESERIES {
		@Override
		public void  addTo(DataModel dataModel) {
			Table<TimeSeries> table = dataModel.addTable(name(),TimeSeries.class);
			table.map(TimeSeriesImpl.class);
			Column idColumn = table.addAutoIdColumn();
			Column vaultComponent = table.column("VAULTCOMPONENT").varChar(3).notNull().add();
			Column vaultIdColumn = table.column("VAULTID").number().notNull().conversion(NUMBER2LONG).add();
			Column recordSpecComponent = table.column("RECORDSPECCOMPONENT").varChar(3).notNull().add();
			Column recordSpecIdColumn = table.column("RECORDSPECID").number().notNull().conversion(NUMBER2LONG).add();
			table.column("FIRSTTIME").number().conversion(NUMBER2INSTANT).map("firstTime").add();
			table.column("LASTTIME").number().conversion(NUMBER2INSTANT).map("lastTime").add();
			table.column("LOCKTIME").number().conversion(NUMBER2INSTANT).map("lockTime").add();
			table.column("TIMEZONENAME").varChar(NAME_LENGTH).notNull().map("timeZoneName").add();
			table.column("REGULAR").type("CHAR(1)").notNull().conversion(CHAR2BOOLEAN).map("regular").add();
			table.column("INTERVALLENGTH").number().conversion(NUMBER2INTNULLZERO).map("intervalLength").add();
			table.column("INTERVALLENGTHUNIT").number().conversion(NUMBER2ENUMPLUSONE).map("intervalLengthUnit").add();
			table.column("HOUROFFSET").number().conversion(NUMBER2INTNULLZERO).map("offset").add();
			table.addAuditColumns();
			table.primaryKey("IDS_PK_TIMESERIES").on(idColumn).add();
			table.foreignKey("IDS_FK_TIMESERIESVAULT").references(IDS_VAULT.name()).onDelete(RESTRICT).map("vault").on(vaultComponent , vaultIdColumn).add();
			table.foreignKey("IDS_FK_TIMESERIESRECORDSPEC").references(IDS_RECORDSPEC.name()).onDelete(RESTRICT).map("recordSpec").on(recordSpecComponent, recordSpecIdColumn).add();
		}
	};

	public abstract void addTo(DataModel component);

}