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
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.impl.wrappers.asymmetric.AbstractPlaintextPrivateKeyWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.symmetric.KeyImpl;
import com.elster.jupiter.pki.impl.wrappers.symmetric.PlaintextPassphraseImpl;
import com.elster.jupiter.pki.impl.wrappers.symmetric.PlaintextSymmetricKeyImpl;

import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    SSM_PLAINTEXTPK {
        @Override
        void addTo(DataModel dataModel) {
            Table<PrivateKeyWrapper> table = dataModel.addTable(this.name(), PrivateKeyWrapper.class).since(Version.version(10,3));
            table.map(AbstractPlaintextPrivateKeyWrapperImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            table.column("KEY")
                    .varChar()
                    .map(AbstractPlaintextPrivateKeyWrapperImpl.Fields.ENCRYPTED_KEY.fieldName())
                    .add();
            table.column("EXPIRATION")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(AbstractPlaintextPrivateKeyWrapperImpl.Fields.EXPIRATION.fieldName())
                    .add();
            Column keyTypeColumn = table.column("KEYTYPE")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
            table.foreignKey("SSM_FK_PRIKEY_KT")
                    .on(keyTypeColumn)
                    .references(KeyType.class)
                    .map(AbstractPlaintextPrivateKeyWrapperImpl.Fields.KEY_TYPE.fieldName())
                    .add();
            table.primaryKey("PK_SSM_PLAINTEXTPK")
                    .on(id)
                    .add();
        }
    },

    SSM_PLAINTEXTSK {
        @Override
        void addTo(DataModel dataModel) {
            Table<SymmetricKeyWrapper> table = dataModel.addTable(this.name(), SymmetricKeyWrapper.class);
            table.map(KeyImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)")
                    .since(version(10, 4,3));
            table.column("KEY")
                    .varChar()
                    .map(PlaintextSymmetricKeyImpl.Fields.ENCRYPTED_KEY.fieldName())
                    .add();
            Column keyTypeColumn = table.column("KEYTYPE")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
            table.column("EXPIRATION")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(PlaintextSymmetricKeyImpl.Fields.EXPIRATION.fieldName())
                    .add();
            table.column("LABEL")
                    .varChar(SHORT_DESCRIPTION_LENGTH)
                    .map(KeyImpl.Fields.LABEL.fieldName())
                    .since(version(10,4,2))
                    .add();
            table.foreignKey("SSM_FK_SYMKEY_KT").on(keyTypeColumn)
                    .references(KeyType.class)
                    .map(PlaintextSymmetricKeyImpl.Fields.KEY_TYPE.fieldName())
                    .add();
            table.primaryKey("PK_SSM_PLAINTEXTSK").on(id).add();
        }
    },
    SSM_PLAINTEXTPW {
        @Override
        void addTo(DataModel dataModel) {
            Table<PlaintextPassphrase> table = dataModel.addTable(this.name(), PlaintextPassphrase.class).since(Version.version(10, 3));
            table.map(PlaintextPassphraseImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("PASSPHRASE")
                    .varChar()
                    .map(PlaintextPassphraseImpl.Fields.PASSPHRASE.fieldName())
                    .add();
            Column keyTypeColumn = table.column("KEYTYPE")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
            table.column("EXPIRATION")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(PlaintextPassphraseImpl.Fields.EXPIRATION.fieldName())
                    .add();
            table.foreignKey("SSM_FK_PASSPHRASE_KT").on(keyTypeColumn)
                    .references(KeyType.class)
                    .map(PlaintextPassphraseImpl.Fields.KEY_TYPE.fieldName())
                    .add();
            table.primaryKey("PK_SSM_PLAINTEXTPW").on(id).add();
        }
    }
    ;

    abstract void addTo(DataModel component);

}
