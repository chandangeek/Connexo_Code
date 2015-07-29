package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationEstimationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceConfValidationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypeFields;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperty;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

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
            table.addAuditColumns();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.column("DEVICEPROTOCOLPLUGGABLEID").number().conversion(ColumnConversion.NUMBER2LONG).map(DeviceTypeFields.DEVICE_PROTOCOL_PLUGGABLE_CLASS.fieldName()).add();
            table.column("DEVICEUSAGETYPE").number().conversion(ColumnConversion.NUMBER2INT).map("deviceUsageTypeId").add();
            table.unique("UK_DTC_DEVICETYPE").on(name).add();
            table.primaryKey("PK_DTC_DEVICETYPE").on(id).add();
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
                    .references(DeviceLifeCycleConfigurationService.COMPONENT_NAME, "DLD_DEVICE_LIFE_CYCLE")
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
            table.addAuditColumns();
            table.primaryKey("PK_DTC_LOADPRFTYPEFORDEVTYPE").on(loadProfileType, deviceType).add();
            table.foreignKey("FK_DTC_DEVTYPE_LPT_DEVTYPE").
                    on(deviceType).
                    references(DTC_DEVICETYPE.name())
                    .map("deviceType").
                    reverseMap("loadProfileTypeUsages").
                    composition().
                    onDelete(CASCADE).
                    add();
            table.foreignKey("FK_DTC_LPT_LPT_DEVTYPE").
                    on(loadProfileType).
                    references(MasterDataService.COMPONENTNAME, "MDS_LOADPROFILETYPE").
                    map("loadProfileType").
                    add();
        }
    },

    DTC_REGTYPEFORDEVICETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceTypeRegisterTypeUsage> table = dataModel.addTable(name(), DeviceTypeRegisterTypeUsage.class);
            table.map(DeviceTypeRegisterTypeUsage.class);
            Column registerType = table.column("REGISTERTYPEID").number().notNull().add();
            Column deviceType = table.column("DEVICETYPEID").number().notNull().add();
            table.addAuditColumns();
            table.primaryKey("PK_DTC_REGTYPEFORDEVICETYPE").on(registerType, deviceType).add();
            table.foreignKey("FK_DTC_DEVTYPE_REGTYPE_DEVTYPE").
                    on(deviceType).
                    references(DTC_DEVICETYPE.name()).
                    map("deviceType").
                    reverseMap("registerTypeUsages").
                    composition().
                    onDelete(CASCADE).
                    add();
            table.foreignKey("FK_DTC_MAPID_REGTYPE_DEVTYPE").
                    on(registerType).
                    references(MasterDataService.COMPONENTNAME, "MDS_MEASUREMENTTYPE")
                    .map("registerType").
                    add();
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
            table.foreignKey("FK_DTC_DEVTYPE_LOGBT_DEVTYPE").
                    on(deviceType).
                    references(DTC_DEVICETYPE.name()).
                    map("deviceType").
                    reverseMap("logBookTypeUsages").
                    composition().
                    onDelete(CASCADE).
                    add();
            table.foreignKey("FK_DTC_LBTYPE_LBTT_DEVTYPE").
                    on(logBookType).
                    references(MasterDataService.COMPONENTNAME, "MDS_LOGBOOKTYPE").
                    map("logBookType").
                    add();
        }
    },

    DTC_DEVICECONFIG {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceConfiguration> table = dataModel.addTable(name(), DeviceConfiguration.class);
            table.map(DeviceConfigurationImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            Column deviceType = table.column("DEVICETYPEID").number().notNull().add();
            table.column("ACTIVE").number().conversion(ColumnConversion.NUMBER2BOOLEAN).map("active").add();
            table.column("COMMUNICATIONFUNCTIONMASK").number().conversion(ColumnConversion.NUMBER2INT).map("communicationFunctionMask").add();
            table.column("SUPPORTALLCATEGORIES").number().conversion(NUMBER2BOOLEAN).notNull().map("supportsAllProtocolMessages").add();
            table.column("USERACTIONS").number().conversion(NUMBER2LONG).notNull().map("supportsAllProtocolMessagesUserActionsBitVector").add();
            table.column("GATEWAY_TYPE").number().conversion(ColumnConversion.NUMBER2ENUM).map(DeviceConfigurationImpl.Fields.GATEWAY_TYPE.fieldName()).notNull().add();
            table.primaryKey("PK_DTC_DEVICECONFIG").on(id).add();
            table.foreignKey("FK_DTC_DEVCONFIG_DEVTYPE").
                    on(deviceType).
                    references(DTC_DEVICETYPE.name()).
                    map("deviceType").
                    reverseMap("deviceConfigurations").
                    composition().
                    onDelete(CASCADE).
                    add();
        }
    },

    DTC_LOADPROFILESPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileSpec> table = dataModel.addTable(name(), LoadProfileSpec.class);
            table.map(LoadProfileSpecImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column deviceConfiguration = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column loadProfileType = table.column("LOADPROFILETYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("OBISCODE").varChar(80).map("overruledObisCodeString").add(); // obiscode is not a free field: derived from real obiscode, so user can not exceed max length
            table.primaryKey("PK_DTC_LOADPROFILESPECID").on(id).add();
            table.foreignKey("FK_DTC_LPRFSPEC_LOADPROFTYPE").
                    on(loadProfileType).
                    references(MasterDataService.COMPONENTNAME, "MDS_LOADPROFILETYPE").
                    map("loadProfileType").
                    add();
            table.foreignKey("FK_DTC_LPRFSPEC_DEVCONFIG").
                    on(deviceConfiguration).
                    references(DTC_DEVICECONFIG.name()).
                    map("deviceConfiguration").
                    reverseMap("loadProfileSpecs").
                    composition().
                    onDelete(CASCADE).
                    add();
        }
    },

    DTC_CHANNELSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ChannelSpec> table = dataModel.addTable(name(), ChannelSpec.class);
            table.map(ChannelSpecImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column deviceConfiguration = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column channelType = table.column("CHANNELTYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("OBISCODE").varChar(80).map("overruledObisCodeString").add();
            table.column("FRACTIONDIGITS").number().conversion(ColumnConversion.NUMBER2INT).map("nbrOfFractionDigits").add();
            table.column("OVERFLOWVALUE").number().map("overflow").add();
            table.column("READINGMETHOD").number().conversion(ColumnConversion.NUMBER2ENUM).notNull().map("readingMethod").add();
            table.column("VALUECALCULATIONMETHOD").number().conversion(ColumnConversion.NUMBER2ENUM).notNull().map("valueCalculationMethod").add();
            Column loadProfileSpec = table.column("LOADPROFILESPECID").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("INTERVAL").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.count").add();
            table.column("INTERVALCODE").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.timeUnitCode").add();
            table.primaryKey("PK_DTC_CHANNELSPEC").on(id).add();
            table.foreignKey("FK_DTC_CHANNELSPEC_DEVCONFIG").
                    on(deviceConfiguration).
                    references(DTC_DEVICECONFIG.name()).
                    map("deviceConfiguration").
                    onDelete(CASCADE).
                    add();
            table.foreignKey("FK_DTC_CHANNELSPEC_REGMAP").
                    on(channelType).
                    references(MasterDataService.COMPONENTNAME, "MDS_MEASUREMENTTYPE").
                    map("channelType").
                    add();
            table.foreignKey("FK_DTC_CHANNELSPEC_LPRFSPEC").
                    on(loadProfileSpec).
                    references(DTC_LOADPROFILESPEC.name()).
                    map("loadProfileSpec").
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
            table.addAuditColumns();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            Column registerType = table.column("REGISTERTYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("NUMBEROFDIGITS").number().conversion(ColumnConversion.NUMBER2INT).map(RegisterSpecFields.NUMBER_OF_DIGITS.fieldName()).add();
            table.column("DEVICEOBISCODE").varChar().map("overruledObisCodeString").add();
            table.column("NUMBEROFFRACTIONDIGITS").number().conversion(ColumnConversion.NUMBER2INT).map(RegisterSpecFields.NUMBER_OF_FRACTION_DIGITS.fieldName()).add();
            table.column("OVERFLOWVALUE").number().map(RegisterSpecFields.OVERFLOW_VALUE.fieldName()).add();
            Column deviceConfiguration = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.primaryKey("PK_DTC_REGISTERSPEC").on(id).add();
            table.foreignKey("FK_DTC_REGISTERSPEC_REGMAP").
                    on(registerType).
                    references(MasterDataService.COMPONENTNAME, "MDS_MEASUREMENTTYPE").
                    map(RegisterSpecFields.REGISTER_TYPE.fieldName()).
                    add();
            table.foreignKey("FK_DTC_REGISTERSPEC_DEVCONFIG").
                    on(deviceConfiguration).
                    references(DTC_DEVICECONFIG.name()).
                    map("deviceConfig").
                    reverseMap("registerSpecs").
                    composition().
                    onDelete(CASCADE).
                    add();
            table.unique("U_DTC_REGISTERTYPE_IN_CONFIG").
                    on(deviceConfiguration, registerType).
                    add();
        }
    },

    DTC_LOGBOOKSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBookSpec> table = dataModel.addTable(name(), LogBookSpec.class);
            table.map(LogBookSpecImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column deviceconfigid = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column logbooktypeid = table.column("LOGBOOKTYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("OBISCODE").varChar(80).map("overruledObisCodeString").add();
            table.primaryKey("PK_DTC_LOGBOOKSPEC").on(id).add();
            table.foreignKey("FK_DTC_LOGBOOKSPEC_DEVCONFIG").
                    on(deviceconfigid).
                    references(DTC_DEVICECONFIG.name()).
                    map("deviceConfiguration").
                    reverseMap("logBookSpecs").
                    composition().
                    onDelete(CASCADE).
                    add();
            table.foreignKey("FK_DTC_LOGBOOKSPEC_LOGBOOKTYPE").
                    on(logbooktypeid).
                    references(MasterDataService.COMPONENTNAME, "MDS_LOGBOOKTYPE")
                    .map("logBookType").
                    add();
        }
    },

    DTC_DIALECTCONFIGPROPERTIES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProtocolDialectConfigurationProperties> table = dataModel.addTable(name(), ProtocolDialectConfigurationProperties.class);
            table.map(ProtocolDialectConfigurationPropertiesImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column deviceConfiguration = table.column("DEVICECONFIGURATION").number().notNull().add();
            table.column("DEVICEPROTOCOLDIALECT").varChar().notNull().map("protocolDialectName").add();
            table.column("NAME").varChar().notNull().map("name").add();
            table.foreignKey("FK_DTC_DIALECTCONFPROPS_CONFIG").
                    on(deviceConfiguration).
                    references(DTC_DEVICECONFIG.name()).
                    map("deviceConfiguration").
                    reverseMap("configurationPropertiesList").
                    onDelete(CASCADE).
                    composition().
                    add();
            table.primaryKey("PK_DTC_DIALECTCONFIGPROPS").on(id).add();
        }
    },

    DTC_DIALECTCONFIGPROPSATTR {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProtocolDialectConfigurationProperty> table = dataModel.addTable(name(), ProtocolDialectConfigurationProperty.class);
            table.map(ProtocolDialectConfigurationPropertyImpl.class);
            Column id = table.column("ID").number().notNull().add();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.addAuditColumns();
            table.column("VALUE").varChar(4000).notNull().map("value").add();
            table.foreignKey("FK_DTC_CONFPROPSATTR_PROPS").
                    on(id).
                    references(DTC_DIALECTCONFIGPROPERTIES.name()).
                    map("properties").
                    composition().
                    reverseMap("propertyList").
                    onDelete(CASCADE).
                    add();
            table.primaryKey("PK_DTC_DIALECTCONFIGPROPSATTR").on(id,name).add();
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
            table.addAuditColumns();
            table.foreignKey("FK_DTC_PROTCONFPROPSATTR_CONF")
                    .on(deviceConfiguration)
                    .references(DTC_DEVICECONFIG.name())
                    .map("deviceConfiguration")
                    .composition()
                    .reverseMap("protocolProperties")
                    .onDelete(CASCADE)
                    .add();
            table.primaryKey("PK_DTC_PROTOCOLCONFPROPSATTR").on(deviceConfiguration, name).add();
        }
    },

    DTC_PARTIALCONNECTIONTASK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<PartialConnectionTask> table = dataModel.addTable(name(), PartialConnectionTask.class);
            table.map(PartialConnectionTaskImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.column("NAME").varChar().notNull().map("name").add();
            table.addDiscriminatorColumn("DISCRIMINATOR", "number");
            Column deviceConfiguration = table.column("DEVICECONFIG").number().add();
            Column connectionType = table.column("CONNECTIONTYPE").number().conversion(NUMBER2LONG).map("pluggableClassId").add();
            Column initiator = table.column("INITIATOR").number().add();
            table.column("COMWINDOWSTART").number().conversion(NUMBER2INT).map("comWindowStart").add();
            table.column("COMWINDOWEND").number().conversion(NUMBER2INT).map("comWindowEnd").add();
            table.column("CONNECTIONSTRATEGY").number().conversion(NUMBER2ENUM).map("connectionStrategy").add();
            table.column("SIMULTANEOUSCONNECTIONS").number().conversion(NUMBER2BOOLEAN).map("allowSimultaneousConnections").add();
            table.column("ISDEFAULT").number().conversion(NUMBER2BOOLEAN).map("isDefault").add();
            Column nextexecutionspecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("RESCHEDULERETRYDELAY").number().conversion(NUMBER2INT).map("rescheduleRetryDelay.count").add();
            Column comportpool = table.column("COMPORTPOOL").number().add();
            table.column("RESCHEDULERETRYDELAYCODE").number().conversion(NUMBER2INT).map("rescheduleRetryDelay.timeUnitCode").add();
            table.primaryKey("PK_DTC_PARTIALCONNTASK").on(id).add();
            table.foreignKey("FK_DTC_PARTIALCT_PLUGGABLE").
                    on(connectionType).
                    references(PluggableService.COMPONENTNAME, "CPC_PLUGGABLECLASS").
                    map("pluggableClass").
                    add();
            table.foreignKey("FK_DTC_PARTIALCT_COMPORTPOOL").
                    on(comportpool).
                    references(EngineConfigurationService.COMPONENT_NAME, "MDC_COMPORTPOOL").
                    map("comPortPool").
                    add();
            table.foreignKey("FK_DTC_PARTIALCT_DEVCONFIG").
                    on(deviceConfiguration).
                    references(DTC_DEVICECONFIG.name()).
                    map("configuration").
                    reverseMap("partialConnectionTasks").
                    onDelete(CASCADE).
                    composition().
                    add();
            table.foreignKey("FK_DTC_PARTIALCT_NEXTEXECSPEC").
                    on(nextexecutionspecs).
                    references(SchedulingService.COMPONENT_NAME, "SCH_NEXTEXECUTIONSPEC").
                    onDelete(CASCADE).
                    map("nextExecutionSpecs").
                    add();
            table.foreignKey("FK_DTC_PARTIALCT_INITIATOR").
                    on(initiator).
                    references(DTC_PARTIALCONNECTIONTASK.name()).
                    map("initiator").
                    add();
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
            table.addAuditColumns();
            table.foreignKey("FK_DTC_PARTIALCTPROPS_TASK").
                    on(partialconnectiontask).
                    references(DTC_PARTIALCONNECTIONTASK.name()).
                    map("partialConnectionTask").
                    reverseMap("properties").
                    onDelete(CASCADE).
                    composition().
                    add();
            table.primaryKey("PK_DTC_PARTIALCONTASKPROPS").on(partialconnectiontask,name).add();
        }
    },
    DTC_MESSAGEENABLEMENT {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceMessageEnablement> table = dataModel.addTable(name(), DeviceMessageEnablement.class);
            table.map(DeviceMessageEnablementImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceConfig = table.column("DEVICECONFIG").conversion(NUMBER2LONG).number().notNull().add();
            table.column("DEVICEMESSAGEID").number().conversion(NUMBER2ENUM).map("deviceMessageId").add();
            table.addAuditColumns();
            table.foreignKey("FK_DTC_DME_DEVCONFIG").
                    on(deviceConfig).
                    references(DTC_DEVICECONFIG.name()).
                    map("deviceConfiguration").
                    reverseMap(DeviceConfigurationImpl.Fields.DEVICE_MESSAGE_ENABLEMENTS.fieldName()).
                    onDelete(CASCADE).
                    composition().
                    add();
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
            table.addAuditColumns();
            table.foreignKey("FK_DTC_MESENUSRACTION").
                    on(deviceMessageEnablement).
                    references(DTC_MESSAGEENABLEMENT.name()).
                    reverseMap("deviceMessageUserActionRecords").
                    onDelete(CASCADE).
                    composition().
                    map("deviceMessageEnablement").
                    add();
            table.primaryKey("PK_DTC_MESENABLEUSERACTION").on(useraction,deviceMessageEnablement).add();
        }
    },
    DTC_SECURITYPROPERTYSET {
        @Override
        public void addTo(DataModel dataModel) {
            Table<SecurityPropertySet> table = dataModel.addTable(name(), SecurityPropertySet.class);
            table.map(SecurityPropertySetImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar().notNull().map("name").add();
            Column deviceConfiguration = table.column("DEVICECONFIG").conversion(NUMBER2LONG).number().notNull().add();
            table.column("AUTHENTICATIONLEVEL").number().conversion(NUMBER2INT).notNull().map("authenticationLevelId").add();
            table.column("ENCRYPTIONLEVEL").number().conversion(NUMBER2INT).notNull().map("encryptionLevelId").add();
            table.addAuditColumns();
            table.foreignKey("FK_DTC_SECPROPSET_DEVCONFIG").
                    on(deviceConfiguration).
                    references(DTC_DEVICECONFIG.name()).
                    map("deviceConfiguration").
                    reverseMap(DeviceConfigurationImpl.Fields.SECURITY_PROPERTY_SETS.fieldName()).
                    onDelete(CASCADE).
                    composition().
                    add();
            table.primaryKey("PK_DTC_SECURITYPROPERTYSET").on(id).add();
        }
    },
    DTC_SECURITYPROPSETUSERACTION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<SecurityPropertySetImpl.UserActionRecord> table = dataModel.addTable(name(), SecurityPropertySetImpl.UserActionRecord.class);
            table.map(SecurityPropertySetImpl.UserActionRecord.class);
            Column useraction = table.column("USERACTION").number().conversion(NUMBER2ENUM).notNull().map("userAction").add();
            Column securitypropertyset = table.column("SECURITYPROPERTYSET").number().notNull().add();
            table.addAuditColumns();
            table.foreignKey("FK_DTC_SECPROPSETUSRACT_SPS").
                    on(securitypropertyset).
                    references(DTC_SECURITYPROPERTYSET.name()).
                    reverseMap("userActionRecords").
                    onDelete(CASCADE).
                    composition().
                    map("set").
                    add();
            table.primaryKey("PK_DTC_SECPROPSETUSERACTION").on(useraction,securitypropertyset).add();
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
            table.
                foreignKey("FK_DTC_COMTASKENABLMNT_OPARTCT").
                on(partialConnectionTask).
                references(DTC_PARTIALCONNECTIONTASK.name()).
                map(ComTaskEnablementImpl.Fields.PARTIAL_CONNECTION_TASK.fieldName()).add();
            table.
                foreignKey("FK_DTC_COMTASKENABLMNT_SECURPS").
                on(securityPropertySet).
                references(DTC_SECURITYPROPERTYSET.name()).
                map(ComTaskEnablementImpl.Fields.SECURITY_PROPERTY_SET.fieldName()).add();
            table.
                foreignKey("FK_DTC_COMTASKENABLMNT_COMTASK").
                on(comtask).
                references(TaskService.COMPONENT_NAME, "CTS_COMTASK").
                map(ComTaskEnablementImpl.Fields.COM_TASK.fieldName()).add();
            table.
                foreignKey("FK_DTC_COMTASKENBLMNT_DCOMCONF").
                on(deviceCommunicationConfigation).
                references(DTC_DEVICECONFIG.name()).
                map(ComTaskEnablementImpl.Fields.CONFIGURATION.fieldName())
                    .reverseMap(DeviceConfigurationImpl.Fields.COM_TASK_ENABLEMENTS.fieldName())
                    .composition()
                    .onDelete(CASCADE)
                .add();
            table.
                foreignKey("FK_DTC_COMTASKENABLMNT_PDCP").
                on(dialectConfigurationProperties).
                references(DTC_DIALECTCONFIGPROPERTIES.name()).
                map(ComTaskEnablementImpl.Fields.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES.fieldName()).onDelete(CASCADE).add();
            table.unique("UK_DTC_COMTASKENABLEMENT").on(comtask,deviceCommunicationConfigation).add();
            table.primaryKey("PK_DTC_COMTASKENABLEMENT").on(id).add();
        }
    },

    //deviceConfValidationRuleSetUsages
    DTC_DEVCFGVALRULESETUSAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceConfValidationRuleSetUsage> table = dataModel.addTable(name(), DeviceConfValidationRuleSetUsage.class);
            table.map(DeviceConfValidationRuleSetUsageImpl.class);
            table.setJournalTableName("DTC_DEVCFGVALRULESETUSAGEJRNL");
            Column validationRuleSetIdColumn =
                    table.column("VALIDATIONRULESETID").type("number").notNull().conversion(NUMBER2LONG).map("validationRuleSetId").add();
            Column deviceConfigurationIdColumn =
                    table.column("DEVICECONFIGID").type("number").notNull().conversion(NUMBER2LONG).map("deviceConfigurationId").add();

            table.primaryKey("DTC_PK_SETCONFIGUSAGE").on(validationRuleSetIdColumn, deviceConfigurationIdColumn).add();
            table.foreignKey("DTC_FK_RULESET").references(ValidationService.COMPONENTNAME, "VAL_VALIDATIONRULESET").onDelete(RESTRICT).map("validationRuleSet").on(validationRuleSetIdColumn).add();
            table.foreignKey("DTC_FK_DEVICECONFIG").references("DTC_DEVICECONFIG").reverseMap("deviceConfValidationRuleSetUsages").composition().map("deviceConfiguration").on(deviceConfigurationIdColumn).add();
        }
    },

    //deviceConfEstimationRuleSetUsages
    DTC_DEVCFGESTRULESETUSAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceConfigurationEstimationRuleSetUsage> table = dataModel.addTable(name(), DeviceConfigurationEstimationRuleSetUsage.class);
            table.map(DeviceConfigurationEstimationRuleSetUsageImpl.class);
            table.setJournalTableName(name() + "JRNL");
            Column estimationRuleSetColumn = table.column("ESTIMATIONRULESET").type("number").notNull().conversion(NUMBER2LONG).add();
            Column deviceConfigurationColumn = table.column("DEVICECONFIG").type("number").notNull().conversion(NUMBER2LONG).add();
            table.column("POSITION").number().notNull().conversion(NUMBER2INT).map(DeviceConfigurationEstimationRuleSetUsageImpl.Fields.POSITION.fieldName()).add();
            table.addAuditColumns();

            table.primaryKey("DTC_PK_ESTRULESETUSAGE").on(estimationRuleSetColumn, deviceConfigurationColumn).add();

            table.foreignKey("DTC_FK_ESTIMATIONRULESET").
                    references(EstimationService.COMPONENTNAME, "EST_ESTIMATIONRULESET").
                    onDelete(RESTRICT).
                    map(DeviceConfigurationEstimationRuleSetUsageImpl.Fields.ESTIMATIONRULESET.fieldName()).on(estimationRuleSetColumn).
                    add();

            table.foreignKey("DTC_FK_ESTRSUSAGE_DEVICECONF").
                    references(DTC_DEVICECONFIG.name()).
                    reverseMap(DeviceConfigurationImpl.Fields.DEVICECONF_ESTIMATIONRULESET_USAGES.fieldName()).
                    composition().
                    reverseMapOrder(DeviceConfigurationEstimationRuleSetUsageImpl.Fields.POSITION.fieldName()).
                    map(DeviceConfigurationEstimationRuleSetUsageImpl.Fields.DEVICECONFIGURATION.fieldName()).on(deviceConfigurationColumn).
                    add();
        }
    },
    ;

    abstract void addTo(DataModel component);

}