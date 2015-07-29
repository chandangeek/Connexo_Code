package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignProperty;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.PassiveFirmwareVersion;

import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

public enum TableSpecs {
    FWC_FIRMWAREVERSION {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareVersion> table = dataModel.addTable(name(),FirmwareVersion.class);
            table.map(FirmwareVersionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column firmwareVersion = table.column("FIRMWAREVERSION").varChar(Table.NAME_LENGTH).map(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName()).notNull().add();
            Column deviceTypeColumn = table.column("DEVICETYPE").number().notNull().add();
            Column firmwareType = table.column("TYPE").number().map(FirmwareVersionImpl.Fields.FIRMWARETYPE.fieldName()).conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("STATUS").number().map(FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("FIRMWAREFILE").type("blob").map(FirmwareVersionImpl.Fields.FIRMWAREFILE.fieldName()).conversion(ColumnConversion.BLOB2BYTE).add();
            table.addAuditColumns();
            table.primaryKey("FWC_PK_FIRMWARE").on(idColumn).add();
            table.foreignKey("FWC_FK_DEVICETYPE").on(deviceTypeColumn).map(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName()).references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICETYPE").onDelete(DeleteRule.CASCADE).add();
            table.unique("FWC_UK_VERSIONTYPE").on(firmwareVersion, firmwareType, deviceTypeColumn).add();
        }
    },

    FWC_FIRMWAREMANAGEMENTOPTIONS {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareManagementOptions> table = dataModel.addTable(name(), FirmwareManagementOptions.class);
            table.map(FirmwareManagementOptionsImpl.class);
            Column deviceTypeColumn = table.column("DEVICETYPE").number().notNull().add();
            table.column("INSTALL").type("char(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(FirmwareManagementOptionsImpl.Fields.INSTALL.fieldName()).add();
            table.column("ACTIVATE").type("char(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(FirmwareManagementOptionsImpl.Fields.ACTIVATE.fieldName()).add();
            table.column("ACTIVATEONDATE").type("char(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(FirmwareManagementOptionsImpl.Fields.ACTIVATEONDATE.fieldName()).add();
            table.addAuditColumns();
            table.primaryKey("FWC_PK_FIRMWAREMGTOPTIONS").on(deviceTypeColumn).add();
            table.foreignKey("FWC_OPTIONS_FK_DEVICETYPE").on(deviceTypeColumn).map(FirmwareManagementOptionsImpl.Fields.DEVICETYPE.fieldName()).references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICETYPE").onDelete(DeleteRule.CASCADE).add();
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

    FWC_CAMPAIGN {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareCampaign> table = dataModel.addTable(name(), FirmwareCampaign.class);
            table.map(FirmwareCampaignImpl.class);

            Column idColumn = table.addAutoIdColumn();
            Column name = table.column("CAMPAIGN_NAME").varChar(NAME_LENGTH).map(FirmwareCampaignImpl.Fields.NAME.fieldName()).notNull().add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map(FirmwareCampaignImpl.Fields.STATUS.fieldName()).notNull().add();
            Column deviceType = table.column("DEVICE_TYPE").number().notNull().add();
            table.column("MANAGEMENT_OPTION").number().conversion(ColumnConversion.NUMBER2ENUM).map(FirmwareCampaignImpl.Fields.MANAGEMENT_OPTION.fieldName()).notNull().add();
            table.column("FIRMWARE_TYPE").number().conversion(ColumnConversion.NUMBER2ENUM).map(FirmwareCampaignImpl.Fields.FIRMWARE_TYPE.fieldName()).notNull().add();
            table.column("STARTED_ON").number().map(FirmwareCampaignImpl.Fields.STARTED_ON.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("FINISHED_ON").number().map(FirmwareCampaignImpl.Fields.FINISHED_ON.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();

            table.addAuditColumns();

            table.unique("UQ_FWC_CAMPAIGN_NAME").on(name).add();
            table.foreignKey("FK_FWC_CAMPAIGN_TO_D_TYPE")
                    .on(deviceType)
                    .references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICETYPE")
                    .map(FirmwareCampaignImpl.Fields.DEVICE_TYPE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.primaryKey("PK_FWC_CAMPAIGN").on(idColumn).add();
        }
    },

    FWC_CAMPAIGN_DEVICES {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceInFirmwareCampaign> table = dataModel.addTable(name(), DeviceInFirmwareCampaign.class);
            table.map(DeviceInFirmwareCampaignImpl.class);

            Column campaign = table.column("CAMPAIGN").number().notNull().add();
            Column device = table.column("DEVICE").number().notNull().add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map(DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName()).add();
            table.column("MESSAGE_ID").number().conversion(ColumnConversion.NUMBER2LONGNULLZERO).map(DeviceInFirmwareCampaignImpl.Fields.MESSAGE_ID.fieldName()).add();
            table.column("STARTED_ON").number().conversion(ColumnConversion.NUMBER2INSTANT).map(DeviceInFirmwareCampaignImpl.Fields.STARTED_ON.fieldName()).add();
            table.column("FINISHED_ON").number().conversion(ColumnConversion.NUMBER2INSTANT).map(DeviceInFirmwareCampaignImpl.Fields.FINISHED_ON.fieldName()).add();

            table.foreignKey("FK_FWC_DEVICE_TO_CAMPAIGN")
                    .on(campaign)
                    .references(FWC_CAMPAIGN.name())
                    .map(DeviceInFirmwareCampaignImpl.Fields.CAMPAIGN.fieldName())
                    .reverseMap(FirmwareCampaignImpl.Fields.DEVICES.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("FK_FWC_DEVICE_TO_DEVICE")
                    .on(device)
                    .references(DeviceDataServices.COMPONENT_NAME, "DDC_DEVICE")
                    .map(DeviceInFirmwareCampaignImpl.Fields.DEVICE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.primaryKey("PK_FWC_CAMPAIGN_DEVICES").on(campaign, device).add();
        }
    },

    FWC_CAMPAIGN_PROPS {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareCampaignProperty> table = dataModel.addTable(name(), FirmwareCampaignProperty.class);
            table.map(FirmwareCampaignPropertyImpl.class);

            Column campaign = table.column("CAMPAIGN").number().notNull().add();
            Column key = table.column("KEY").varChar(NAME_LENGTH).map(FirmwareCampaignPropertyImpl.Fields.KEY.fieldName()).notNull().add();
            table.column("VALUE").varChar(DESCRIPTION_LENGTH).map(FirmwareCampaignPropertyImpl.Fields.VALUE.fieldName()).notNull().add();

            table.foreignKey("FK_FWC_PROPS_TO_CAMPAIGN")
                    .on(campaign)
                    .references(FWC_CAMPAIGN.name())
                    .map(FirmwareCampaignPropertyImpl.Fields.CAMPAIGN.fieldName())
                    .reverseMap(FirmwareCampaignImpl.Fields.PROPERTIES.fieldName())
                    .composition()
                    .add();
            table.primaryKey("PK_FWC_CAMPAIGN_PROPS").on(campaign, key).add();
        }
    },

    FWC_CAMPAIGN_STATUS {
        @Override
        void addTo(DataModel dataModel) {
            Table<DevicesInFirmwareCampaignStatusImpl> table = dataModel.addTable(name(), DevicesInFirmwareCampaignStatusImpl.class);
            table.map(DevicesInFirmwareCampaignStatusImpl.class);

            Column campaign = table.column("CAMPAIGN").number().notNull().add();
            table.column("ONGOING").number().map(DevicesInFirmwareCampaignStatusImpl.Fields.STATUS_ONGOING.fieldName()).conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("SUCCESS").number().map(DevicesInFirmwareCampaignStatusImpl.Fields.STATUS_SUCCESS.fieldName()).conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("PENDING").number().map(DevicesInFirmwareCampaignStatusImpl.Fields.STATUS_PENDING.fieldName()).conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("FAILED").number().map(DevicesInFirmwareCampaignStatusImpl.Fields.STATUS_FAILED.fieldName()).conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("CONFIGURATIONERROR").number().map(DevicesInFirmwareCampaignStatusImpl.Fields.STATUS_CONFIGURATION_ERROR.fieldName()).conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("CANCELLED").number().map(DevicesInFirmwareCampaignStatusImpl.Fields.STATUS_CANCELLED.fieldName()).conversion(ColumnConversion.NUMBER2LONG).add();

            table.foreignKey("FK_FWC_STATUS_TO_CAMPAIGN")
                    .on(campaign)
                    .references(FWC_CAMPAIGN.name())
                    .map(DevicesInFirmwareCampaignStatusImpl.Fields.CAMPAIGN.fieldName())
                    .reverseMap(FirmwareCampaignImpl.Fields.DEVICES_STATUS.fieldName())
                    .composition()
                    .add();
            table.primaryKey("PK_FWC_CAMPAIGN_STATUS").on(campaign).add();
        }
    },
    ;

    abstract void addTo(DataModel dataModel);
}
