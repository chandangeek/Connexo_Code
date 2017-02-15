/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;

public enum TableSpecs {
    PKI_PLAINTEXTPK {
        @Override
        void addTo(DataModel dataModel) {
            Table<AbstractPlaintextPrivateKeyImpl> table = dataModel.addTable(this.name(), AbstractPlaintextPrivateKeyImpl.class);
            table.map(AbstractPlaintextPrivateKeyImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn().since(Version.version(10,3));
            table.column("KEY").varChar().map(AbstractPlaintextPrivateKeyImpl.Properties.ENCRYPTED_PRIVATE_KEY.fieldName()).since(Version.version(10,3)).add();
            table.column("CURVE").varChar().map(AbstractPlaintextPrivateKeyImpl.Properties.CURVE.fieldName()).since(Version.version(10,3)).add();
            table.column("KEYSIZE").number().map(AbstractPlaintextPrivateKeyImpl.Properties.KEYSIZE.fieldName()).since(Version.version(10,3)).add();
            table.primaryKey("PK_PKI_PPK").on(id).add();
        }
    };

    abstract void addTo(DataModel component);

}
