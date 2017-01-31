/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.BLOB2BYTE;

public enum TableSpecs {
    DVA_KEYSTORE {
        @Override
        void addTo(DataModel dataModel) {
            Table<OrmKeyStoreImpl> table = dataModel.addTable(name(), OrmKeyStoreImpl.class);
            table.map(OrmKeyStoreImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("STOREDATA").type("blob").conversion(BLOB2BYTE).map(OrmKeyStoreImpl.Fields.STORE_DATA.fieldName()).add();
            table.primaryKey("PK_DVA_STORE").on(idColumn).add();
        }
    };

    abstract void addTo(DataModel component);

}
