/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.Audit;
import com.elster.jupiter.audit.AuditLog;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    ADT_AUDIT_TRAIL {
        public void addTo(DataModel dataModel) {
            Table<AuditTrail> table = dataModel.addTable(name(), AuditTrail.class);
            table.map(AuditTrailImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.since(version(10, 6));
            table.column("DOMAIN").varChar(NAME_LENGTH).notNull().map(AuditTrailImpl.Field.DOMAIN.fieldName()).add();
            table.column("CONTEXT").varChar(NAME_LENGTH).notNull().map(AuditTrailImpl.Field.CONTEXT.fieldName()).add();
            table.column("MODTIMESTART").number().conversion(NUMBER2INSTANT).notNull().map(AuditTrailImpl.Field.MODTIMESTART.fieldName()).add();
            table.column("MODTIMEEND").number().conversion(NUMBER2INSTANT).notNull().map(AuditTrailImpl.Field.MODTIMEEND.fieldName()).add();
            table.column("TABLENAME").varChar(NAME_LENGTH).notNull().map(AuditTrailImpl.Field.TABLENAME.fieldName()).add();
            table.column("PKCOLUMN1").varChar(NAME_LENGTH).map(AuditTrailImpl.Field.PKCOLUMN1.fieldName()).add();
            table.column("PKCOLUMN2").varChar(NAME_LENGTH).map(AuditTrailImpl.Field.PKCOLUMN2.fieldName()).add();
            table.column("PKCOLUMN3").varChar(NAME_LENGTH).map(AuditTrailImpl.Field.PKCOLUMN3.fieldName()).add();
            table.column("OPERATION").number().conversion(NUMBER2ENUM).map(AuditTrailImpl.Field.OPERATION.fieldName()).add();
            table.column("CREATETIME").number().conversion(NUMBER2INSTANT).map(AuditTrailImpl.Field.CREATETIME.fieldName()).add();
            table.column("USERNAME").varChar(NAME_LENGTH).notNull().map(AuditTrailImpl.Field.USERNAME.fieldName()).add();
            table.primaryKey("ADT_PK_AUDIT_TAIL").on(idColumn).add();
        }
    },
    ADT_AUDIT {
        public void addTo(DataModel dataModel) {
            Table<Audit> table = dataModel.addTable(name(), Audit.class);
            table.map(AuditImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.since(version(10, 6));
            table.column("TABLENAME").varChar(NAME_LENGTH).notNull().map(AuditImpl.Field.TABLENAME.fieldName()).add();
            table.column("REFERENCE").varChar(DESCRIPTION_LENGTH).notNull().map(AuditImpl.Field.REFERENCE.fieldName()).add();
            table.column("SREFERENCE").varChar(DESCRIPTION_LENGTH).notNull().map(AuditImpl.Field.SREFERENCE.fieldName()).add();
            table.column("DOMAIN").varChar(NAME_LENGTH).notNull().map(AuditImpl.Field.DOMAIN.fieldName()).add();
            table.column("CONTEXT").varChar(NAME_LENGTH).notNull().map(AuditImpl.Field.CONTEXT.fieldName()).add();
            table.column("OPERATION").number().conversion(NUMBER2ENUM).map(AuditImpl.Field.OPERATION.fieldName()).add();
            table.column("CREATETIME").number().conversion(NUMBER2INSTANT).map(AuditImpl.Field.CREATETIME.fieldName()).add();
            table.column("USERNAME").varChar(NAME_LENGTH).notNull().map(AuditImpl.Field.USERNAME.fieldName()).add();
            table.primaryKey("ADT_PK_AUDIT").on(idColumn).add();
        }
    },
    ADT_AUDIT_LOG {
        public void addTo(DataModel dataModel) {
            Table<AuditLog> table = dataModel.addTable(name(), AuditLog.class);
            table.map(AuditLogImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.since(version(10, 6));
            Column auditId = table.column("AUDITID").number().notNull().conversion(NUMBER2LONG).add();
            table.column("TABLENAME").varChar(NAME_LENGTH).notNull().map(AuditLogImpl.Field.TABLENAME.fieldName()).add();
            table.column("REFERENCE").varChar(DESCRIPTION_LENGTH).notNull().map(AuditLogImpl.Field.REFERENCE.fieldName()).add();
            table.foreignKey("FK_ADT_AUDIT_LOG_AUDIT")
                    .on(auditId)
                    .references(ADT_AUDIT.name())
                    .map(AuditLogImpl.Field.AUDIT.fieldName())
                    .reverseMap("auditLogs")
                    .composition()
                    .add();
            table.primaryKey("ADT_PK_AUDIT_LOG").on(idColumn).add();
        }
    };

    public abstract void addTo(DataModel dataModel);
}