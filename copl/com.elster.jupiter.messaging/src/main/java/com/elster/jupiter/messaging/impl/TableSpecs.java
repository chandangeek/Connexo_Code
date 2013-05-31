package com.elster.jupiter.messaging.impl;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.*;

import com.elster.jupiter.orm.*;

public enum TableSpecs {
	MSG_QUEUETABLESPEC {
		void  describeTable(Table table) {
			Column nameColumn = table.addColumn("NAME","varchar2(30)",true,NOCONVERSION,"name");
			table.addColumn("PAYLOADTYPE", "varchar2(30)" , true , NOCONVERSION , "payloadType");
			table.addColumn("MULTICONSUMER", "char(1)", true, CHAR2BOOLEAN, "multiConsumer");
			table.addColumn("ACTIVE", "char(1)", true, CHAR2BOOLEAN, "active");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MSG_PK_QUEUETABLESPEC", nameColumn);
		}
	},
	MSG_DESTINATIONSPEC {
		void describeTable(Table table) {
			Column nameColumn = table.addColumn("NAME","varchar2(30)",true,NOCONVERSION,"name");
			Column queueTableNameColumn = table.addColumn("QUEUETABLENAME" , "varchar2(30)", true , NOCONVERSION , "queueTableName");
			table.addColumn("RETRYDELAY" , "number" , true, NUMBER2INT , "retryDelay");
			table.addColumn("ACTIVE", "char(1)", true, CHAR2BOOLEAN, "active");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MSG_PK_DESTINATIONSPEC", nameColumn);
			table.addForeignKeyConstraint("MSG_FK_DESTINATIONSPEC", MSG_QUEUETABLESPEC.name() , RESTRICT, new AssociationMapping("queueTable") , queueTableNameColumn);				
		}
	}, 
	MSG_CONSUMERSPEC {
		void describeTable(Table table) {
			Column destinationNameColumn = table.addColumn("DESTINATION","varchar2(30)",true,NOCONVERSION,"destinationName");
			Column nameColumn = table.addColumn("NAME","varchar2(30)",true,NOCONVERSION,"name");
			table.addColumn("WORKERCOUNT" , "number" , true, NUMBER2INT , "workerCount");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MSG_PK_CONSUMERSPEC", destinationNameColumn , nameColumn);
			table.addForeignKeyConstraint("MSG_FK_CONSUMERSPEC", MSG_DESTINATIONSPEC.name() , CASCADE , new AssociationMapping("destination","consumers") , destinationNameColumn);				
		}
	};
	
	public void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}