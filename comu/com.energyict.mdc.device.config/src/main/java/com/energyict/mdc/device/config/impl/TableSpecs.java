package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfValidationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationEstimationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperty;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.config.impl.deviceconfigchange.ConflictingConnectionMethodSolutionImpl;
import com.energyict.mdc.device.config.impl.deviceconfigchange.ConflictingSecuritySetSolutionImpl;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigConflictMappingImpl;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.security.SecurityPropertySpecProvider;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.tasks.ComTask;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Version.version;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (11:17)
 */
public enum TableSpecs {

    DTC_DEVICETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceType> table = dataModel.addTable(this.name(), DeviceType.class);
            table.map(DeviceTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.column("DEVICEPROTOCOLPLUGGABLEID").number().conversion(ColumnConversion.NUMBER2LONG).map(DeviceTypeImpl.Fields.DEVICE_PROTOCOL_PLUGGABLE_CLASS.fieldName()).add();
            table.column("DEVICEUSAGETYPE").number().conversion(ColumnConversion.NUMBER2INT).map("deviceUsageTypeId").add();
            table.column("DEVICETYPEPURPOSE").number().notNull().conversion(NUMBER2ENUM).map(DeviceTypeImpl.Fields.DEVICETYPEPURPOSE.fieldName()).since(version(10, 2)).installValue("0").add();
            table.column("FILEMNGMTENABLED")
                    .number()
                    .notNull()
                    .conversion(NUMBER2BOOLEAN)
                    .map(DeviceTypeImpl.Fields.FILE_MANAGEMENT_ENABLED.fieldName())
                    .since(version(10, 2))
                    .installValue("0")
                    .add();
            table.addAuditColumns();
            table.unique("UK_DTC_DEVICETYPE").on(name).add();
            table.primaryKey("PK_DTC_DEVICETYPE").on(id).add();
        }
    },

    DTC_KEYACCESSORTYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<KeyAccessorType> table = dataModel.addTable(name(), KeyAccessorType.class);
            table.map(KeyAccessorTypeImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME")
                    .varChar()
                    .notNull()
                    .map(KeyAccessorTypeImpl.Fields.NAME.fieldName())
                    .since(Version.version(10,3))
                    .add();
            Column deviceType = table.column("DEVICETYPEID")
                    .number()
                    .notNull()
                    .since(Version.version(10,3))
                    .add();
            table.column("DESCRIPTION")
                    .varChar()
                    .map(KeyAccessorTypeImpl.Fields.DESCRIPTION.fieldName())
                    .since(Version.version(10,3))
                    .add();
            table.column("DURATION").number()
                    .conversion(NUMBER2INT)
                    .map(KeyAccessorTypeImpl.Fields.DURATION.fieldName()+".count")
                    .since(Version.version(10,3))
                    .add();
            table.column("DURATIONCODE").number()
                    .conversion(NUMBER2INT)
                    .map(KeyAccessorTypeImpl.Fields.DURATION.fieldName()+".timeUnitCode")
                    .since(Version.version(10,3))
                    .add();
            table.foreignKey("FK_DTC_KEYACCESSOR_DEVTYPE")
                    .on(deviceType)
                    .references(DTC_DEVICETYPE.name())
                    .map(KeyAccessorTypeImpl.Fields.DEVICETYPE.fieldName())
                    .reverseMap(DeviceTypeImpl.Fields.KEY_ACCESSOR_TYPE.fieldName())
                    .composition()
                    .add();
            table.primaryKey("PK_DTC_KEYACCESSOR").on(id).add();
        }
    },

    DTC_DEVICETYPE_DLC {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceLifeCycleInDeviceType> table = dataModel.addTable(name(), DeviceLifeCycleInDeviceType.class);
            table.map(DeviceLifeCycleInDeviceTypeImpl.class);
            Column deviceType = table.column("DEVICETYPE").notNull().number().add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addAuditColumns();
            Column deviceLifeCycle = table.column("DEVICELIFECYCLE").notNull().number().add();
            table.primaryKey("PK_DTC_DEVTYPE_DLC").on(deviceType, intervalColumns.get(0)).add();
            table.foreignKey("FK_DTC_DLCINDT_DLC")
                    .on(deviceLifeCycle)
                    .references(DeviceLifeCycle.class)
                    .onDelete(RESTRICT)
                    .map("deviceLifeCycle")
                    .add();
            table.foreignKey("FK_DTC_DLCINDT_DT")
                    .on(deviceType)
                    .references(DTC_DEVICETYPE.name())
                    .onDelete(CASCADE)
                    .map("deviceType")
                    .reverseMap("deviceLifeCycle")
                    .composition()
                    .add();
        }
    },

    DTC_LOADPRFTYPEFORDEVICETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceTypeLoadProfileTypeUsage> table = dataModel.addTable(name(), DeviceTypeLoadProfileTypeUsage.class);
            table.map(DeviceTypeLoadProfileTypeUsage.class);
            Column loadProfileType = table.column("LOADPROFILETYPEID").number().notNull().add();
            Column deviceType = table.column("DEVICETYPEID").number().notNull().add();
            Column customPropertySet = table.column("CUSTOMPROPERTYSETID").number().add();
            table.addAuditColumns();
            table.primaryKey("PK_DTC_LOADPRFTYPEFORDEVTYPE").on(loadProfileType, deviceType).add();
            table.foreignKey("FK_DTC_DEVTYPE_LPT_DEVTYPE")
                    .on(deviceType)
                    .references(DTC_DEVICETYPE.name())
                    .map("deviceType")
                    .reverseMap("loadProfileTypeUsages")
                    .composition()
                    .onDelete(CASCADE)
                    .add();
            table.foreignKey("FK_DTC_LPT_LPT_DEVTYPE")
                    .on(loadProfileType)
                    .references(LoadProfileType.class)
                    .map("loadProfileType")
                    .add();
            table.foreignKey("FK_DTC_LPT_CPS_CPS")
                    .references(RegisteredCustomPropertySet.class)
                    .on(customPropertySet)
                    .map("customPropertySet")
                    .add();
        }
    },

    DTC_REGTYPEFORDEVICETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceTypeRegisterTypeUsage> table = dataModel.addTable(name(), DeviceTypeRegisterTypeUsage.class);
            table.map(DeviceTypeRegisterTypeUsage.class);
            Column registerType = table.column("REGISTERTYPEID").number().notNull().add();
            Column deviceType = table.column("DEVICETYPEID").number().notNull().add();
            Column customPropertySet = table.column("CUSTOMPROPERTYSETID").number().add();
            table.addAuditColumns();
            table.primaryKey("PK_DTC_REGTYPEFORDEVICETYPE").on(registerType, deviceType).add();
            table.foreignKey("FK_DTC_DEVTYPE_REGTYPE_DEVTYPE")
                    .on(deviceType)
                    .references(DTC_DEVICETYPE.name())
                    .map("deviceType")
                    .reverseMap("registerTypeUsages")
                    .composition()
                    .onDelete(CASCADE)
                    .add();
            table.foreignKey("FK_DTC_MAPID_REGTYPE_DEVTYPE")
                    .on(registerType)
                    .references(MeasurementType.class)
                    .map("registerType")
                    .add();
            table.foreignKey("FK_DTC_REGTYPE_CPS_CPS")
                    .references(RegisteredCustomPropertySet.class)
                    .on(customPropertySet)
                    .map("registeredCustomPropertySet")
                    .add();
        }
    },

    DTC_LOGBOOKTYPEFORDEVICETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceTypeLogBookTypeUsage> table = dataModel.addTable(name(), DeviceTypeLogBookTypeUsage.class);
            table.map(DeviceTypeLogBookTypeUsage.class);
            Column logBookType = table.column("LOGBOOKTYPEID").number().notNull().add();
            Column deviceType = table.column("DEVICETYPEID").number().notNull().add();
            table.addAuditColumns();
            table.primaryKey("PK_DTC_LOGBOOKTYPEFORDEVTYPE").on(logBookType, deviceType).add();
            table.foreignKey("FK_DTC_DEVTYPE_LOGBT_DEVTYPE")
                    .on(deviceType)
                    .references(DTC_DEVICETYPE.name())
                    .map("deviceType")
                    .reverseMap("logBookTypeUsages")
                    .composition()
                    .onDelete(CASCADE)
                    .add();
            table.foreignKey("FK_DTC_LBTYPE_LBTT_DEVTYPE")
                    .on(logBookType)
                    .references(LogBookType.class)
                    .map("logBookType")
                    .add();
        }
    },

    DTC_DEVICECONFIG {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceConfiguration> table = dataModel.addTable(name(), DeviceConfiguration.class);
            table.map(DeviceConfigurationImpl.class);
            Column id = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            Column deviceType = table.column("DEVICETYPEID").number().notNull().add();
            table.column("ACTIVE").number().conversion(ColumnConversion.NUMBER2BOOLEAN).map("active").add();
            table.column("COMMUNICATIONFUNCTIONMASK").number().conversion(ColumnConversion.NUMBER2INT).map("communicationFunctionMask").add();
            table.column("SUPPORTALLCATEGORIES").number().conversion(NUMBER2BOOLEAN).notNull().map("supportsAllProtocolMessages").add();
            table.column("USERACTIONS").number().conversion(NUMBER2LONG).notNull().map("supportsAllProtocolMessagesUserActionsBitVector").add();
            table.column("GATEWAY_TYPE").number().conversion(ColumnConversion.NUMBER2ENUM).map(DeviceConfigurationImpl.Fields.GATEWAY_TYPE.fieldName()).notNull().add();
            table.column("DATALOGGERENABLED").number().conversion(ColumnConversion.NUMBER2BOOLEAN).map(DeviceConfigurationImpl.Fields.DATALOGGER_ENABLED.fieldName()).since(version(10, 2)).add();
            table.setJournalTableName("DTC_DEVICECONFIGJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.primaryKey("PK_DTC_DEVICECONFIG").on(id).add();
            table.foreignKey("FK_DTC_DEVCONFIG_DEVTYPE")
                    .on(deviceType)
                    .references(DTC_DEVICETYPE.name())
                    .map("deviceType")
                    .reverseMap("deviceConfigurations")
                    .composition()
                    .add();
            table.unique("UQ_DTC_DEVICECONFIG_NAME").on(deviceType, nameColumn).add();
        }
    },

    DTC_LOADPROFILESPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileSpec> table = dataModel.addTable(name(), LoadProfileSpec.class);
            table.map(LoadProfileSpecImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceConfiguration = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column loadProfileType = table.column("LOADPROFILETYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("OBISCODE").varChar(80).map("overruledObisCodeString").add(); // obiscode is not a free field: derived from real obiscode, so user can not exceed max length
            table.setJournalTableName("DTC_LOADPROFILESPECJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.primaryKey("PK_DTC_LOADPROFILESPECID").on(id).add();
            table.foreignKey("FK_DTC_LPRFSPEC_LOADPROFTYPE")
                    .on(loadProfileType)
                    .references(LoadProfileType.class)
                    .map("loadProfileType")
                    .add();
            table.foreignKey("FK_DTC_LPRFSPEC_DEVCONFIG")
                    .on(deviceConfiguration)
                    .references(DTC_DEVICECONFIG.name())
                    .map("deviceConfiguration")
                    .reverseMap("loadProfileSpecs")
                    .composition()
                    .add();
        }
    },

    DTC_CHANNELSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ChannelSpec> table = dataModel.addTable(name(), ChannelSpec.class);
            table.map(ChannelSpecImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceConfiguration = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column channelType = table.column("CHANNELTYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("OBISCODE").varChar(Table.NAME_LENGTH).map(ChannelSpecImpl.ChannelSpecFields.OVERRULED_OBISCODE.fieldName()).add();
            table.column("FRACTIONDIGITS").number().conversion(ColumnConversion.NUMBER2INT).map(ChannelSpecImpl.ChannelSpecFields.NUMBER_OF_FRACTION_DIGITS.fieldName()).add();
            table.column("OVERFLOWVALUE").number().map(ChannelSpecImpl.ChannelSpecFields.OVERFLOW_VALUE.fieldName()).add();
            Column loadProfileSpec = table.column("LOADPROFILESPECID").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("INTERVAL").number().notNull().conversion(ColumnConversion.NUMBER2INT).map(ChannelSpecImpl.ChannelSpecFields.INTERVAL_COUNT.fieldName()).add();
            table.column("INTERVALCODE").number().notNull().conversion(ColumnConversion.NUMBER2INT).map(ChannelSpecImpl.ChannelSpecFields.INTERVAL_CODE.fieldName()).add();
            table.column("USEMULTIPLIER").number().conversion(NUMBER2BOOLEAN).map(ChannelSpecImpl.ChannelSpecFields.USEMULTIPLIER.fieldName()).add();
            Column calculatedReadingType = table.column("CALCULATEDREADINGTYPE").varChar(Table.NAME_LENGTH).add();
            table.setJournalTableName("DTC_CHANNELSPECJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.primaryKey("PK_DTC_CHANNELSPEC").on(id).add();
            table.foreignKey("FK_DTC_CHANNELSPEC_DEVCONFIG").
                    on(deviceConfiguration).
                    references(DTC_DEVICECONFIG.name()).
                    map(ChannelSpecImpl.ChannelSpecFields.DEVICE_CONFIG.fieldName()).
                    add();
            table.foreignKey("FK_DTC_CHANNELSPEC_REGMAP").
                    on(channelType).
                    references(MeasurementType.class).
                    map(ChannelSpecImpl.ChannelSpecFields.CHANNEL_TYPE.fieldName()).
                    add();
            table.foreignKey("FK_DTC_CHANNELSPEC_CALC")
                    .on(calculatedReadingType)
                    .references(ReadingType.class)
                    .map(ChannelSpecImpl.ChannelSpecFields.CALCULATED_READINGTYPE.fieldName())
                    .add();
            table.foreignKey("FK_DTC_CHANNELSPEC_LPRFSPEC").
                    on(loadProfileSpec).
                    references(DTC_LOADPROFILESPEC.name()).
                    map(ChannelSpecImpl.ChannelSpecFields.LOADPROFILE_SPEC.fieldName()).
                    reverseMap("channelSpecs").
                    composition().
                    add();
        }
    },

    DTC_REGISTERSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterSpec> table = dataModel.addTable(name(), RegisterSpec.class);
            table.map(RegisterSpecImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            Column registerType = table.column("REGISTERTYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("DEVICEOBISCODE").varChar().map("overruledObisCodeString").add();
            table.column("NUMBEROFFRACTIONDIGITS").number().conversion(ColumnConversion.NUMBER2INT).map(RegisterSpecFields.NUMBER_OF_FRACTION_DIGITS.fieldName()).add();
            table.column("OVERFLOWVALUE").number().map(RegisterSpecFields.OVERFLOW_VALUE.fieldName()).add();
            Column deviceConfiguration = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("USEMULTIPLIER").number().conversion(NUMBER2BOOLEAN).map(RegisterSpecFields.USEMULTIPLIER.fieldName()).add();
            Column calculatedReadingType = table.column("CALCULATEDREADINGTYPE").varChar(Table.NAME_LENGTH).add();
            table.setJournalTableName("DTC_REGISTERSPECJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.primaryKey("PK_DTC_REGISTERSPEC").on(id).add();
            table.foreignKey("FK_DTC_REGISTERSPEC_REGMAP")
                    .on(registerType)
                    .references(MeasurementType.class)
                    .map(RegisterSpecFields.REGISTER_TYPE.fieldName())
                    .add();
            table.foreignKey("FK_DTC_REGISTERSPEC_DEVCONFIG")
                    .on(deviceConfiguration)
                    .references(DTC_DEVICECONFIG.name())
                    .map("deviceConfig")
                    .reverseMap("registerSpecs")
                    .composition()
                    .add();
            table.foreignKey("FK_DTC_REGISTERSPEC_CALC")
                    .on(calculatedReadingType)
                    .references(ReadingType.class)
                    .map(RegisterSpecFields.CALCULATED_READINGTYPE.fieldName()).add();
            table.unique("U_DTC_REGISTERTYPE_IN_CONFIG")
                    .on(deviceConfiguration, registerType)
                    .add();
        }
    },

    DTC_LOGBOOKSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBookSpec> table = dataModel.addTable(name(), LogBookSpec.class);
            table.map(LogBookSpecImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceconfigid = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column logbooktypeid = table.column("LOGBOOKTYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("OBISCODE").varChar(80).map("overruledObisCodeString").add();
            table.setJournalTableName("DTC_LOGBOOKSPECJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.primaryKey("PK_DTC_LOGBOOKSPEC").on(id).add();
            table.foreignKey("FK_DTC_LOGBOOKSPEC_DEVCONFIG")
                    .on(deviceconfigid)
                    .references(DTC_DEVICECONFIG.name())
                    .map("deviceConfiguration")
                    .reverseMap("logBookSpecs")
                    .composition()
                    .add();
            table.foreignKey("FK_DTC_LOGBOOKSPEC_LOGBOOKTYPE")
                    .on(logbooktypeid)
                    .references(LogBookType.class)
                    .map("logBookType")
                    .add();
        }
    },

    DTC_DIALECTCONFIGPROPERTIES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProtocolDialectConfigurationProperties> table = dataModel.addTable(name(), ProtocolDialectConfigurationProperties.class);
            table.map(ProtocolDialectConfigurationPropertiesImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceConfiguration = table.column("DEVICECONFIGURATION").number().notNull().add();
            table.column("DEVICEPROTOCOLDIALECT").varChar().notNull().map("protocolDialectName").add();
            Column nameColumn = table.column("NAME").varChar().notNull().map("name").add();
            table.setJournalTableName("DTC_DIALECT_CONFIG_PROPSJRNL").since(version(10, 2));
            table.addAuditColumns();
            table
                .foreignKey("FK_DTC_DIALECTCONFPROPS_CONFIG")
                .on(deviceConfiguration)
                .references(DTC_DEVICECONFIG.name())
                .map("deviceConfiguration")
                .reverseMap("configurationPropertiesList")
                .composition()
                .add();
            table.primaryKey("PK_DTC_DIALECTCONFIGPROPS").on(id).add();
            table.unique("UQ_DTC_CONFIGPROPS_NAME").on(deviceConfiguration, nameColumn).add();
        }
    },

    DTC_DIALECTCONFIGPROPSATTR {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProtocolDialectConfigurationProperty> table = dataModel.addTable(name(), ProtocolDialectConfigurationProperty.class);
            table.map(ProtocolDialectConfigurationPropertyImpl.class);
            Column id = table.column("ID").number().notNull().add();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.setJournalTableName("DTC_DIALCTCONFPROPSATTRJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.column("VALUE").varChar(4000).notNull().map("value").add();
            table
                .foreignKey("DTC_DIALECTCONFIGPROPSATTRJRNL")
                .on(id)
                .references(DTC_DIALECTCONFIGPROPERTIES.name())
                .map("properties")
                .composition()
                .reverseMap("propertyList")
                .add();
            table.primaryKey("PK_DTC_DIALCTCFGPROPSATTR").on(id, name).add();
        }
    },

    DTC_PROTOCOLCONFIGPROPSATTR {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceProtocolConfigurationProperty> table = dataModel.addTable(name(), DeviceProtocolConfigurationProperty.class);
            table.map(DeviceProtocolConfigurationProperty.class);
            Column deviceConfiguration = table.column("DEVICECONFIGURATION").number().notNull().add();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("VALUE").varChar().notNull().map("value").add();
            table.setJournalTableName("DTC_PRTCLCONFIGPROPSATTRJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.foreignKey("FK_DTC_PROTCONFPROPSATTR_CONF")
                    .on(deviceConfiguration)
                    .references(DTC_DEVICECONFIG.name())
                    .map("deviceConfiguration")
                    .composition()
                    .reverseMap("protocolProperties")
                    .add();
            table.primaryKey("PK_DTC_PRTCLCONFPROPSATTR").on(deviceConfiguration, name).add();
        }
    },

    DTC_PARTIALCONNECTIONTASK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<PartialConnectionTask> table = dataModel.addTable(name(), PartialConnectionTask.class);
            table.map(PartialConnectionTaskImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar().notNull().map("name").add();
            table.addDiscriminatorColumn("DISCRIMINATOR", "number");
            Column deviceConfiguration = table.column("DEVICECONFIG").number().add();
            Column connectionType = table.column("CONNECTIONTYPE").number().conversion(NUMBER2LONG).map("pluggableClassId").add();
            Column initiator = table.column("INITIATOR").number().add();
            table.column("COMWINDOWSTART").number().conversion(NUMBER2INT).map("comWindowStart").add();
            table.column("COMWINDOWEND").number().conversion(NUMBER2INT).map("comWindowEnd").add();
            table.column("CONNECTIONSTRATEGY").number().conversion(NUMBER2ENUM).map("connectionStrategy").add();
            table.column("SIMULTANEOUSCONNECTIONS").number().conversion(NUMBER2INT).map("numberOfSimultaneousConnections").add();
            table.column("ISDEFAULT").number().conversion(NUMBER2BOOLEAN).map("isDefault").add();
            Column nextexecutionspecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("RESCHEDULERETRYDELAY").number().conversion(NUMBER2INT).map("rescheduleRetryDelay.count").add();
            Column comportpool = table.column("COMPORTPOOL").number().add();
            table.column("RESCHEDULERETRYDELAYCODE").number().conversion(NUMBER2INT).map("rescheduleRetryDelay.timeUnitCode").add();
            table.setJournalTableName("DTC_PARTIALCONNECTIONTASKJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.primaryKey("PK_DTC_PARTIALCONNTASK").on(id).add();
            table.foreignKey("FK_DTC_PARTIALCT_PLUGGABLE")
                    .on(connectionType)
                    .references(PluggableClass.class)
                    .map("pluggableClass")
                    .add();
            table.foreignKey("FK_DTC_PARTIALCT_COMPORTPOOL")
                    .on(comportpool)
                    .references(ComPortPool.class)
                    .map("comPortPool")
                    .add();
            table.foreignKey("FK_DTC_PARTIALCT_DEVCONFIG")
                    .on(deviceConfiguration)
                    .references(DTC_DEVICECONFIG.name())
                    .map("configuration")
                    .reverseMap("partialConnectionTasks")
                    .composition()
                    .add();
            table.foreignKey("FK_DTC_PARTIALCT_NEXTEXECSPEC")
                    .on(nextexecutionspecs)
                    .references(NextExecutionSpecs.class)
                    .map("nextExecutionSpecs")
                    .add();
            table.foreignKey("FK_DTC_PARTIALCT_INITIATOR")
                    .on(initiator)
                    .references(DTC_PARTIALCONNECTIONTASK.name())
                    .map("initiator")
                    .add();
            table.unique("UQ_DTC_PARTIALCT_NAME").on(deviceConfiguration, nameColumn).add();
        }
    },

    DTC_PARTIALCONNECTIONTASKPROPS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<PartialConnectionTaskPropertyImpl> table = dataModel.addTable(name(), PartialConnectionTaskPropertyImpl.class);
            table.map(PartialConnectionTaskPropertyImpl.class);
            Column partialconnectiontask = table.column("PARTIALCONNECTIONTASK").number().notNull().add();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("VALUE").varChar().notNull().map("value").add();
            table.setJournalTableName("DTC_PARTIAL_CONNTASK_PROPSJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.foreignKey("FK_DTC_PARTIALCTPROPS_TASK")
                    .on(partialconnectiontask)
                    .references(DTC_PARTIALCONNECTIONTASK.name())
                    .map("partialConnectionTask")
                    .reverseMap("properties")
                    .composition()
                    .add();
            table.primaryKey("PK_DTC_PARTIALCONTSKPROPS").on(partialconnectiontask, name).add();
        }
    },

    DTC_MESSAGEENABLEMENT {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceMessageEnablement> table = dataModel.addTable(name(), DeviceMessageEnablement.class);
            table.map(DeviceMessageEnablementImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceConfig = table.column("DEVICECONFIG").conversion(NUMBER2LONG).number().notNull().add();
            table.column("DEVICEMESSAGEID").number().conversion(NUMBER2LONG).map("deviceMessageIdDbValue").notNull().add();
            table.setJournalTableName("DTC_MESSAGEENABLEMENTJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.foreignKey("FK_DTC_DME_DEVCONFIG")
                    .on(deviceConfig)
                    .references(DTC_DEVICECONFIG.name())
                    .map("deviceConfiguration")
                    .reverseMap(DeviceConfigurationImpl.Fields.DEVICE_MESSAGE_ENABLEMENTS.fieldName())
                    .composition()
                    .add();
            table.primaryKey("PK_DTC_DEVMESENABLEMENT").on(id).add();
        }
    },

    DTC_MSGABLEMENTUSERACTION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceMessageEnablementImpl.DeviceMessageUserActionRecord> table = dataModel.addTable(name(), DeviceMessageEnablementImpl.DeviceMessageUserActionRecord.class);
            table.map(DeviceMessageEnablementImpl.DeviceMessageUserActionRecord.class);
            Column useraction = table.column("USERACTION").number().conversion(NUMBER2ENUM).notNull().map("userAction").add();
            Column deviceMessageEnablement = table.column("DEVICEMESSAGEENABLEMENT").number().notNull().add();
            table.setJournalTableName("DTC_MSGENBLMNTUSRACTIONJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.foreignKey("FK_DTC_MESENUSRACTION")
                    .on(deviceMessageEnablement)
                    .references(DTC_MESSAGEENABLEMENT.name())
                    .reverseMap("deviceMessageUserActionRecords")
                    .composition()
                    .map("deviceMessageEnablement")
                    .add();
            table.primaryKey("PK_DTC_MSGENABLEUSRACTION").on(useraction, deviceMessageEnablement).add();
        }
    },

    DTC_SECURITYPROPERTYSET {
        @Override
        public void addTo(DataModel dataModel) {
            Table<SecurityPropertySet> table =
                    dataModel
                            .addTable(name(), SecurityPropertySet.class)
                            .alsoReferredToAs(SecurityPropertySpecProvider.class);
            table.map(SecurityPropertySetImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            Column deviceConfiguration = table.column("DEVICECONFIG").conversion(NUMBER2LONG).number().notNull().add();
            table.column("AUTHENTICATIONLEVEL").number().conversion(NUMBER2INT).notNull().map("authenticationLevelId").add();
            table.column("ENCRYPTIONLEVEL").number().conversion(NUMBER2INT).notNull().map("encryptionLevelId").add();
            table.setJournalTableName("DTC_SECURITYPROPERTYSETJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.foreignKey("FK_DTC_SECPROPSET_DEVCONFIG")
                    .on(deviceConfiguration)
                    .references(DTC_DEVICECONFIG.name())
                    .map("deviceConfiguration")
                    .reverseMap(DeviceConfigurationImpl.Fields.SECURITY_PROPERTY_SETS.fieldName())
                    .composition()
                    .add();
            table.primaryKey("PK_DTC_SECURITYPROPSET").on(id).add();
            table.unique("UK_DTC_SECPROPSET_NAME").on(deviceConfiguration, name).add();
        }
    },

    DTC_SECURITYPROPSETUSERACTION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<SecurityPropertySetImpl.UserActionRecord> table = dataModel.addTable(name(), SecurityPropertySetImpl.UserActionRecord.class);
            table.map(SecurityPropertySetImpl.UserActionRecord.class);
            Column useraction = table.column("USERACTION").number().conversion(NUMBER2ENUM).notNull().map("userAction").add();
            Column securitypropertyset = table.column("SECURITYPROPERTYSET").number().notNull().add();
            table.setJournalTableName("DTC_SEC_PROP_SET_USRACTIONJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.foreignKey("FK_DTC_SECPROPSETUSRACT_SPS")
                    .on(securitypropertyset)
                    .references(DTC_SECURITYPROPERTYSET.name())
                    .reverseMap("userActionRecords")
                    .composition()
                    .map("set")
                    .add();
            table.primaryKey("PK_DTC_SECPROPSETUSRACTN").on(useraction, securitypropertyset).add();
        }
    },

    DTC_COMTASKENABLEMENT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComTaskEnablement> table = dataModel.addTable(name(), ComTaskEnablement.class);
            table.map(ComTaskEnablementImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column comtask = table.column("COMTASK").number().notNull().add();
            Column deviceCommunicationConfigation = table.column("DEVICECOMCONFIG").number().notNull().add();
            Column securityPropertySet = table.column("SECURITYPROPERTYSET").number().notNull().add();
            table.column("SUSPENDED").number().notNull().conversion(NUMBER2BOOLEAN).map(ComTaskEnablementImpl.Fields.SUSPENDED.fieldName()).add();
            Column partialConnectionTask = table.column("PARTIALCONNECTIONTASK").number().add();
            table.column("USEDEFAULTCONNECTIONTASK").number().notNull().conversion(NUMBER2BOOLEAN).map(ComTaskEnablementImpl.Fields.USE_DEFAULT_CONNECTION_TASK.fieldName()).add();
            table.column("PRIORITY").number().notNull().conversion(NUMBER2INT).map(ComTaskEnablementImpl.Fields.PRIORITY.fieldName()).add();
            Column dialectConfigurationProperties = table.column("DIALECTCONFIGPROPERTIES").number().notNull().add();
            table.column("IGNORENEXTEXECSPECS").number().notNull().conversion(NUMBER2BOOLEAN).map(ComTaskEnablementImpl.Fields.IGNORE_NEXT_EXECUTION_SPECS_FOR_INBOUND.fieldName()).add();
            table.foreignKey("FK_DTC_COMTASKENABLMNT_OPARTCT")
                    .on(partialConnectionTask)
                    .references(DTC_PARTIALCONNECTIONTASK.name())
                    .map(ComTaskEnablementImpl.Fields.PARTIAL_CONNECTION_TASK.fieldName())
                    .add();
            table.foreignKey("FK_DTC_COMTASKENABLMNT_SECURPS")
                    .on(securityPropertySet)
                    .references(DTC_SECURITYPROPERTYSET.name())
                    .map(ComTaskEnablementImpl.Fields.SECURITY_PROPERTY_SET.fieldName())
                    .add();
            table.foreignKey("FK_DTC_COMTASKENABLMNT_COMTASK")
                    .on(comtask)
                    .references(ComTask.class)
                    .map(ComTaskEnablementImpl.Fields.COM_TASK.fieldName())
                    .add();
            table.foreignKey("FK_DTC_COMTASKENBLMNT_DCOMCONF")
                    .on(deviceCommunicationConfigation)
                    .references(DTC_DEVICECONFIG.name())
                    .map(ComTaskEnablementImpl.Fields.CONFIGURATION.fieldName())
                    .reverseMap(DeviceConfigurationImpl.Fields.COM_TASK_ENABLEMENTS.fieldName())
                    .composition()
                    .onDelete(CASCADE)
                    .add();
            table.foreignKey("FK_DTC_COMTASKENABLMNT_PDCP")
                    .on(dialectConfigurationProperties)
                    .references(DTC_DIALECTCONFIGPROPERTIES.name())
                    .map(ComTaskEnablementImpl.Fields.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES.fieldName())
                    .onDelete(CASCADE)
                    .add();
            table.unique("UK_DTC_COMTASKENABLEMENT").on(comtask, deviceCommunicationConfigation).add();
            table.primaryKey("PK_DTC_COMTASKENABLEMENT").on(id).add();
        }
    },

    //deviceConfValidationRuleSetUsages
    DTC_DEVCFGVALRULESETUSAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceConfValidationRuleSetUsage> table = dataModel.addTable(name(), DeviceConfValidationRuleSetUsage.class);
            table.map(DeviceConfValidationRuleSetUsageImpl.class);
            Column validationRuleSetIdColumn =
                    table.column("VALIDATIONRULESETID")
                            .number()
                            .notNull()
                            .conversion(NUMBER2LONG)
                            .map("validationRuleSetId")
                            .add();
            Column deviceConfigurationIdColumn =
                    table.column("DEVICECONFIGID")
                            .number()
                            .notNull()
                            .conversion(NUMBER2LONG)
                            .map("deviceConfigurationId")
                            .add();
            table.setJournalTableName("DTC_DEVCFGVALRULESETUSAGEJRNL");
            table.addAuditColumns();

            table.primaryKey("DTC_PK_SETCONFIGUSAGE").on(validationRuleSetIdColumn, deviceConfigurationIdColumn).add();
            table.foreignKey("DTC_FK_RULESET").references(ValidationRuleSet.class).onDelete(RESTRICT).map("validationRuleSet").on(validationRuleSetIdColumn).add();
            table.foreignKey("DTC_FK_DEVICECONFIG").references("DTC_DEVICECONFIG").reverseMap("deviceConfValidationRuleSetUsages").composition().map("deviceConfiguration").on(deviceConfigurationIdColumn).add();
        }
    },

    //deviceConfEstimationRuleSetUsages
    DTC_DEVCFGESTRULESETUSAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceConfigurationEstimationRuleSetUsage> table = dataModel.addTable(name(), DeviceConfigurationEstimationRuleSetUsage.class);
            table.map(DeviceConfigurationEstimationRuleSetUsageImpl.class);
            Column estimationRuleSetColumn = table.column("ESTIMATIONRULESET")
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            Column deviceConfigurationColumn = table.column("DEVICECONFIG")
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            table.column("POSITION")
                    .number()
                    .notNull()
                    .conversion(NUMBER2INT)
                    .map(DeviceConfigurationEstimationRuleSetUsageImpl.Fields.POSITION.fieldName())
                    .add();
            table.setJournalTableName(name() + "JRNL");
            table.addAuditColumns();

            table.primaryKey("DTC_PK_ESTRULESETUSAGE").on(estimationRuleSetColumn, deviceConfigurationColumn).add();

            table.foreignKey("DTC_FK_ESTIMATIONRULESET").
                    references(EstimationRuleSet.class).
                    onDelete(RESTRICT).
                    map(DeviceConfigurationEstimationRuleSetUsageImpl.Fields.ESTIMATIONRULESET.fieldName()).
                    on(estimationRuleSetColumn).
                    add();

            table.foreignKey("DTC_FK_ESTRSUSAGE_DEVICECONF").
                    references(DTC_DEVICECONFIG.name()).
                    reverseMap(DeviceConfigurationImpl.Fields.DEVICECONF_ESTIMATIONRULESET_USAGES.fieldName()).
                    composition().
                    reverseMapOrder(DeviceConfigurationEstimationRuleSetUsageImpl.Fields.POSITION.fieldName()).
                    map(DeviceConfigurationEstimationRuleSetUsageImpl.Fields.DEVICECONFIGURATION.fieldName()).
                    on(deviceConfigurationColumn).
                    add();
        }
    },

    DTC_CONFIGCONFLICTMAPPING {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceConfigConflictMapping> table = dataModel.addTable(name(), DeviceConfigConflictMapping.class);
            table.map(DeviceConfigConflictMappingImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceType = table.column("DEVICETYPE").number().notNull().add();
            Column originDeviceConfig = table.column("ORIGINDEVCONFIG").notNull().number().conversion(ColumnConversion.NUMBER2LONG).add();
            Column destinationDeviceConfig = table.column("DESTINATIONDEVCONFIG").notNull().number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("SOLVED").type("char(1)").notNull().map(DeviceConfigConflictMappingImpl.Fields.SOLVED.fieldName()).conversion(ColumnConversion.CHAR2BOOLEAN).add();
            table.addAuditColumns();
            table.primaryKey("PK_DTC_CONFLMAP").on(id).add();
            table.foreignKey("FK_DTC_CONFLMAPDEVTYPE")
                    .references(DTC_DEVICETYPE.name())
                    .on(deviceType)
                    .onDelete(CASCADE)
                    .map(DeviceConfigConflictMappingImpl.Fields.DEVICETYPE.fieldName())
                    .reverseMap(DeviceTypeImpl.Fields.CONFLICTINGMAPPING.fieldName())
                    .composition().add();
            table.foreignKey("FK_DTC_CONFLICTMAPORIGIN")
                    .references(DTC_DEVICECONFIG.name())
                    .on(originDeviceConfig)
                    .onDelete(CASCADE)
                    .map(DeviceConfigConflictMappingImpl.Fields.ORIGINDEVICECONFIG.fieldName()).add();
            table.foreignKey("FK_DTC_CONFLICTMAPDEST")
                    .references(DTC_DEVICECONFIG.name())
                    .on(destinationDeviceConfig)
                    .onDelete(CASCADE)
                    .map(DeviceConfigConflictMappingImpl.Fields.DESTINATIONDEVICECONFIG.fieldName()).add();
            table.unique("UK_DTC_CONFLMAP").on(originDeviceConfig, destinationDeviceConfig).add();
        }
    },

    DTC_CONFLICTCONMETHSOLUTION {
        @Override
        void addTo(DataModel dataModel) {
            Table<ConflictingConnectionMethodSolution> table = dataModel.addTable(name(), ConflictingConnectionMethodSolution.class);
            table.map(ConflictingConnectionMethodSolutionImpl.class);
            Column id = table.addAutoIdColumn();
            Column conflictmapping = table.column("CONFLICTMAPPING").number().notNull().add();
            table.column("ACTION").number().conversion(ColumnConversion.NUMBER2ENUM).map(ConflictingConnectionMethodSolutionImpl.Fields.ACTION.fieldName()).add();
            Column originconnectionmethod = table.column("ORIGINCONNECTIONMETHOD").number().conversion(NUMBER2LONG).add();
            Column destinationconnectionmethod = table.column("DESTCONNECTIONMETHOD").number().conversion(NUMBER2LONG).add();
            table.primaryKey("PK_DTC_CMSOLUTION").on(id).add();
            table.foreignKey("FK_DTC_CM_SOLUTION")
                    .references(DTC_CONFIGCONFLICTMAPPING.name())
                    .on(conflictmapping)
                    .composition()
                    .map(ConflictingConnectionMethodSolutionImpl.Fields.CONFLICTINGMAPPING.fieldName())
                    .reverseMap(DeviceConfigConflictMappingImpl.Fields.CONNECTIONMETHODSOLUTIONS.fieldName())
                    .onDelete(CASCADE)
                    .add();
            table.foreignKey("FK_DTC_CONFORIGINCONMETH")
                    .references(DTC_PARTIALCONNECTIONTASK.name())
                    .on(originconnectionmethod)
                    .map(ConflictingConnectionMethodSolutionImpl.Fields.ORIGINCONNECTIONMETHOD.fieldName()).add();
            table.foreignKey("FK_DTC_CONFDESTCONMETH")
                    .references(DTC_PARTIALCONNECTIONTASK.name())
                    .on(destinationconnectionmethod)
                    .map(ConflictingConnectionMethodSolutionImpl.Fields.DESTINATIONCONNECTIONMETHOD.fieldName()).add();
        }
    },

    DTC_CONFLICTSECSETSOLUTION {
        @Override
        void addTo(DataModel dataModel) {
            Table<ConflictingSecuritySetSolution> table = dataModel.addTable(name(), ConflictingSecuritySetSolution.class);
            table.map(ConflictingSecuritySetSolutionImpl.class);
            Column id = table.addAutoIdColumn();
            Column conflictmapping = table.column("CONFLICTMAPPING").number().notNull().add();
            table.column("ACTION").number().conversion(ColumnConversion.NUMBER2ENUM).map(ConflictingSecuritySetSolutionImpl.Fields.ACTION.fieldName()).add();
            Column originSecuritySet = table.column("ORIGINSECURITYSET").number().conversion(NUMBER2LONG).add();
            Column destinationSecuritySet = table.column("DESTSECURITYSET").number().conversion(NUMBER2LONG).add();
            table.primaryKey("PK_DTC_SSSOLUTION").on(id).add();
            table.foreignKey("FK_DTC_SS_SOLUTION")
                    .references(DTC_CONFIGCONFLICTMAPPING.name())
                    .on(conflictmapping)
                    .composition()
                    .map(ConflictingSecuritySetSolutionImpl.Fields.CONFLICTINGMAPPING.fieldName())
                    .reverseMap(DeviceConfigConflictMappingImpl.Fields.SECURITYSETSOLUTIONS.fieldName())
                    .onDelete(CASCADE)
                    .add();
            table.foreignKey("FK_DTC_CONFORIGINSECSET")
                    .references(DTC_SECURITYPROPERTYSET.name())
                    .on(originSecuritySet)
                    .map(ConflictingSecuritySetSolutionImpl.Fields.ORIGINSECURITYSET.fieldName()).add();
            table.foreignKey("FK_DTC_CONFDESTSECSET")
                    .references(DTC_SECURITYPROPERTYSET.name())
                    .on(destinationSecuritySet)
                    .map(ConflictingSecuritySetSolutionImpl.Fields.DESTINATIONSECURITYSET.fieldName()).add();
        }
    },
    DTC_DEVICETYPECPSUSAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceTypeCustomPropertySetUsage> table = dataModel.addTable(name(), DeviceTypeCustomPropertySetUsage.class);
            table.map(DeviceTypeCustomPropertySetUsageImpl.class);
            Column deviceType = table.column("DEVICETYPE").number().notNull().add();
            Column customPropertySet = table.column("CUSTOMPROPERTYSET").number().notNull().add();
            table.primaryKey("PK_DTC_CPSUSAGE").on(deviceType, customPropertySet).add();
            table.foreignKey("FK_DTC_CPSDEVICETYPE")
                    .references(DTC_DEVICETYPE.name())
                    .on(deviceType)
                    .onDelete(CASCADE)
                    .map(DeviceTypeCustomPropertySetUsageImpl.Fields.DEVICETYPE.fieldName())
                    .reverseMap(DeviceTypeImpl.Fields.CUSTOMPROPERTYSETUSAGE.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_DTC_CPS")
                    .references(RegisteredCustomPropertySet.class)
                    .on(customPropertySet)
                    .onDelete(CASCADE)
                    .map(DeviceTypeCustomPropertySetUsageImpl.Fields.CUSTOMPROPERTYSET.fieldName())
                    .add();
        }
    },
    DTC_DEVICEMESSAGEFILE {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceMessageFile> table = dataModel.addTable(name(), DeviceMessageFile.class).since(version(10, 2));
            table.map(DeviceMessageFileImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table
                            .column("NAME")
                            .varChar()
                            .notNull()
                            .map(DeviceMessageFileImpl.Fields.NAME.fieldName())
                            .add();
            Column deviceType = table
                            .column("DEVICETYPE")
                            .number()
                            .notNull()
                            .add();
            table
                .column("CONTENTS")
                .blob()
                .map(DeviceMessageFileImpl.Fields.CONTENTS.fieldName())
                .add();
            Column obsolete = table.column("OBSOLETE_DATE").number().conversion(ColumnConversion.NUMBER2INSTANT).map(DeviceMessageFileImpl.Fields.COBSOLETEDATE.fieldName()).add();
            table.addAuditColumns();
            table.primaryKey("PK_DTC_DEVICEMESSAGEFILE").on(id).add();
            table
                .foreignKey("FK_DTC_DEVMSGFILE_DEVTYPE")
                .on(deviceType)
                .references(DTC_DEVICETYPE.name())
                .map(DeviceMessageFileImpl.Fields.DEVICE_TYPE.fieldName())
                .reverseMap(DeviceTypeImpl.Fields.DEVICE_MESSAGE_FILES.fieldName())
                .composition()
                .add();
            table.unique("UK_DTC_DEVICEMESSAGEFILENAME").on(deviceType, name, obsolete).add();
        }
    },
    DTC_DEVICETYPECALENDARUSAGE {
        @Override
        void addTo(DataModel dataModel) {
            Table<AllowedCalendar> table = dataModel.addTable(name(), AllowedCalendar.class).since(version(10, 2));
            table.map(AllowedCalendarImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceType = table.column("DEVICETYPE").number().notNull().add();
            Column calendar = table.column("CALENDAR").number().add();
            table.column("NAME").varChar().map(AllowedCalendarImpl.Fields.NAME.fieldName()).add();
            table.setJournalTableName("DTC_DEVICETYPE_CAL_USAGEJRNL").since(version(10, 2));
            table.addAuditColumns();
            table.column("OBSOLETE").number().conversion(NUMBER2INSTANT).map(AllowedCalendarImpl.Fields.OBSOLETE.fieldName()).add();
            table.primaryKey("PK_DTC_CALUSAGE").on(id).add();
            table.foreignKey("FK_DTC_CALDEVICETYPE")
                    .references(DTC_DEVICETYPE.name())
                    .on(deviceType)
                    .map(AllowedCalendarImpl.Fields.DEVICETYPE.fieldName())
                    .reverseMap(DeviceTypeImpl.Fields.ALLOWEDCALENDARS.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_DTC_CAL")
                    .references(Calendar.class)
                    .on(calendar)
                    .map(AllowedCalendarImpl.Fields.CALENDAR.fieldName())
                    .add();
        }
    },
    DTC_TIMEOFUSEMANAGEMENTOPTIONS {
        @Override
        void addTo(DataModel dataModel) {
            Table<TimeOfUseOptions> table = dataModel.addTable(name(), TimeOfUseOptions.class).since(version(10, 2));
            table.map(TimeOfUseOptionsImpl.class);
            Column deviceTypeColumn = table.column("DEVICETYPE").number().notNull().add();
            table.column("OPTIONS").number().map(TimeOfUseOptionsImpl.Fields.OPTION_BITS.fieldName()).conversion(NUMBER2LONG).notNull().add();
            table.setJournalTableName("DTC_TOU_MANAGEMENTOPTIONSJRNL").since(version(10, 2));
            table.addAuditColumns( );
            table.primaryKey("DTC_PK_TIMEOFUSEOPTIONS").on(deviceTypeColumn).add();
            table.foreignKey("DTC_TOUOPTIONS_FK_DEVICETYPE")
                    .references(DTC_DEVICETYPE.name())
                    .on(deviceTypeColumn)
                    .map(TimeOfUseOptionsImpl.Fields.DEVICETYPE.fieldName())
                    .add();
        }
    };

    abstract void addTo(DataModel component);
}