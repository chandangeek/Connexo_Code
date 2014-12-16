package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
    YFN_ED_IN_DG {
        void addTo(DataModel dataModel) {
            Table<EndDevicesInDeviceGroup.Entry> table = dataModel.addTable(name(), EndDevicesInDeviceGroup.Entry.class);
            table.map(EndDevicesInDeviceGroupImpl.EntryImpl.class);
            Column deviceIdColumn = table.column("ENDDEVICEID").number().notNull().conversion(NUMBER2LONG).map("endDeviceId").add();
            Column groupIdColumn = table.column("DEVICEGROUPID").number().notNull().conversion(NUMBER2LONG).map("groupId").add();
            table.primaryKey("YFG_PK_DEVICEINGROUP").on(deviceIdColumn , groupIdColumn).add();
            table.foreignKey("YFG_FK_DEVICEINGROUP2GROUP").references(MeteringGroupsService.COMPONENTNAME, "MTG_ED_GROUP").onDelete(RESTRICT).map("groupId").on(groupIdColumn).add();
            table.foreignKey("YFG_FK_DEVICEINGROUP2DEVICE").references(MeteringService.COMPONENTNAME, "MTR_ENDDEVICE").onDelete(RESTRICT).map("endDeviceId").on(deviceIdColumn).add();
        }
    };

    abstract void addTo(DataModel dataModel);
}
