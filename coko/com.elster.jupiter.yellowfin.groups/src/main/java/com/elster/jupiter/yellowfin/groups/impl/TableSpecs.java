package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

public enum TableSpecs {
    YFN_ADHOC_DG {
        void addTo(DataModel dataModel) {
            Table<AdHocDeviceGroupImpl> table = dataModel.addTable(name(), AdHocDeviceGroupImpl.class);
            table.map(AdHocDeviceGroupImpl.class);
            Column deviceIdColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.primaryKey("YFG_PK_ADHOCGROUP").on(deviceIdColumn).add();
        }
    },
    YFN_ED_IN_AHG {
        void addTo(DataModel dataModel) {
            Table<AdHocDeviceGroupImpl.AdHocEntryImpl> table = dataModel.addTable(name(), AdHocDeviceGroupImpl.AdHocEntryImpl.class);
            table.map(AdHocDeviceGroupImpl.AdHocEntryImpl.class);
            Column deviceIdColumn = table.column("ENDDEVICEID").number().notNull().conversion(NUMBER2LONG).map("deviceId").add();
            Column groupIdColumn = table.column("DEVICEGROUPID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
            table.primaryKey("YFG_PK_DEVICEINAHG").on(deviceIdColumn , groupIdColumn).add();
            //table.foreignKey("YFG_FK_DEVICEINAHG2GROUP").references(YFN_ADHOC_DG.name()).onDelete(CASCADE).map("groupId").on(groupIdColumn).add();
            //table.foreignKey("YFG_FK_DEVICEINAHG2DEVICE").references(MeteringService.COMPONENTNAME, "MTR_ENDDEVICE").onDelete(CASCADE).map("deviceId").on(deviceIdColumn).add();
        }
    },
    YFN_ED_IN_DG {
        void addTo(DataModel dataModel) {
            Table<DynamicDeviceGroupImpl.DynamicEntryImpl> table = dataModel.addTable(name(), DynamicDeviceGroupImpl.DynamicEntryImpl.class);
            table.map(DynamicDeviceGroupImpl.DynamicEntryImpl.class);
            Column deviceIdColumn = table.column("ENDDEVICEID").number().notNull().conversion(NUMBER2LONG).map("deviceId").add();
            Column groupIdColumn = table.column("DEVICEGROUPID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
            table.primaryKey("YFG_PK_DEVICEINDG").on(deviceIdColumn , groupIdColumn).add();
            //table.foreignKey("YFG_FK_DEVICEINDG2GROUP").references(MeteringGroupsService.COMPONENTNAME, "MTG_ED_GROUP").onDelete(CASCADE).map("groupId").on(groupIdColumn).add();
            //table.foreignKey("YFG_FK_DEVICEINDG2DEVICE").references(MeteringService.COMPONENTNAME, "MTR_ENDDEVICE").onDelete(CASCADE).map("deviceId").on(deviceIdColumn).add();
        }
    };

    abstract void addTo(DataModel dataModel);
}
