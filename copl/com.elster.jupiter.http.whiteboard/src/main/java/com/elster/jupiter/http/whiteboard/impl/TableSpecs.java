/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.UserJWT;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;

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
    },
    HTW_JWTSTORE(UserJWT.class) {
        @Override
        void describeTable(Table table) {
            table.since(Version.version(10, 8));
            table.map(UserJWT.class);

            Column jwtIdColumn = table.column("JWT_ID").varChar().notNull().map("jwtId").add();
            Column userIdColumn = table.column("USER_ID").number().map("userId").add();
            Column tokenColumn = table.column("TOKEN").varChar().notNull().map("token").add();
            Column expirationDateColumn = table.column("EXPIRATION_DATE").number().conversion(NUMBER2INSTANT).map("expirationDate").add();

            table.addCreateTimeColumn("CREATION_DATE", "creationDate");

            table.primaryKey("JWT_PK_JWTSTORE").on(jwtIdColumn).add();
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