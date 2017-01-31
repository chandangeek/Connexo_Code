/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

public enum TableSpecs {
    HTW_KEYSTORE(KeyStoreImpl.class) {

        @Override
        void describeTable(Table table) {
            table.map(KeyStoreImpl.class);
            Column idColumn = table.column("ID").number().notNull().map("id").add();
            Column privateKeyColumn = table.column("PRIVATE_KEY").varChar(2048).notNull().map("privateKey").add();
            Column publicKeyColumn = table.column("PUBLIC_KEY").varChar(2048).notNull().map("publicKey").add();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.addModTimeColumn("MODTIME","modTime");
            table.primaryKey("HTW_PK_KEYSTORE").on(idColumn).add();
            table.unique("HTW_U_PUBKEY").on(publicKeyColumn).add();
            table.unique("HTW_U_PRVKEY").on(privateKeyColumn).add();
        }
    };

    private final Class<?> api;

    TableSpecs(Class<?> api) {
        this.api = api;
    }

    public void addTo(DataModel component) {
        Table table = component.addTable(name(), api);
        describeTable(table);
    }


    abstract void describeTable(Table table);
}