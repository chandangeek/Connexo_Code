package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.PersistentKeyStore;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.BLOB2BYTE;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    DVA_KEYSTORE {
        @Override
        void addTo(DataModel dataModel) {
            Table<PersistentKeyStore> table = dataModel.addTable(name(), PersistentKeyStore.class);
            table.map(PersistentKeyStoreImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            /* Would want to use the following
             * table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)").since(Version.version(10, 3));
             * but that does not support the default value */
            table
                .column("DISCRIMINATOR")
                .type("char(1)")
                .notNull()
                .map(Column.TYPEFIELDNAME)
                .since(version(10, 3))
                .installValue("'D'")
                .add();
            table
                .column("NAME")
                .varChar()
                .map(PersistentKeyStoreImpl.Fields.NAME.fieldName())
                .since(version(10, 3))
                .add();
            table
                .column("STOREDATA")
                .type("blob")
                .conversion(BLOB2BYTE)
                .map(PersistentKeyStoreImpl.Fields.STORE_DATA.fieldName())
                .add();
            table.primaryKey("PK_DVA_STORE").on(idColumn).add();
        }
    };

    abstract void addTo(DataModel component);

}
