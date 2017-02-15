/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.mdm.usagepoint.data.impl.favorites.FavoriteUsagePointGroupImpl;
import com.elster.jupiter.mdm.usagepoint.data.impl.favorites.FavoriteUsagePointImpl;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.users.User;

public enum TableSpecs {
    FAV_FAVUSAGEPOINT {
        @Override
        void addTo(DataModel dataModel) {
            Table<FavoriteUsagePoint> table = dataModel.addTable(name(), FavoriteUsagePoint.class)
                    .since(Version.version(10, 3));
            table.map(FavoriteUsagePointImpl.class);

            Column userColumn = table.column("USERID").number().notNull()
                    .conversion(ColumnConversion.NUMBER2LONG).add();
            Column usagePointColumn = table.column("USAGEPOINT").number().notNull()
                    .conversion(ColumnConversion.NUMBER2LONG).add();
            table.addCreateTimeColumn("CREATETIME", "creationDate");
            table.column("UPCOMMENT").type("CLOB").map("comment").conversion(ColumnConversion.CLOB2STRING).add();

            table.primaryKey("FAV_PK_FAVUP").on(userColumn, usagePointColumn).add();
            table.foreignKey("FAV_FK_FAVUP_USER").on(userColumn).references(User.class)
                    .map("user").onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("FAV_FK_FAVUP_UP").on(usagePointColumn).references(UsagePoint.class)
                    .map("usagePoint").onDelete(DeleteRule.CASCADE).add();
        }
    },
    FAV_FAVUSAGEPOINTGROUP {
        @Override
        void addTo(DataModel dataModel) {
            Table<FavoriteUsagePointGroup> table = dataModel.addTable(name(), FavoriteUsagePointGroup.class)
                    .since(Version.version(10, 3));
            table.map(FavoriteUsagePointGroupImpl.class);

            Column userColumn = table.column("USERID").number().notNull()
                    .conversion(ColumnConversion.NUMBER2LONG).add();
            Column usagePointGroupColumn = table.column("USAGEPOINTGROUP").number().notNull()
                    .conversion(ColumnConversion.NUMBER2LONG).add();
            table.addCreateTimeColumn("CREATETIME", "creationDate");
            table.column("UPGCOMMENT").type("CLOB").map("comment").conversion(ColumnConversion.CLOB2STRING).add();

            table.primaryKey("FAV_PK_FAVUPGROUP").on(userColumn, usagePointGroupColumn).add();
            table.foreignKey("FAV_FK_FAVUPGROUP_USER").on(userColumn).references(User.class)
                    .map("user").onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("FAV_FK_FAVUPGROUP_GROUP").on(usagePointGroupColumn).references(UsagePointGroup.class)
                    .map("usagePointGroup").onDelete(DeleteRule.CASCADE).add();
        }
    };

    abstract void addTo(DataModel dataModel);
}
