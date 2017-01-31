/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.LifeCycleClass;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.users.Group;

import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.Table.MAX_STRING_LENGTH;

public enum TableSpecs {
    WS_ENDPOINTCFG {
        @Override
        public void addTo(DataModel dataModel) {
            Table<EndPointConfiguration> table = dataModel.addTable(this.name(), EndPointConfiguration.class);
            table.map(EndPointConfigurationImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.column("name").varChar().notNull().map(EndPointConfigurationImpl.Fields.NAME.fieldName()).add();
            table.column("URL").varChar().map(EndPointConfigurationImpl.Fields.URL.fieldName()).notNull().add();
            table.column("webServiceName")
                    .varChar()
                    .notNull()
                    .map(EndPointConfigurationImpl.Fields.WEB_SERVICE_NAME.fieldName())
                    .add();
            table.column("active").bool().map(EndPointConfigurationImpl.Fields.ACTIVE.fieldName()).add();
            table.column("authenticated")
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .notNull()
                    .map(EndPointConfigurationImpl.Fields.AUTHENTICATED.fieldName())
                    .add();
            table.column("httpCompression")
                    .bool()
                    .map(EndPointConfigurationImpl.Fields.HTTP_COMPRESSION.fieldName())
                    .add();
            table.column("schemaValidation")
                    .bool()
                    .map(EndPointConfigurationImpl.Fields.SCHEMA_VALIDATION.fieldName())
                    .add();
            table.column("tracing").bool().map(EndPointConfigurationImpl.Fields.TRACING.fieldName()).add();
            table.column("tracefile")
                    .varChar()
                    .map(EndPointConfigurationImpl.Fields.TRACEFILE.fieldName())
                    .add();
            table.column("logLevel")
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(EndPointConfigurationImpl.Fields.LOG_LEVEL.fieldName())
                    .add();
            Column group = table.column("groupref")
                    .number()
                    .add();
            table.column("usrname") // intentional spelling: avoid conflict with 'username' as added by addAuditColumns()
                    .varChar()
                    .map(EndPointConfigurationImpl.Fields.USERNAME.fieldName())
                    .add();
            table.column("passwd")
                    .varChar()
                    .map(EndPointConfigurationImpl.Fields.PASSWD.fieldName())
                    .add();
            table.foreignKey("FK_USR_GROUP")
                    .references(Group.class)
                    .on(group)
                    .map(EndPointConfigurationImpl.Fields.GROUP.fieldName())
                    .add();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");

            table.primaryKey("PK_WS_ENDPOINT").on(id).add();
        }
    },
    WS_ENDPOINT_LOG {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndPointLog> table = dataModel.addTable(this.name(), EndPointLog.class);
            table.map(EndPointLogImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column endPoint = table.column("ENDPOINTCFG").number().notNull().add();
            table.foreignKey("FK_WS_ENDPOINT")
                    .references(WS_ENDPOINTCFG.name())
                    .on(endPoint)
                    .onDelete(DeleteRule.CASCADE)
                    .map(EndPointLogImpl.Fields.endPointConfiguration.fieldName())
                    .add();
            table.column("LOGLEVEL")
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .notNull()
                    .map(EndPointLogImpl.Fields.logLevel.fieldName())
                    .add();
            Column timestampColumn = table.column("TIMESTAMP")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .notNull()
                    .map(EndPointLogImpl.Fields.timestamp.fieldName())
                    .add();
            table.column("MESSAGE")
                    .number()
                    .varChar(MAX_STRING_LENGTH)
                    .map(EndPointLogImpl.Fields.message.fieldName())
                    .add();
            table.column("STACKTRACE")
                    .type("CLOB")
                    .conversion(CLOB2STRING)
                    .map(EndPointLogImpl.Fields.stacetrace.fieldName())
                    .add();
            table.primaryKey("SCS_PK_ENDPOINT_LOG").on(idColumn).add();
            table.autoPartitionOn(timestampColumn, LifeCycleClass.WEBSERVICES);

        }
    };

    abstract void addTo(DataModel component);

}
