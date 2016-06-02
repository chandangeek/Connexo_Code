package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

/**
 * Created by bvn on 5/3/16.
 */
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
                    .type("CHAR(1)")
                    .conversion(ColumnConversion.CHAR2BOOLEAN)
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
            table.column("logLevel")
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(EndPointConfigurationImpl.Fields.LOG_LEVEL.fieldName())
                    .add();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");

            table.primaryKey("PK_WS_ENDPOINT").on(id).add();
        }
    };

    abstract void addTo(DataModel component);

}
