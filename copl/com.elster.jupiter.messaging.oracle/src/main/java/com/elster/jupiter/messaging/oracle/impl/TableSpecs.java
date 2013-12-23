package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
	MSG_QUEUETABLESPEC(QueueTableSpec.class) {
		void  describeTable(Table table) {
            table.map(QueueTableSpecImpl.class);
			Column nameColumn = table.column("NAME").type("varchar2(30)").notNull().map("name").add();
			table.column("PAYLOADTYPE").type("varchar2(30)").notNull().map("payloadType").add();
			table.column("MULTICONSUMER").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("multiConsumer").add();
			table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
			table.addAuditColumns();
			table.primaryKey("MSG_PK_QUEUETABLESPEC").on(nameColumn).add();
		}
	},
	MSG_DESTINATIONSPEC (DestinationSpec.class) {
		void describeTable(Table table) {
            table.map(DestinationSpecImpl.class);
			Column nameColumn = table.column("NAME").type("varchar2(30)").notNull().map("name").add();
			Column queueTableNameColumn = table.column("QUEUETABLENAME").type("varchar2(30)").notNull().map("queueTableName").add();
			table.column("RETRYDELAY").type("number").notNull().conversion(NUMBER2INT).map("retryDelay").add();
			table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
			table.addAuditColumns();
			table.primaryKey("MSG_PK_DESTINATIONSPEC").on(nameColumn).add();
			table.foreignKey("MSG_FK_DESTINATIONSPEC").references(MSG_QUEUETABLESPEC.name()).onDelete(RESTRICT).map("queueTableSpec").on(queueTableNameColumn).add();
		}
	}, 
	MSG_SUBSCRIBERSPEC (SubscriberSpec.class) {
		void describeTable(Table table) {
            table.map(SubscriberSpecImpl.class);
			Column destinationColumn = table.column("DESTINATION").type("varchar2(30)").notNull().add();
			Column nameColumn = table.column("NAME").type("varchar2(30)").notNull().map("name").add();
			table.addAuditColumns();
			table.primaryKey("MSG_PK_SUBSCRIBERSPEC").on(destinationColumn , nameColumn).add();
			table.foreignKey("MSG_FK_SUBSCRIBERSPEC").references(MSG_DESTINATIONSPEC.name()).onDelete(CASCADE).map("destination").reverseMap("subscribers").on(destinationColumn).composition().add();
		}
	};
	
	private Class<?> api;
	
	TableSpecs(Class<?> api) {
		this.api = api;
	}
	
	public void addTo(DataModel component) {
		Table table = component.addTable(name(),api);
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}