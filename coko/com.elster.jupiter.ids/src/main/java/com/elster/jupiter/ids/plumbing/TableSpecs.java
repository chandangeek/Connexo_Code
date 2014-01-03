package com.elster.jupiter.ids.plumbing;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.ids.impl.FieldSpecImpl;
import com.elster.jupiter.ids.impl.RecordSpecImpl;
import com.elster.jupiter.ids.impl.TimeSeriesImpl;
import com.elster.jupiter.ids.impl.VaultImpl;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
	IDS_VAULT {
		@Override
		public void  addTo(DataModel dataModel) {
			Table<Vault> table = dataModel.addTable(name(),Vault.class);
			table.map(VaultImpl.class);
			table.cache();
			Column componentName = table.column("COMPONENT").type("varchar2(3)").notNull().map("componentName").add();
			Column idColumn = table.column("ID").type("number").notNull().conversion(NUMBER2LONG).map("id").add();
			table.column("DESCRIPTION").type("varchar2(80)").notNull().map("description").add();
			table.column("MINTIME").type("number").notNull().conversion(NUMBER2UTCINSTANT).map("minTime").add();
			table.column("MAXTIME").type("number").conversion(NUMBER2UTCINSTANT).map("maxTime").add();
			table.column("SLOTCOUNT").type("number").conversion(NUMBER2INT).map("slotCount").add();
			table.column("LOCALTIME").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("localTime").add();
			table.column("REGULAR").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("regular").add();
			table.column("JOURNAL").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("journal").add();
			table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
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
			Column componentName = table.column("COMPONENT").type("varchar2(3)").notNull().map("componentName").add();
			Column idColumn = table.column("ID").type("number").notNull().conversion(NUMBER2LONG).map("id").add();
			Column nameColumn = table.column("NAME").type("varchar2(80)").notNull().map("name").add();
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
			Column componentName = table.column("COMPONENT").type("varchar2(3)").notNull().add();
			Column recordSpecIdColumn = table.column("RECORDSPECID").type("number").notNull().conversion(NUMBER2LONG).add();
			Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
			Column nameColumn = table.column("NAME").type("varchar2(80)").notNull().map("name").add();
			table.column("FIELDTYPE").type("number").notNull().conversion(NUMBER2ENUM).map("fieldType").add();
			table.column("DERIVATIONRULE").type("number").notNull().conversion(NUMBER2ENUM).map("derivationRule").add();
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
			Column vaultComponent = table.column("VAULTCOMPONENT").type("varchar2(3)").notNull().add();
			Column vaultIdColumn = table.column("VAULTID").type("number").notNull().conversion(NUMBER2LONG).add();
			Column recordSpecComponent = table.column("RECORDSPECCOMPONENT").type("varchar2(3)").notNull().add();
			Column recordSpecIdColumn = table.column("RECORDSPECID").type("number").notNull().conversion(NUMBER2LONG).add();
			table.column("FIRSTTIME").type("number").conversion(NUMBER2UTCINSTANT).map("firstTime").add();
			table.column("LASTTIME").type("number").conversion(NUMBER2UTCINSTANT).map("lastTime").add();
			table.column("LOCKTIME").type("number").conversion(NUMBER2UTCINSTANT).map("lockTime").add();
			table.column("TIMEZONENAME").type("varchar2(80)").notNull().map("timeZoneName").add();
			table.column("REGULAR").type("CHAR(1)").notNull().conversion(CHAR2BOOLEAN).map("regular").add();
			table.column("INTERVALLENGTH").type("number").conversion(NUMBER2INTNULLZERO).map("intervalLength").add();
			table.column("INTERVALLENGTHUNIT").type("number").conversion(NUMBER2ENUMPLUSONE).map("intervalLengthUnit").add();
			table.column("HOUROFFSET").type("number").conversion(NUMBER2INTNULLZERO).map("offset").add();
			table.addAuditColumns();
			table.primaryKey("IDS_PK_TIMESERIES").on(idColumn).add();
			table.foreignKey("IDS_FK_TIMESERIESVAULT").references(IDS_VAULT.name()).onDelete(RESTRICT).map("vault").on(vaultComponent , vaultIdColumn).add();
			table.foreignKey("IDS_FK_TIMESERIESRECORDSPEC").references(IDS_RECORDSPEC.name()).onDelete(RESTRICT).map("recordSpec").on(recordSpecComponent, recordSpecIdColumn).add();
		}
	};
	
	abstract public void addTo(DataModel component);
	
}