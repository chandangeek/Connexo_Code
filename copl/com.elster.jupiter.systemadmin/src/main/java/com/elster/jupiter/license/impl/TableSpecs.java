/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.license.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

public enum TableSpecs {
    LIC_LICENSE() {
        @Override
        void addTo(DataModel dataModel) {
            Table<License> table = dataModel.addTable(name(), License.class);
            table.map(LicenseImpl.class);
            table.setJournalTableName("LIC_LICENSEJRNL");
            Column appName = table.column("APPNAME").varChar().notNull().map("appKey").add();
            table.column("LICENSE").type("blob").map("signedObject").conversion(ColumnConversion.BLOB2BYTE).add();
            table.addAuditColumns();
            table.primaryKey("LIC_PK_LICENSE").on(appName).add();
            table.cache();

        }
    };

    abstract void addTo(DataModel dataModel);
}

