package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

/**
 * Models the database tables that hold the data of the Custom Property Set bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (14:22)
 */
public enum TableSpecs {

    CPS_REGISTERED_CUSTOM_PROP_SET {
        @Override
        void addTo(DataModel dataModel) {
            Table<RegisteredCustomPropertySet> table = dataModel.addTable(name(), RegisteredCustomPropertySet.class);
            table.map(RegisteredCustomPropertySetImpl.class);
            Column id = table.addAutoIdColumn();
            Column logicalId = table
                    .column("LOGICALID")
                    .varChar()
                    .notNull()
                    .map(RegisteredCustomPropertySetImpl.FieldNames.LOGICAL_ID.name())
                    .add();
            table.primaryKey("PK_CPS_CPS").on(id).add();
            table.unique("UK_CPS_CPS").on(logicalId).add();
        }
    };

    abstract void addTo(DataModel dataModel);

}