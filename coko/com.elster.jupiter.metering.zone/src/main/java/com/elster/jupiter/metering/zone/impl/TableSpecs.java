/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    MTZ_ZONETYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ZoneType> table = dataModel.addTable(name(), ZoneType.class);
            table.map(ZoneTypeImpl.class);
            table.since(version(10, 6));
            table.setJournalTableName("MTZ_ZONETYPEJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column application = table.column("APPLICATION").varChar(10).notNull().map("application").add();
            Column name = table.column("NAME").varChar(NAME_LENGTH).notNull().map("typeName").add();
            table.primaryKey("MTZ_PK_ZONETYPE").on(idColumn).add();
            table.unique("MTZ_UK_ZONETYPE").on(application, name).add();
            table.addAuditColumns();
        }
    },
    MTZ_ZONE {
        @Override
        void addTo(DataModel dataModel) {
            Table<Zone> table = dataModel.addTable(name(), Zone.class);
            table.map(ZoneImpl.class);
            table.since(version(10, 6));
            table.setJournalTableName("MTZ_ZONEJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            Column zoneType = table.column("ZONETYPE").number().notNull().conversion(NUMBER2LONG).map("zoneTypeId").add();
            table.addAuditColumns();
            table.primaryKey("MTZ_PK_ZONE").on(idColumn).add();
            table.unique("MTZ_UK_ZONE").on(zoneType, name).add();
            table
                    .foreignKey("MTZ_FK_ZONETYPE")
                    .on(zoneType)
                    .references(MTZ_ZONETYPE.name())
                    .map("zoneType")
                    .add();
        }
    };

    abstract void addTo(DataModel component);
}
