/*
 *
 *  * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users.blacklist.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.blacklist.BlackListToken;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.MAX_STRING_LENGTH;
/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 12/30/2019 (11:04)
 */
public enum TableSpecs {
    BLT_BLACKLISTEDTOKEN {
        @Override
        void addTo(DataModel dataModel) {
            Table<BlackListToken> table = dataModel.addTable(name(), BlackListToken.class);
            table.map(BlackListTokenImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("USERID").map("userId").number().conversion(NUMBER2LONG).notNull().add();
            table.column("TOKEN").map("token").varChar(MAX_STRING_LENGTH).notNull().add();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.primaryKey("USR_PK_ID").on(idColumn).add();
        }
    };
    abstract void addTo(DataModel dataModel);
}
