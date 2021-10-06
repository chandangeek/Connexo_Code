/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCampaignManagementOptions;
import com.energyict.mdc.firmware.FirmwareCampaignProperty;
import com.energyict.mdc.firmware.FirmwareCampaignVersionStateShapshot;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.PassiveFirmwareVersion;
import com.energyict.mdc.firmware.SecurityAccessorOnDeviceType;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignDomainExtension;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignItemPersistenceSupport;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignPersistenceSupport;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignPropertyImpl;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

import com.google.common.collect.Range;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2ENUM;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    FWC_FIRMWAREVERSION {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareVersion> table = dataModel.addTable(name(), FirmwareVersion.class).alsoReferredToAs(BaseFirmwareVersion.class);
            table.map(FirmwareVersionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column firmwareVersion = table.column("FIRMWAREVERSION").varChar(Table.NAME_LENGTH).map(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName()).notNull().add();
            Column deviceTypeColumn = table.column("DEVICETYPE").number().notNull().add();
            Column firmwareType = table.column("TYPE").number().map(FirmwareVersionImpl.Fields.FIRMWARETYPE.fieldName()).conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("STATUS").number().map(FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).conversion(ColumnConversion.NUMBER2ENUM).add();
            table.column("FIRMWAREFILE").blob().map(FirmwareVersionImpl.Fields.FIRMWAREFILE.fieldName()).add();
            table.column("IMAGEIDENTIFIER").varChar(80).map(FirmwareVersionImpl.Fields.IMAGEIDENTIFIER.fieldName()).add().since(version(10, 4));
            table.addAuditColumns();
            Column rankColumn = table.column(FirmwareVersionImpl.Fields.RANK.name())
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(FirmwareVersionImpl.Fields.RANK.fieldName())
                    .since(Version.version(10, 6))
                    .installValue("0")
                    .add();
            Column meterFWDependency = table.column(FirmwareVersionImpl.Fields.METER_FW_DEP.name()).number().since(Version.version(10, 6)).add();
            Column communicationFWDependency = table.column(FirmwareVersionImpl.Fields.COM_FW_DEP.name()).number().since(Version.version(10, 6)).add();
            Column auxiliaryFWDependency = table.column(FirmwareVersionImpl.Fields.AUX_FW_DEP.name()).number().since(Version.version(10, 7)).add();
            table.primaryKey("FWC_PK_FIRMWARE").on(idColumn).add();
            table.foreignKey("FWC_FK_DEVICETYPE")
                    .on(deviceTypeColumn)
                    .references(DeviceType.class)
                    .map(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName())
                    .onDelete(CASCADE)
                    .add();
            table.unique("FWC_UK_VERSIONTYPE").on(firmwareVersion, firmwareType, deviceTypeColumn).add();
            table.index("FWC_IDX_FW_BY_RANK").on(deviceTypeColumn, rankColumn).compress(1).add().since(Version.version(10, 6));
            table.foreignKey("FWC_FK_FW_METER_FW_DEP")
                    .on(meterFWDependency)
                    .references(FirmwareVersion.class)
                    .map(FirmwareVersionImpl.Fields.METER_FW_DEP.fieldName())
                    .since(Version.version(10, 6))
                    .add();
            table.foreignKey("FWC_FK_FW_COM_FW_DEP")
                    .on(communicationFWDependency)
                    .references(FirmwareVersion.class)
                    .map(FirmwareVersionImpl.Fields.COM_FW_DEP.fieldName())
                    .since(Version.version(10, 6))
                    .add();
            table.foreignKey("FWC_FK_FW_AUX_FW_DEP")
                    .on(auxiliaryFWDependency)
                    .references(FirmwareVersion.class)
                    .map(FirmwareVersionImpl.Fields.AUX_FW_DEP.fieldName())
                    .since(Version.version(10, 7))
                    .add();
        }
    },

    FWC_FIRMWAREMANAGEMENTOPTIONS {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareManagementOptions> table = dataModel.addTable(name(), FirmwareManagementOptions.class);
            table.map(FirmwareManagementOptionsImpl.class);
            Column deviceTypeColumn = table.column("DEVICETYPE").number().notNull().add();
            table.column(FirmwareManagementOptionsImpl.Fields.INSTALL.name())
                    .type("char(1)")
                    .conversion(ColumnConversion.CHAR2BOOLEAN)
                    .map(FirmwareManagementOptionsImpl.Fields.INSTALL.fieldName())
                    .add();
            table.column(FirmwareManagementOptionsImpl.Fields.ACTIVATE.name())
                    .type("char(1)")
                    .conversion(ColumnConversion.CHAR2BOOLEAN)
                    .map(FirmwareManagementOptionsImpl.Fields.ACTIVATE.fieldName())
                    .add();
            table.column(FirmwareManagementOptionsImpl.Fields.ACTIVATEONDATE.name())
                    .type("char(1)")
                    .conversion(ColumnConversion.CHAR2BOOLEAN)
                    .map(FirmwareManagementOptionsImpl.Fields.ACTIVATEONDATE.fieldName())
                    .add();
            table.setJournalTableName("FWC_FIRMWAREMNGMNTOPTIONSJRNL").since(version(10, 2));
            table.addAuditColumns();
            addCheckConfigurationColumnFor10_6(table, FirmwareManagementOptionsImpl.Fields.CHK_TARGET_FW_FINAL, "'Y'");
            addCheckConfigurationColumnFor10_6(table, FirmwareManagementOptionsImpl.Fields.CHK_TARGET_FW_TEST, "'Y'");
            addCheckConfigurationColumnFor10_6(table, FirmwareManagementOptionsImpl.Fields.CHK_CURRENT_FW, "'N'");
            addCheckConfigurationColumnFor10_6(table, FirmwareManagementOptionsImpl.Fields.CHK_MASTER_FW_FINAL, "'Y'");
            addCheckConfigurationColumnFor10_6(table, FirmwareManagementOptionsImpl.Fields.CHK_MASTER_FW_TEST, "'N'");
            table.primaryKey("FWC_PK_FIRMWAREMGTOPTIONS").on(deviceTypeColumn).add();
            table.foreignKey("FWC_OPTIONS_FK_DEVICETYPE")
                    .on(deviceTypeColumn)
                    .references(DeviceType.class)
                    .map(FirmwareManagementOptionsImpl.Fields.DEVICETYPE.fieldName())
                    .add();
        }

        private Column addCheckConfigurationColumnFor10_6(Table<FirmwareManagementOptions> table, FirmwareManagementOptionsImpl.Fields descriptor, String defaultValue) {
            return table.column(descriptor.name())
                    .bool()
                    .map(descriptor.fieldName())
                    .since(Version.version(10, 6))
                    .installValue(defaultValue)
                    .add();
        }
    },

    FWC_CAMPAIGN_VERSION_SNAPSHOT {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareCampaignVersionStateShapshot> table = dataModel.addTable(name(), FirmwareCampaignVersionStateShapshot.class).since(version(10, 7));
            table.map(FirmwareCampaignVersionSnapshotImpl.class);
            Column firmwareCampaignColumn = table.column("FW_CAMPAIGN").number().notNull().add();
            Column cps = table.column("CPS_ID")
                    .number()
                    .notNull()
                    .add();

            Column version = table.column(FirmwareCampaignVersionSnapshotImpl.Fields.FIRMWAREVERSION.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(FirmwareCampaignVersionSnapshotImpl.Fields.FIRMWAREVERSION.fieldName())
                    .add();
            Column type = table.column(FirmwareCampaignVersionSnapshotImpl.Fields.FIRMWARETYPE.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .conversion(CHAR2ENUM)
                    .map(FirmwareCampaignVersionSnapshotImpl.Fields.FIRMWARETYPE.fieldName())
                    .add();
            table.column(FirmwareCampaignVersionSnapshotImpl.Fields.FIRMWARESTATUS.name())
                    .varChar(NAME_LENGTH)
                    .conversion(CHAR2ENUM)
                    .map(FirmwareCampaignVersionSnapshotImpl.Fields.FIRMWARESTATUS.fieldName())
                    .add();
            table.column(FirmwareCampaignVersionSnapshotImpl.Fields.IMAGEIDENTIFIER.name())
                    .varChar(NAME_LENGTH)
                    .map(FirmwareCampaignVersionSnapshotImpl.Fields.IMAGEIDENTIFIER.fieldName())
                    .add();
            table.column(FirmwareCampaignVersionSnapshotImpl.Fields.RANK.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(FirmwareCampaignVersionSnapshotImpl.Fields.RANK.fieldName())
                    .add();
            table.column(FirmwareCampaignVersionSnapshotImpl.Fields.METER_FW_DEP.name())
                    .varChar(NAME_LENGTH)
                    .map(FirmwareCampaignVersionSnapshotImpl.Fields.METER_FW_DEP.fieldName())
                    .add();
            table.column(FirmwareCampaignVersionSnapshotImpl.Fields.COM_FW_DEP.name())
                    .varChar(NAME_LENGTH)
                    .map(FirmwareCampaignVersionSnapshotImpl.Fields.COM_FW_DEP.fieldName())
                    .add();
            table.column(FirmwareCampaignVersionSnapshotImpl.Fields.AUX_FW_DEP.name())
                    .varChar(NAME_LENGTH)
                    .map(FirmwareCampaignVersionSnapshotImpl.Fields.AUX_FW_DEP.fieldName())
                    .add();

            table.primaryKey("FWC_PK_VERSION_SNAPSHOT").on(firmwareCampaignColumn, cps, version, type).add();
            table.foreignKey("FK_FWC_VRST_TO_CAMPAIGN")
                    .on(firmwareCampaignColumn, cps)
                    .references(FirmwareCampaignDomainExtension.class)
                    .onDelete(CASCADE)
                    .map(FirmwareCampaignVersionSnapshotImpl.Fields.FWRCAMPAIGN.fieldName())
                    .add();
        }
    },

    FWC_CAMPAIGN_CHECK_OPTIONS {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareCampaignManagementOptions> table = dataModel.addTable(name(), FirmwareCampaignManagementOptions.class).since(version(10, 7));
            table.map(FirmwareCampaignManagementOptionsImpl.class);
            Column firmwareCampaignColumn = table.column("FW_CAMPAIGN").number().notNull().add();
            Column cps = table.column("CPS_ID")
                    .number()
                    .notNull()
                    .add();
            addCheckConfigurationColumnFor10_7(table, FirmwareCampaignManagementOptionsImpl.Fields.CHK_TARGET_FW_FINAL, "'Y'");
            addCheckConfigurationColumnFor10_7(table, FirmwareCampaignManagementOptionsImpl.Fields.CHK_TARGET_FW_TEST, "'Y'");
            addCheckConfigurationColumnFor10_7(table, FirmwareCampaignManagementOptionsImpl.Fields.CHK_CURRENT_FW, "'N'");
            addCheckConfigurationColumnFor10_7(table, FirmwareCampaignManagementOptionsImpl.Fields.CHK_MASTER_FW_FINAL, "'Y'");
            addCheckConfigurationColumnFor10_7(table, FirmwareCampaignManagementOptionsImpl.Fields.CHK_MASTER_FW_TEST, "'N'");
            table.primaryKey("FWC_PK_CHECK_OPTIONS").on(firmwareCampaignColumn, cps).add();
            table.foreignKey("FK_FWC_CHECKOPT_TO_CAMPAIGN")
                    .on(firmwareCampaignColumn, cps)
                    .references(FirmwareCampaignDomainExtension.class)
                    .onDelete(CASCADE)
                    .map(FirmwareCampaignManagementOptionsImpl.Fields.FWRCAMPAIGN.fieldName())
                    .add();
        }

        private Column addCheckConfigurationColumnFor10_7(Table<FirmwareCampaignManagementOptions> table, FirmwareCampaignManagementOptionsImpl.Fields descriptor, String defaultValue) {
            return table.column(descriptor.name())
                    .bool()
                    .map(descriptor.fieldName())
                    .installValue(defaultValue)
                    .add();
        }
    },

    FWC_ACTIVATEDFIRMWAREVERSION {
        @Override
        void addTo(DataModel dataModel) {
            Table<ActivatedFirmwareVersion> table = dataModel.addTable(name(), ActivatedFirmwareVersion.class);
            table.map(ActivatedFirmwareVersionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column deviceColumn = table.column("DEVICEID").number().notNull().add();
            Column firmwareVersionColumn = table.column("FIRMWAREVERSION").number().notNull().add();
            table.column("LASTCHECKED").number().map("lastChecked").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.addIntervalColumns("interval");
            table.addAuditColumns().forEach(column -> column.upTo(version(10, 2)));
            table.primaryKey("FWC_PK_ACTIVATEDVERSION").on(idColumn).add();
            table
                    .foreignKey("FWC_ACTIVATED_FK_DEVICE")
                    .on(deviceColumn)
                    .map("device")
                    .references(Device.class)
                    .onDelete(CASCADE)
                    .add();
            table
                    .foreignKey("FWC_ACTIVATED_FK_FIRMWARE")
                    .on(firmwareVersionColumn)
                    .references(FWC_FIRMWAREVERSION.name())
                    .map("firmwareVersion")
                    .onDelete(CASCADE)
                    .add();
        }
    },

    FWC_PASSIVEFIRMWAREVERSION {
        @Override
        void addTo(DataModel dataModel) {
            Table<PassiveFirmwareVersion> table = dataModel.addTable(name(), PassiveFirmwareVersion.class);
            table.map(PassiveFirmwareVersionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column deviceColumn = table.column("DEVICEID").number().notNull().add();
            Column firmwareVersionColumn = table.column("FIRMWAREVERSION").number().notNull().add();
            table.column("LASTCHECKED").number().map("lastChecked").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("ACTIVATEDATE").number().map("activateDate").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.addIntervalColumns("interval");
            table.addAuditColumns().forEach(column -> column.upTo(version(10, 2)));
            table.primaryKey("FWC_PK_PASSIVEFIRMWAREVERSION").on(idColumn).add();
            table
                    .foreignKey("FWC_PASSIVE_FK_DEVICE")
                    .on(deviceColumn)
                    .references(Device.class)
                    .map("device")
                    .onDelete(CASCADE)
                    .add();
            table
                    .foreignKey("FWC_PASSIVE_FK_FIRMWARE")
                    .on(firmwareVersionColumn)
                    .references(FWC_FIRMWAREVERSION.name())
                    .map("firmwareVersion")
                    .onDelete(CASCADE)
                    .add();
        }
    },

    /**
     * @deprecated removed in 10.7; now done via {@link FirmwareCampaignPersistenceSupport}
     */
    @Deprecated
    FWC_CAMPAIGN {
        @Override
        void addTo(DataModel dataModel) {
            Table<String> table = dataModel.addTable(name(), String.class).upTo(version(10, 7));

            Column idColumn = table.addAutoIdColumn();
            Column name = table.column("CAMPAIGN_NAME").varChar(NAME_LENGTH).map("name").notNull().add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map("status").notNull().add();
            Column deviceType = table.column("DEVICE_TYPE").number().notNull().add();
            table.column("MANAGEMENT_OPTION").number().conversion(ColumnConversion.NUMBER2ENUM).map("managementOption").notNull().add();
            table.column("FIRMWARE_TYPE").number().conversion(ColumnConversion.NUMBER2ENUM).map("firmwareType").notNull().add();
            table.column("STARTED_ON").number().map("startedOn").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("FINISHED_ON").number().map("finishedOn").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("COMWINDOWSTART").number().conversion(ColumnConversion.NUMBER2INT).map("comWindow.start.millis").add();
            table.column("COMWINDOWEND").number().conversion(ColumnConversion.NUMBER2INT).map("comWindow.end.millis").add();
            table.column("NROFDEVICES").number().map("numberOfDevices").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("VALIDATION_TIMEOUT_VALUE")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map("validationTimeout" + ".count")
                    .notNull()
                    .since(version(10, 4, 1))
                    .installValue("1")
                    .add();
            table.column("VALIDATION_TIMEOUT_UNIT")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map("validationTimeout" + ".timeUnitCode")
                    .notNull()
                    .since(version(10, 4, 1))
                    .installValue(Integer.toString(TimeDuration.TimeUnit.HOURS.getCode()))
                    .add();
            table.setJournalTableName("FWC_CAMPAIGNJRNL").during(Range.closedOpen(version(10, 2), version(10, 7)));
            table.addAuditColumns();

            table.unique("UQ_FWC_CAMPAIGN_NAME").on(name).add();
            table.foreignKey("FK_FWC_CAMPAIGN_TO_D_TYPE")
                    .on(deviceType)
                    .references(DeviceType.class)
                    .map("deviceType")
                    .add();
            table.primaryKey("PK_FWC_CAMPAIGN").on(idColumn).add();
        }
    },

    /**
     * @deprecated removed in 10.7; now done via {@link FirmwareCampaignItemPersistenceSupport}
     */
    @Deprecated
    FWC_CAMPAIGN_DEVICES {
        @Override
        void addTo(DataModel dataModel) {
            Table<String> table = dataModel.addTable(name(), String.class).upTo(version(10, 7));
            Column campaign = table.column("CAMPAIGN").number().notNull().add();
            Column device = table.column("DEVICE").number().notNull().add();
            Column status = table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map("status").add();
            table.column("MESSAGE_ID").number().conversion(ColumnConversion.NUMBER2LONGNULLZERO).map("firmwareMessageId").add();
            table.column("STARTED_ON").number().conversion(ColumnConversion.NUMBER2INSTANT).map("startedOn").add();
            Column finishedOn = table.column("FINISHED_ON").number().conversion(ColumnConversion.NUMBER2INSTANT).map("finishedOn").add();

            table.unique("UQ_FWC_DEV_IN_CAMP").on(device, status, finishedOn).add();
            table.foreignKey("FK_FWC_DEVICE_TO_CAMPAIGN")
                    .on(campaign)
                    .references(FWC_CAMPAIGN.name())
                    .map("campaign")
                    .onDelete(CASCADE)
                    .add();
            table.foreignKey("FK_FWC_DEVICE_TO_DEVICE")
                    .on(device)
                    .references(Device.class)
                    .map("device")
                    .onDelete(CASCADE)
                    .add();
            table.primaryKey("PK_FWC_CAMPAIGN_DEVICES").on(campaign, device).add();
        }
    },

    /**
     * @deprecated removed in 10.7; now done as {@link DefaultState}
     */
    @Deprecated
    FWC_CAMPAIGN_STATUS {
        @Override
        void addTo(DataModel dataModel) {
            Table<String> table = dataModel.addTable(name(), String.class).upTo(version(10, 7));

            Column campaign = table.column("CAMPAIGN").number().notNull().add();
            table.column("ONGOING").number().map("ongoing").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("SUCCESS").number().map("success").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("PENDING").number().map("pending").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("FAILED").number().map("failed").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("CONFIGURATIONERROR").number().map("configurationError").conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("CANCELLED").number().map("cancelled").conversion(ColumnConversion.NUMBER2LONG).add();

            table.foreignKey("FK_FWC_STATUS_TO_CAMPAIGN")
                    .on(campaign)
                    .references(FWC_CAMPAIGN.name())
                    .map("campaign")
                    .onDelete(CASCADE)
                    .composition()
                    .add();
            table.primaryKey("PK_FWC_CAMPAIGN_STATUS").on(campaign).add();
        }
    },

    FWC_CAMPAIGN_PROPS {
        @Override
        void addTo(DataModel dataModel) {
            Table<FirmwareCampaignProperty> table = dataModel.addTable(name(), FirmwareCampaignProperty.class);
            table.map(FirmwareCampaignPropertyImpl.class);

            Column campaign = table.column("CAMPAIGN")
                    .number()
                    .notNull()
                    .add();
            Column key = table.column("KEY")
                    .varChar(NAME_LENGTH)
                    .map(FirmwareCampaignPropertyImpl.Fields.KEY.fieldName())
                    .notNull()
                    .add();
            Column cps = table.column("CPS_ID")
                    .number()
                    //.notNull() can't make not null here; done manually in UpgraderV10_7 and Installer
                    .since(version(10, 7))
                    .add();
            table.column("VALUE")
                    .varChar(DESCRIPTION_LENGTH)
                    .map(FirmwareCampaignPropertyImpl.Fields.VALUE.fieldName())
                    .notNull()
                    .add();
            table.setJournalTableName("FWC_CAMPAIGN_PROPSJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.foreignKey("FK_FWC_PROPS_TO_CAMPAIGN")
                    .on(campaign, cps)
                    .since(version(10, 7))
                    // previously referenced FWC_CAMPAIGN; old fk is removed manually in UpgraderV10_7
                    .references(FirmwareCampaignDomainExtension.class)
                    .onDelete(CASCADE)
                    .map(FirmwareCampaignPropertyImpl.Fields.CAMPAIGN.fieldName())
                    .add();
            table.primaryKey("PK_FWC_CAMPAIGN_PROPS").on(campaign, key).add();
        }
    },

    FWC_SECACCESSOR_ON_DEVICETYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<SecurityAccessorOnDeviceType> table = dataModel.addTable(name(), SecurityAccessorOnDeviceType.class)
                    .since(version(10, 4, 1))
                    .map(SecurityAccessorOnDeviceTypeImpl.class);
            Column deviceTypeColumn = table.column(SecurityAccessorOnDeviceTypeImpl.Fields.DEVICETYPE.name()).number().notNull().add();
            Column secAccColumn = table.column(SecurityAccessorOnDeviceTypeImpl.Fields.SECACCESSOR.name()).number().notNull().add();
            table.setJournalTableName(Constants.FWC_SECACC_ON_DEVICETYPE_JOURNAL_TABLE).since(version(10, 4, 1));
            table.addAuditColumns();
            table.primaryKey("FWC_PK_SECACCONDEVTYPE").on(deviceTypeColumn).add();
            table.foreignKey("FWC_FK_SECACCONDEVTYPE2DT")
                    .references(DeviceType.class)
                    .on(deviceTypeColumn)
                    .map(SecurityAccessorOnDeviceTypeImpl.Fields.DEVICETYPE.fieldName())
                    .add();
            table.foreignKey("FWC_FK_SECACCONDEVTYPE2SA")
                    .references(SecurityAccessor.class)
                    .on(secAccColumn)
                    .map(SecurityAccessorOnDeviceTypeImpl.Fields.SECACCESSOR.fieldName())
                    .add();
        }
    };

    abstract void addTo(DataModel dataModel);

    interface Constants {
        String FWC_SECACC_ON_DEVICETYPE_JOURNAL_TABLE = "FWC_SECACCESSORONDEVTYPE_JRNL";
    }

}
