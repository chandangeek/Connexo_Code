/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    CPS_REGISTERED_CUSTOMPROPSET {
        @Override
        void addTo(DataModel dataModel) {
            Table<RegisteredCustomPropertySet> table = dataModel.addTable(name(), RegisteredCustomPropertySet.class);
            table.map(RegisteredCustomPropertySetImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table.setJournalTableName("CPS_REG_CUSTOMPROPSET_JRNL").since(version(10, 2));
            Column logicalId = table
                    .column("LOGICALID")
                    .varChar()
                    .notNull()
                    .map(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName())
                    .add();
            table.column("SYSTEMDEFINED")
                .number()
                .notNull()
                .conversion(ColumnConversion.NUMBER2BOOLEAN)
                .map(RegisteredCustomPropertySetImpl.FieldNames.SYSTEM_DEFINED.javaName())
                .add();
            table.column("VIEWPRIVILEGES")
                .number()
                .notNull()
                .conversion(ColumnConversion.NUMBER2LONG)
                .map(RegisteredCustomPropertySetImpl.FieldNames.VIEW_PRIVILEGES.javaName())
                .add();
            table.column("EDITPRIVILEGES")
                .number()
                .notNull()
                .conversion(ColumnConversion.NUMBER2LONG)
                .map(RegisteredCustomPropertySetImpl.FieldNames.EDIT_PRIVILEGES.javaName())
                .add();
            table.primaryKey("PK_CPS_CPS").on(id).add();
            table.unique("UK_CPS_CPS").on(logicalId).add();
        }
    };

    abstract void addTo(DataModel dataModel);

}