/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    ADT_AUDIT_TRAIL {
        public void addTo(DataModel dataModel) {
            Table<AuditTrail> table = dataModel.addTable(name(), AuditTrail.class);
            table.map(AuditTrailImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.since(version(10, 6));
            Column domainContext = table.column("DOMAINCONTEXT").number().conversion(NUMBER2ENUM).notNull().map(AuditTrailImpl.Field.DOMAINCONTEXT.fieldName()).add();
            table.column("MODTIMESTART").number().conversion(NUMBER2INSTANT).notNull().map(AuditTrailImpl.Field.MODTIMESTART.fieldName()).add();
            table.column("MODTIMEEND").number().conversion(NUMBER2INSTANT).notNull().map(AuditTrailImpl.Field.MODTIMEEND.fieldName()).add();
            Column pkDomain = table.column("PKDOMAIN").number().notNull().conversion(NUMBER2LONG).map(AuditTrailImpl.Field.PKDOMAIN.fieldName()).add();
            table.column("PKCONTEXT1").number().notNull().conversion(NUMBER2LONG).map(AuditTrailImpl.Field.PKCONTEXT1.fieldName()).add();
            table.column("PKCONTEXT2").number().notNull().conversion(NUMBER2LONG).map(AuditTrailImpl.Field.PKCONTEXT2.fieldName()).add();
            table.column("OPERATION").number().conversion(NUMBER2ENUM).notNull().map(AuditTrailImpl.Field.OPERATION.fieldName()).add();
            table.column("CREATETIME").number().conversion(NUMBER2INSTANT).notNull().map(AuditTrailImpl.Field.CREATETIME.fieldName()).add();
            table.column("USERNAME").varChar(NAME_LENGTH).notNull().map(AuditTrailImpl.Field.USERNAME.fieldName()).add();
            table.primaryKey("ADT_PK_AUDIT_TAIL").on(idColumn).add();
            table.index("IX_ADT_AUDIT_TRAIL_DC_PKD").on(pkDomain, domainContext).since(version(10, 8, 1)).add();
        }
    };

    public abstract void addTo(DataModel dataModel);
}
