package com.elster.jupiter.ids.impl;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.*;

import com.elster.jupiter.orm.*;

public enum TableSpecs {
	IDS_VAULT {
		void  describeTable(Table table) {
			Column componentName = table.addColumn("COMPONENT","varchar2(3)",true,NOCONVERSION,"componentName");
			Column idColumn = table.addColumn("ID", "number" , true, NUMBER2LONG , "id");
			table.addColumn("DESCRIPTION", "varchar2(80)" , true , NOCONVERSION , "description");
			table.addColumn("MINTIME", "number", true, NUMBER2UTCINSTANT , "minTime");
			table.addColumn("MAXTIME", "number", false, NUMBER2UTCINSTANT, "maxTime");
			table.addColumn("SLOTCOUNT", "number", false, NUMBER2INT, "slotCount");
			table.addColumn("LOCALTIME", "char(1)", true, CHAR2BOOLEAN, "localTime");
			table.addColumn("REGULAR", "char(1)", true, CHAR2BOOLEAN, "regular");
			table.addColumn("JOURNAL", "char(1)", true, CHAR2BOOLEAN, "journal");
			table.addColumn("ACTIVE", "char(1)", true, CHAR2BOOLEAN, "active");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("IDS_PK_VAULTS", componentName, idColumn);
		}
	},
	IDS_RECORDSPEC {
		void describeTable(Table table) {
			Column componentName = table.addColumn("COMPONENT","varchar2(3)",true,NOCONVERSION,"componentName");
			Column idColumn = table.addColumn("ID", "number" , true, NUMBER2LONG , "id");
			Column nameColumn = table.addColumn("NAME", "varchar2(80)" , true , NOCONVERSION , "name");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("IDS_PKRECORDSPECS", componentName, idColumn);
			table.addUniqueConstraint("IDS_U_RECORDSPECS",componentName, nameColumn);
		}
	},	
	IDS_FIELDSPEC { 
		void describeTable(Table table) {
			Column componentName = table.addColumn("COMPONENT","varchar2(3)",true,NOCONVERSION,"componentName");
			Column recordSpecIdColumn = table.addColumn("RECORDSPECID", "number" , true, NUMBER2LONG , "recordSpecId");
			Column positionColumn = table.addColumn("POSITION", "number" , true, NUMBER2INT , "position");
			Column nameColumn = table.addColumn("NAME", "varchar2(80)" , true , NOCONVERSION , "name");
			table.addColumn("FIELDTYPE", "number" , true, NUMBER2ENUM , "fieldType");
			table.addColumn("DERIVATIONRULE", "number" , true, NUMBER2ENUM , "derivationRule");
			table.addCreateTimeColumn("CREATETIME", "createTime");
			table.addModTimeColumn("MODTIME", "modTime");
			table.addPrimaryKeyConstraint("IDS_PK_FIELDSPECS", componentName, recordSpecIdColumn , positionColumn );
			table.addUniqueConstraint("IDS_U_FIELDSPECS", componentName, recordSpecIdColumn , nameColumn );	
			table.addForeignKeyConstraint("IDS_FK_FIELDSPECS", IDS_RECORDSPEC.name(), CASCADE, "recordSpec" ,"fieldSpecs", componentName, recordSpecIdColumn);
		}
	},
	IDS_TIMESERIES {
		void describeTable(Table table) {
			Column idColumn = table.addAutoIdColumn();
			Column vaultComponent = table.addColumn("VAULTCOMPONENT","varchar2(3)",true,NOCONVERSION,"vaultComponentName");
			Column vaultIdColumn = table.addColumn("VAULTDID" , "number", true , NUMBER2LONG , "vaultId");
			Column recordSpecComponent = table.addColumn("RECORDSPECCOMPONENT","varchar2(3)",true,NOCONVERSION,"recordSpecComponentName");
			Column recordSpecIdColumn = table.addColumn("RECORDSPECID" , "number", true , NUMBER2LONG , "recordSpecId");
			table.addColumn("FIRSTTIME","number",false,NUMBER2UTCINSTANT,"firstTime");
			table.addColumn("LASTTIME","number",false,NUMBER2UTCINSTANT,"lastTime");
			table.addColumn("LOCKTIME","number",false,NUMBER2UTCINSTANT,"lockTime");
			table.addColumn("TIMEZONENAME", "varchar2(80)" , true , NOCONVERSION , "timeZoneName");
			table.addColumn("REGULAR", "CHAR(1)", true, CHAR2BOOLEAN, "regular");
			table.addColumn("INTERVALLENGTH", "number" , false, NUMBER2INTNULLZERO , "intervalLength");
			table.addColumn("INTERVALLENGTHUNIT", "number" , false, NUMBER2ENUMPLUSONE , "intervalLengthUnit");
			table.addColumn("HOUROFFSET", "number" , false, NUMBER2INTNULLZERO , "offset");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("IDS_PK_TIMESERIES", idColumn);
			table.addForeignKeyConstraint("IDS_FK_TIMESERIESVAULT", IDS_VAULT.name() , RESTRICT, "vault" , null , vaultComponent , vaultIdColumn);	
			table.addForeignKeyConstraint("IDS_FK_TIMESERIESRECORDSPEC", IDS_RECORDSPEC.name(), RESTRICT, "recordSpec" , null , recordSpecComponent, recordSpecIdColumn);
		}
	};
	
	void addTo(Component component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}