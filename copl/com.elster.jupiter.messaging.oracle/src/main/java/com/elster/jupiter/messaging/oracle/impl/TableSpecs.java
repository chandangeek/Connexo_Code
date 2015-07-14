package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
    MSG_QUEUETABLESPEC {
        public void addTo(DataModel dataModel) {
            Table<QueueTableSpec> table = dataModel.addTable(name(), QueueTableSpec.class);
            table.map(QueueTableSpecImpl.class);
            Column nameColumn = table.column("NAME").varChar(30).notNull().map("name").add();
            table.column("PAYLOADTYPE").varChar(30).notNull().map("payloadType").add();
            table.column("MULTICONSUMER").bool().map("multiConsumer").add();
            table.column("ACTIVE").bool().map("active").add();
            table.addAuditColumns();
            table.primaryKey("MSG_PK_QUEUETABLESPEC").on(nameColumn).add();
        }
    },
    MSG_DESTINATIONSPEC {
        public void addTo(DataModel dataModel) {
            Table<DestinationSpec> table = dataModel.addTable(name(), DestinationSpec.class);
            table.map(DestinationSpecImpl.class);
            Column nameColumn = table.column("NAME").varChar(30).notNull().map("name").add();
            Column queueTableNameColumn = table.column("QUEUETABLENAME").varChar(30).notNull().map("queueTableName").add();
            table.column("RETRYDELAY").number().notNull().conversion(NUMBER2INT).map("retryDelay").add();
            table.column("ACTIVE").bool().map("active").add();
            table.column("BUFFERED").bool().map("buffered").add();
            table.addAuditColumns();
            table.primaryKey("MSG_PK_DESTINATIONSPEC").on(nameColumn).add();
            table.foreignKey("MSG_FK_DESTINATIONSPEC").references(MSG_QUEUETABLESPEC.name()).onDelete(RESTRICT).map("queueTableSpec").on(queueTableNameColumn).add();
        }
    },
    MSG_SUBSCRIBERSPEC {
        public void addTo(DataModel dataModel) {
            Table<SubscriberSpec> table = dataModel.addTable(name(), SubscriberSpec.class);
            table.map(SubscriberSpecImpl.class);
            Column destinationColumn = table.column("DESTINATION").varChar(30).notNull().add();
            Column nameColumn = table.column("NAME").varChar(30).notNull().map("name").add();
            table.column("SYSTEMMANAGED").bool().map("systemManaged").add();
            table.column("filter").varChar().map("filter").add();
            table.addAuditColumns();
            table.primaryKey("MSG_PK_SUBSCRIBERSPEC").on(destinationColumn , nameColumn).add();
            table.foreignKey("MSG_FK_SUBSCRIBERSPEC").references(MSG_DESTINATIONSPEC.name()).onDelete(CASCADE).map("destination").reverseMap("subscribers").on(destinationColumn).composition().add();
        }
    };

    public abstract void addTo(DataModel dataModel);

}