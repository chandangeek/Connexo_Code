/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.cache.DeviceCacheImpl;

public enum TableSpecs {

    CES_DEVICECACHE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceCache> table = dataModel.addTable(name(), DeviceCache.class);
            table.map(DeviceCacheImpl.class);
            Column device = table.column("DEVICEID").number().notNull().add();
            table.addAuditColumns();
            table.column("CONTENT").type("BLOB").conversion(ColumnConversion.BLOB2BYTE).map("simpleCache").add();
            table.primaryKey("PK_CES_DEVICECACHE").on(device).add();
            table.foreignKey("FK_CES_DEVICECACHE_DEVICE")
                    .on(device)
                    .references(Device.class)
                    .map("device")
                    .add();
        }
    },;

    abstract void addTo(DataModel component);

}