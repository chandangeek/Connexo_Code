package com.energyict.mdc.engine.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.cache.DeviceCacheImpl;

/**
 * Copyrights EnergyICT
 * Date: 08/05/14
 * Time: 11:53
 */
public enum TableSpecs {

    EISDEVICECACHE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceCache> table = dataModel.addTable(name(), DeviceCache.class);
            table.map(DeviceCacheImpl.class);
            Column device = table.column("RTUID").number().notNull().map("deviceId").add();
            table.column("CONTENT").type("BLOB(4000)").conversion(ColumnConversion.BLOB2BYTE).map("simpleCache").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            table.primaryKey("PK_EISDEVICECACHE").on(device).add();
            table.foreignKey("FK_EISDEVICECACHE_RTU").on(device).references(DeviceDataService.COMPONENTNAME, "EISRTU").map("device").add();
        }
    },
    ;

    abstract void addTo(DataModel component);
}
