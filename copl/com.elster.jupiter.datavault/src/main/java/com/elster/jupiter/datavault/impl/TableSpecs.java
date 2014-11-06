package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.BLOB2BYTE;

public enum TableSpecs {
    DVA_KEYSTORE {
        @Override
        void addTo(DataModel dataModel) {
            Table<KeyStoreImpl> table = dataModel.addTable(name(), KeyStoreImpl.class);
            table.map(KeyStoreImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("STOREDATA").number().conversion(BLOB2BYTE).map(KeyStoreImpl.Fields.STORE_DATA.fieldName()).add();
            table.primaryKey("PK_DVA_STORE").on(idColumn).add();
        }
    };

    abstract void addTo(DataModel component);

}
