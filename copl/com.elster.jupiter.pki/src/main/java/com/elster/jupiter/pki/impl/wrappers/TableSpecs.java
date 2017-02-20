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
            Table<AbstractPlaintextPrivateKeyImpl> table = dataModel.addTable(this.name(), AbstractPlaintextPrivateKeyImpl.class).since(Version.version(10,3));
            table.map(AbstractPlaintextPrivateKeyImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn().since(Version.version(10,3));
            table.column("KEY")
                    .varChar()
                    .map(AbstractPlaintextPrivateKeyImpl.Fields.ENCRYPTED_KEY.fieldName())
                    .add();
            table.column("EXPIRATION")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(AbstractPlaintextPrivateKeyImpl.Fields.EXPIRATION.fieldName())
                    .add();
            Column keyTypeColumn = table.column("KEYTYPE")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
            table.foreignKey("SSM_FK_PRIKEY_KT").on(keyTypeColumn).references(KeyType.class).map(AbstractPlaintextPrivateKeyImpl.Fields.KEY_TYPE.fieldName()).add();
            table.primaryKey("PK_PKI_PRIKEY").on(id).add();
        }
    },
    SSM_PLAINTEXTSK {
        @Override
        void addTo(DataModel dataModel) {
            Table<PlaintextSymmetricKey> table = dataModel.addTable(this.name(), PlaintextSymmetricKey.class).since(Version.version(10, 3));
            table.map(PlaintextSymmetricKey.class);
            Column id = table.addAutoIdColumn().since(Version.version(10,3));
            table.column("KEY")
                    .varChar()
                    .map(PlaintextSymmetricKey.Fields.ENCRYPTED_KEY.fieldName())
                    .since(Version.version(10,3))
                    .add();
            Column keyTypeColumn = table.column("KEYTYPE")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
            table.column("EXPIRATION")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(PlaintextSymmetricKey.Fields.EXPIRATION.fieldName())
                    .add();
            table.foreignKey("SSM_FK_SYMKEY_KT").on(keyTypeColumn)
                    .references(KeyType.class)
                    .map(PlaintextSymmetricKey.Fields.KEY_TYPE.fieldName())
                    .add();
            table.primaryKey("PK_PKI_SYMKEY").on(id).add();
        }
    };

    abstract void addTo(DataModel component);

}
