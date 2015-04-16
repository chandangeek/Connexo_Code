package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.orm.*;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareUpgradeOptions;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.PassiveFirmwareVersion;

import java.util.List;

public enum TableSpecs {
    FWC_FIRMWAREVERSION {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareVersion> table = dataModel.addTable(name(),FirmwareVersion.class);
            table.map(FirmwareVersionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("FIRMWAREVERSION").varChar(Table.NAME_LENGTH).map(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName()).notNull().add();
            Column deviceTypeColumn = table.column("DEVICETYPE").number().notNull().add();
            table.column("TYPE").number().map(FirmwareVersionImpl.Fields.FIRMWARETYPE.fieldName()).conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("STATUS").number().map(FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("FIRMWAREFILE").type("blob").map(FirmwareVersionImpl.Fields.FIRMWAREFILE.fieldName()).conversion(ColumnConversion.BLOB2BYTE).add();
            table.addAuditColumns();
            table.primaryKey("FWC_PK_FIRMWARE").on(idColumn).add();
            table.foreignKey("FWC_FK_DEVICETYPE").on(deviceTypeColumn).map(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName()).references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICETYPE").onDelete(DeleteRule.CASCADE).add();
        }
    },

    FWC_FIRMWAREUPGRADEOPTIONS {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareUpgradeOptions> table = dataModel.addTable(name(),FirmwareUpgradeOptions.class);
            table.map(FirmwareUpgradeOptionsImpl.class);
            Column deviceTypeColumn = table.column("DEVICETYPE").number().notNull().add();
            table.column("INSTALL").type("char(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(FirmwareUpgradeOptionsImpl.Fields.INSTALL.fieldName()).add();
            table.column("ACTIVATE").type("char(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(FirmwareUpgradeOptionsImpl.Fields.ACTIVATE.fieldName()).add();
            table.column("ACTIVATEONDATE").type("char(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(FirmwareUpgradeOptionsImpl.Fields.ACTIVATEONDATE.fieldName()).add();
            table.addAuditColumns();
            table.primaryKey("FWC_PK_FIRMWAREUPGRADEOPTIONS").on(deviceTypeColumn).add();
            table.foreignKey("FWC_OPTIONS_FK_DEVICETYPE").on(deviceTypeColumn).map(FirmwareUpgradeOptionsImpl.Fields.DEVICETYPE.fieldName()).references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICETYPE").onDelete(DeleteRule.CASCADE).add();
        }
    },

    FWC_ACTIVATEDFIRMWAREVERSION {
        @Override
        void addTo(DataModel dataModel){
            Table<ActivatedFirmwareVersion> table = dataModel.addTable(name(), ActivatedFirmwareVersion.class);
            table.map(ActivatedFirmwareVersionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column deviceColumn = table.column("DEVICEID").number().notNull().add();
            Column firmwareVersionColumn = table.column("FIRMWAREVERSION").number().notNull().add();
            table.column("LASTCHECKED").number().map("lastChecked").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.addIntervalColumns("interval");
            table.addAuditColumns();
            table.primaryKey("FWC_PK_ACTIVATEDVERSION").on(idColumn).add();
            table.foreignKey("FWC_ACTIVATED_FK_DEVICE").on(deviceColumn).map("device").references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("FWC_ACTIVATED_FK_FIRMWARE").references(FWC_FIRMWAREVERSION.name()).on(firmwareVersionColumn).map("firmwareVersion").onDelete(DeleteRule.CASCADE).add();
        }
    },

    FWC_PASSIVEFIRMWAREVERSION {
        @Override
        void addTo(DataModel dataModel){
            Table<PassiveFirmwareVersion> table = dataModel.addTable(name(), PassiveFirmwareVersion.class);
            table.map(PassiveFirmwareVersionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column deviceColumn = table.column("DEVICEID").number().notNull().add();
            Column firmwareVersionColumn = table.column("FIRMWAREVERSION").number().notNull().add();
            table.column("LASTCHECKED").number().map("lastChecked").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("ACTIVATEDATE").number().map("activateDate").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.addIntervalColumns("interval");
            table.addAuditColumns();
            table.primaryKey("FWC_PK_PASSIVEFIRMWAREVERSION").on(idColumn).add();
            table.foreignKey("FWC_PASSIVE_FK_DEVICE").on(deviceColumn).map("device").references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE").onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("FWC_PASSIVE_FK_FIRMWARE").references(FWC_FIRMWAREVERSION.name()).on(firmwareVersionColumn).map("firmwareVersion").onDelete(DeleteRule.CASCADE).add();
        }
    },
    ;

    abstract void addTo(DataModel component);
}
