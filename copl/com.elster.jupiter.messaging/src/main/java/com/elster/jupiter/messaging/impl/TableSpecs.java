package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.orm.AssociationMapping;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

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
	MSG_SUBSCRIBERSPEC {
		void describeTable(Table table) {
			Column destinationNameColumn = table.addColumn("DESTINATION","varchar2(30)",true,NOCONVERSION,"destinationName");
			Column nameColumn = table.addColumn("NAME","varchar2(30)",true,NOCONVERSION,"name");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("MSG_PK_SUBSCRIBERSPEC", destinationNameColumn , nameColumn);
			table.addForeignKeyConstraint("MSG_FK_SUBSCRIBERSPEC", MSG_DESTINATIONSPEC.name() , CASCADE , new AssociationMapping("destination","subscribers") , destinationNameColumn);
		}
	};
	
	public void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}