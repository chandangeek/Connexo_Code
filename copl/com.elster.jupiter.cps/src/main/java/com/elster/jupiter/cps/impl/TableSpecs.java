package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

public enum TableSpecs {

    CPS_REGISTERED_CUSTOMPROPSET {
        @Override
        void addTo(DataModel dataModel) {
            Table<RegisteredCustomPropertySet> table = dataModel.addTable(name(), RegisteredCustomPropertySet.class);
            table.map(RegisteredCustomPropertySetImpl.class);
            Column id = table.addAutoIdColumn();
            Column logicalId = table
                    .column("LOGICALID")
                    .varChar()
                    .notNull()
                    .map(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.javaName())
                    .add();
            Column viewPrivilegesBits = table
                    .column("VIEWPRIVILEGES")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .map(RegisteredCustomPropertySetImpl.FieldNames.VIEW_PRIVILEGES.javaName())
                    .add();
            Column editPrivilegesBits = table
                    .column("EDITPRIVILEGES")
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