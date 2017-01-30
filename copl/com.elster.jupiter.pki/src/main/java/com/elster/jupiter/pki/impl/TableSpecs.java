package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.KeyType;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;

public enum TableSpecs {
    PKI_CERTIFICATES {
        @Override
        void addTo(DataModel component) {

        }
    },
    PKI_KEYS {
        @Override
        void addTo(DataModel dataModel) {
            Table<KeyType> table = dataModel.addTable(this.name(), KeyType.class);
            table.map(KeyTypeImpl.class);
            Column id = table.addAutoIdColumn().since(Version.version(10,3));
            Column name = table.column("NAME").varChar().notNull().map(KeyTypeImpl.Fields.NAME.fieldName()).since(Version.version(10,3)).add();
            table.column("ALGORITHM").varChar().map(KeyTypeImpl.Fields.ALGORITHM.fieldName()).since(Version.version(10,3)).add();
            table.column("CURVE").varChar().map(KeyTypeImpl.Fields.CURVE.fieldName()).since(Version.version(10,3)).add();
            table.column("KEYSIZE").number().conversion(NUMBER2INT).map(KeyTypeImpl.Fields.KEY_SIZE.fieldName()).since(Version.version(10,3)).add();
            table.column("CRYPTOTYPE").number().map(KeyTypeImpl.Fields.CRYPTOGRAPHIC_TYPE.fieldName()).conversion(NUMBER2ENUM).since(Version.version(10,3)).add();
            table.primaryKey("PK_PKI_KEYTYPE").on(id).add();
            table.unique("UK_PKI_KEYTYPE").on(name).add();
        }
    };

    abstract void addTo(DataModel component);

}
