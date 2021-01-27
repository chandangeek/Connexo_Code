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
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.elster.jupiter.soap.whiteboard.cxf.PayloadSaveStrategy;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributeBinding;
import com.elster.jupiter.users.Group;

import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.MAX_STRING_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;


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
            table.column("clientId")
                    .varChar()
                    .map(EndPointConfigurationImpl.Fields.CLIENT_ID.fieldName())
                    .since(version(10, 8))
                    .add();
            table.column("clientSecret")
                    .varChar()
                    .map(EndPointConfigurationImpl.Fields.CLIENT_SECRET.fieldName())
                    .since(version(10, 8))
                    .add();
            table.column(EndPointConfigurationImpl.Fields.PAYLOAD_SAVE_STRATEGY.name())
                    .number()
                    .notNull()
                    .installValue(String.valueOf(PayloadSaveStrategy.ALWAYS.ordinal()))
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(EndPointConfigurationImpl.Fields.PAYLOAD_SAVE_STRATEGY.fieldName())
                    .since(version(10, 8, 7, 1))
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
    WS_CALL_OCCURRENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<WebServiceCallOccurrence> table = dataModel.addTable(this.name(), WebServiceCallOccurrence.class);
            table.map(WebServiceCallOccurrenceImpl.class);
            table.since(version(10, 7));

            Column idColumn = table.addAutoIdColumn();

            Column endPoint = table.column("ENDPOINTCFG").number().notNull().add();
            table.foreignKey("FK_WS_CALL_OCCURRENCE_2_EPC")
                    .references(WS_ENDPOINTCFG.name())
                    .on(endPoint)
                    .onDelete(DeleteRule.CASCADE)
                    .map(WebServiceCallOccurrenceImpl.Fields.ENDPOINT_CONFIGURATION.fieldName())
                    .add();

            Column startTimeColumn = table.column("STARTTIME")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .notNull()
                    .map(WebServiceCallOccurrenceImpl.Fields.START_TIME.fieldName())
                    .add();

            table.column("ENDTIME")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(WebServiceCallOccurrenceImpl.Fields.END_TIME.fieldName())
                    .add();

            table.column("REQUESTNAME")
                    .varChar(NAME_LENGTH)
                    .map(WebServiceCallOccurrenceImpl.Fields.REQUEST_NAME.fieldName())
                    .add();

            table.column("STATUS")
                    .varChar(NAME_LENGTH)
                    .conversion(ColumnConversion.CHAR2ENUM)
                    .notNull()
                    .map(WebServiceCallOccurrenceImpl.Fields.STATUS.fieldName())
                    .add();
            table.column("APPLICATIONNAME")
                    .varChar(NAME_LENGTH)
                    .map(WebServiceCallOccurrenceImpl.Fields.APPLICATION_NAME.fieldName())
                    .add();
            table.column("PAYLOAD")
                    .type("CLOB")
                    .conversion(CLOB2STRING)
                    .map(WebServiceCallOccurrenceImpl.Fields.PAYLOAD.fieldName())
                    .add();
            table.column(WebServiceCallOccurrenceImpl.Fields.APP_SERVER_NAME.name())
                    .varChar(14)  // Application server name should be less then 25 characters due to DB constrains (including "APPSERVER_" prefix)
                    .map(WebServiceCallOccurrenceImpl.Fields.APP_SERVER_NAME.fieldName())
                    .since(version(10, 8, 7))
                    .add();

            table.primaryKey("PK_WS_CALL_OCCURRENCE").on(idColumn).add();
            // table.index("IX_WS_CALL_START").on(desc(startTimeColumn)).add().since(Version.version(10, 7, 5)); // done in UpgraderV10_7_5
            // table.index("IX_WS_CALL_END").on(desc(endTimeColumn)).add().since(Version.version(10, 7, 5)); // done in UpgraderV10_7_5
            // table.index("IX_WS_CALL_STATUS").on(statusColumn).compress(1).add().since(Version.version(10, 7, 5)); // done in UpgraderV10_7_5
            // table.index("IX_WS_CALL_APP").on(applicationColumn).compress(1).add().since(Version.version(10, 7, 5)); // done in UpgraderV10_7_5
            table.autoPartitionOn(startTimeColumn, LifeCycleClass.WEBSERVICES);
        }
    },
    WS_OCC_RELATED_ATTR {
        @Override
        void addTo(DataModel dataModel) {
            Table<WebServiceCallRelatedAttribute> table = dataModel.addTable(this.name(), WebServiceCallRelatedAttribute.class);
            table.map(WebServiceCallRelatedAttributeImpl.class);
            table.since(version(10, 7, 1));

            Column idColumn = table.addAutoIdColumn();

            Column keyColumn = table.column(WebServiceCallRelatedAttributeImpl.Fields.ATTR_KEY.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(WebServiceCallRelatedAttributeImpl.Fields.ATTR_KEY.fieldName())
                    .add();
            Column valueColumn = table.column(WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.fieldName())
                    .add();
            table.unique("WS_UQ_KEY_VALUE").on(keyColumn, valueColumn)
                    // .compressIndex(1) // done in UpgraderV10_7_5
                    .add();
            // table.index("IX_WS_CALL_ATTR_VALUE").on(upper(valueColumn)).add().since(Version.version(10, 7, 5)); // done in UpgraderV10_7_5
            table.primaryKey("PK_WS_RELATED_ATTRBT").on(idColumn).add();
        }
    },
    WS_OCC_ATTRBTS_BINDING {
        @Override
        void addTo(DataModel dataModel) {
            Table<WebServiceCallRelatedAttributeBinding> table = dataModel.addTable(this.name(), WebServiceCallRelatedAttributeBinding.class);
            table.map(WebServiceCallRelatedAttributeBindingImpl.class);
            table.since(version(10, 7, 1));

            Column idColumn = table.addAutoIdColumn();

            Column occurrence = table.column("OCCURRENCE").number().notNull().add();
            table.foreignKey("FK_WS_RO_OCCURRENCE")
                    .references(WS_CALL_OCCURRENCE.name())
                    .on(occurrence)
                    .onDelete(DeleteRule.CASCADE)
                    .map(WebServiceCallRelatedAttributeBindingImpl.Fields.OCCURRENCE.fieldName())
                    .add();

            Column type = table.column("ATTRIBUTE").number().notNull().add();
            table.foreignKey("FK_WS_RA_ATTRIBUTE")
                    .references(WS_OCC_RELATED_ATTR.name())
                    .on(type)
                    .onDelete(DeleteRule.CASCADE)
                    .map(WebServiceCallRelatedAttributeBindingImpl.Fields.ATTRIBUTE.fieldName())
                    .add();

            table.primaryKey("PK_WS_OCC_BINDING").on(idColumn).add();
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
                    .map(EndPointLogImpl.Fields.ENDPOINT_CONFIGURATION.fieldName())
                    .add();
            table.column("LOGLEVEL")
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .notNull()
                    .map(EndPointLogImpl.Fields.LOG_LEVEL.fieldName())
                    .add();
            Column timestampColumn = table.column("TIMESTAMP")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .notNull()
                    .map(EndPointLogImpl.Fields.TIMESTAMP.fieldName())
                    .add();
            table.column("MESSAGE")
                    .number()
                    .varChar(MAX_STRING_LENGTH)
                    .map(EndPointLogImpl.Fields.MESSAGE.fieldName())
                    .add();
            table.column("STACKTRACE")
                    .type("CLOB")
                    .conversion(CLOB2STRING)
                    .map(EndPointLogImpl.Fields.STACKTRACE.fieldName())
                    .add();
            Column occurrence = table.column("OCCURRENCEID").number().since(version(10, 7)).add();

            table.foreignKey("FK_WS_EP_LOG_2_OCCURRENCE")
                    .references(WS_CALL_OCCURRENCE.name())
                    .on(occurrence)
                    .onDelete(DeleteRule.CASCADE)
                    .map(EndPointLogImpl.Fields.OCCURRENCE.fieldName())
                    .since(version(10, 7))
                    .add();
            table.primaryKey("SCS_PK_ENDPOINT_LOG").on(idColumn).add();
            table.autoPartitionOn(timestampColumn, LifeCycleClass.WEBSERVICES);
        }
    },
    WS_ENDPOINT_PROPS {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndPointProperty> table = dataModel.addTable(this.name(), EndPointProperty.class);
            table.map(EndPointPropertyImpl.class);
            table.since(version(10, 4));
            Column endPointColumn = table.column("ENDPOINTCFG").number().notNull().conversion(NUMBER2LONG).add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("VALUE").varChar(SHORT_DESCRIPTION_LENGTH).map("stringValue").add();
            table.primaryKey("PK_WS_ENDPOINT_PROPS").on(endPointColumn, nameColumn).add();
            table.foreignKey("FK_WS_ENDPOINT_PROPS").references(WS_ENDPOINTCFG.name())
                    .onDelete(DeleteRule.CASCADE).map("endPointCfg").reverseMap("properties").composition().on(endPointColumn).add();
        }
    };

    abstract void addTo(DataModel component);

}
