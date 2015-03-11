package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.orm.*;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.FirmwareUpgradeOptions;
import com.energyict.mdc.firmware.FirmwareVersion;

public enum TableSpecs {
    FWC_FIRMWAREVERSION {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareVersion> table = dataModel.addTable(name(),FirmwareVersion.class);
            table.map(FirmwareVersionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("FIRMWAREVERSION").varChar(Table.NAME_LENGTH).map("firmwareVersion").notNull().add();
            Column deviceTypeColumn = table.column("DEVICETYPE").number().notNull().add();
            table.column("TYPE").varChar(Table.NAME_LENGTH).map("firmwareType").conversion(ColumnConversion.CHAR2ENUM).add();
            table.column("STATUS").varChar(Table.NAME_LENGTH).map("firmwareStatus").conversion(ColumnConversion.CHAR2ENUM).add();
            table.column("FILE").type("blob").map("firmwareFile").conversion(ColumnConversion.BLOB2BYTE).add();
            table.addAuditColumns();
            table.primaryKey("FWC_PK_FIRMWARE").on(idColumn).add();
            table.foreignKey("FWC_FK_DEVICETYPE").on(deviceTypeColumn).map("deviceType").references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICETYPE").onDelete(DeleteRule.CASCADE).add();
        }
    },

    FWC_FIRMWAREUPGRADEOPTIONS {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareUpgradeOptions> table = dataModel.addTable(name(),FirmwareUpgradeOptions.class);
            table.map(FirmwareUpgradeOptionsImpl.class);
            Column deviceTypeColumn = table.column("DEVICETYPE").number().notNull().add();
            table.column("INSTALL").bool().map("install").add();
            table.column("ACTIVATE").bool().map("activate").add();
            table.column("ACTIVATEONDATE").bool().map("activateOnDate").add();
            table.addAuditColumns();
            table.primaryKey("FWC_PK_FIRMWAREUPGRADEOPTIONS").on(deviceTypeColumn).add();
            table.foreignKey("FWC_OPTIONS_FK_DEVICETYPE").on(deviceTypeColumn).map("deviceType").references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICETYPE").onDelete(DeleteRule.CASCADE).add();
            table.unique("FWC_OPTIONS_U_DEVICETYPE").on(deviceTypeColumn).add();
        }
    };

    abstract void addTo(DataModel component);
}
