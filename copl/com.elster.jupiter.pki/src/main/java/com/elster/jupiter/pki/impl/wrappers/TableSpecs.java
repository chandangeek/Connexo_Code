/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.impl.wrappers.assymetric.AbstractPlaintextPrivateKeyImpl;
import com.elster.jupiter.pki.impl.wrappers.symmetric.PlaintextSymmetricKey;

public enum TableSpecs {
    SSM_PLAINTEXTPK {
        @Override
        void addTo(DataModel dataModel) {
            Table<AbstractPlaintextPrivateKeyImpl> table = dataModel.addTable(this.name(), AbstractPlaintextPrivateKeyImpl.class);
            table.map(AbstractPlaintextPrivateKeyImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn().since(Version.version(10,3));
            table.column("KEY").varChar().map(AbstractPlaintextPrivateKeyImpl.Properties.ENCRYPTED_PRIVATE_KEY.fieldName()).since(Version.version(10,3)).add();
            Column keyTypeColumn = table.column("KEYTYPE")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .since(Version.version(10, 3))
                    .add();
            table.foreignKey("SSM_FK_PRIKEY_KT").on(keyTypeColumn).references(KeyType.class).map("keyTypeReference").add();
            table.primaryKey("PK_PKI_PRIKEY").on(id).add();
        }
    },
    SSM_PLAINTEXTSK {
        @Override
        void addTo(DataModel dataModel) {
            Table<PlaintextSymmetricKey> table = dataModel.addTable(this.name(), PlaintextSymmetricKey.class);
            table.map(PlaintextSymmetricKey.class);
            Column id = table.addAutoIdColumn().since(Version.version(10,3));
            table.column("KEY").varChar().map(PlaintextSymmetricKey.Properties.ENCRYPTED_KEY.fieldName()).since(Version.version(10,3)).add();
            Column keyTypeColumn = table.column("KEYTYPE")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .since(Version.version(10, 3))
                    .add();
            table.foreignKey("SSM_FK_SYMKEY_KT").on(keyTypeColumn).references(KeyType.class).map("keyTypeReference").add();
            table.primaryKey("PK_PKI_SYMKEY").on(id).add();
        }
    };

    abstract void addTo(DataModel component);

}
