package com.elster.jupiter.messaging.impl;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.*;

import com.elster.jupiter.orm.*;

public enum TableSpecs {
	MSG_QUEUETABLE {
		void  describeTable(Table table) {
			Column nameColumn = table.addColumn("NAME","varchar2(30)",true,NOCONVERSION,"name");
			table.addColumn("PAYLOADTYPE", "varchar2(30)" , true , NOCONVERSION , "payloadType");
			table.addColumn("MULTICONSUMER", "char(1)", true, CHAR2BOOLEAN, "multiConsumer");
			table.addColumn("ACTIVE", "char(1)", true, CHAR2BOOLEAN, "active");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MSG_PK_QUEUETABLE", nameColumn);
		}
	},
	MSG_QUEUE {
		void describeTable(Table table) {
			Column nameColumn = table.addColumn("NAME","varchar2(30)",true,NOCONVERSION,"name");
			Column queueTableNameColumn = table.addColumn("QUEUETABLENAME" , "number", true , NOCONVERSION , "queueTableName");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MSG_PK_QUEUE", nameColumn);
			table.addForeignKeyConstraint("MSG_FK_QUEUE", MSG_QUEUETABLE.name() , RESTRICT, new AssociationMapping("queueTable") , queueTableNameColumn);				
		}
	};
	
	public void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}