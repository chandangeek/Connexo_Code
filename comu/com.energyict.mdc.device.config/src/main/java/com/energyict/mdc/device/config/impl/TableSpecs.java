package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypeFields;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import static com.elster.jupiter.orm.ColumnConversion.DATE2DATE;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (11:17)
 */
public enum TableSpecs {

    EISSYSRTUTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceType> table = dataModel.addTable(this.name(), DeviceType.class);
            table.map(DeviceTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("DESCRIPTION").varChar(4000).map("description").add();
            table.column("USECHANNELJOURNAL").number().conversion(ColumnConversion.NUMBER2BOOLEAN).notNull().map("useChannelJournal").add();
            table.column("DEVICEPROTOCOLPLUGGABLEID").number().conversion(ColumnConversion.NUMBER2LONG).map(DeviceTypeFields.DEVICE_PROTOCOL_PLUGGABLE_CLASS.fieldName()).add();
            table.column("DEVICEUSAGETYPE").number().conversion(ColumnConversion.NUMBER2INT).map("deviceUsageTypeId").add();
            table.unique("UK_SYSRTUTYPE").on(name).add();
            table.primaryKey("PK_SYSRTUTYPE").on(id).add();
        }
    },

    EISLOADPRFTYPEFORRTUTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceTypeLoadProfileTypeUsage> table = dataModel.addTable(name(), DeviceTypeLoadProfileTypeUsage.class);
            table.map(DeviceTypeLoadProfileTypeUsage.class);
            Column loadProfileType = table.column("LOADPROFILETYPEID").number().notNull().add();
            Column deviceType = table.column("RTUTYPEID").number().notNull().add();
            table.primaryKey("PK_LOADPRFTYPEFORRTUTYPE").on(loadProfileType, deviceType).add();
            table.foreignKey("FK_RTUTYPEID_LPT_RTUTYPE_JOIN").on(deviceType).references(EISSYSRTUTYPE.name()).map("deviceType").reverseMap("loadProfileTypeUsages").composition().add();
            table.foreignKey("FK_LPTID_LPT_RTUTYPE_JOIN").on(loadProfileType).references(MasterDataService.COMPONENTNAME, "EISLOADPROFILETYPE").map("loadProfileType").add();
        }
    },

    EISREGMAPFORRTUTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceTypeRegisterMappingUsage> table = dataModel.addTable(name(), DeviceTypeRegisterMappingUsage.class);
            table.map(DeviceTypeRegisterMappingUsage.class);
            Column registermapping = table.column("REGISTERMAPPINGID").number().notNull().add();
            Column deviceType = table.column("RTUTYPEID").number().notNull().add();
            table.primaryKey("PK_REGMAPFORRTUTYPE").on(registermapping, deviceType).add();
            table.foreignKey("FK_RTUTPID_REGMAP_RTUTYPE_JOIN").on(deviceType).references(EISSYSRTUTYPE.name()).map("deviceType").reverseMap("registerMappingUsages").composition().add();
            table.foreignKey("FK_MAPID_REGMAP_RTUTYPE_JOIN").on(registermapping).references(MasterDataService.COMPONENTNAME, "EISRTUREGISTERMAPPING").map("registerMapping").add();
        }
    },

    EISLOGBOOKTYPEFORRTUTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceTypeLogBookTypeUsage> table = dataModel.addTable(name(), DeviceTypeLogBookTypeUsage.class);
            table.map(DeviceTypeLogBookTypeUsage.class);
            Column logBookType = table.column("LOGBOOKTYPEID").number().notNull().add();
            Column deviceType = table.column("RTUTYPEID").number().notNull().add();
            table.primaryKey("PK_LOGBOOKTYPEFORRTUTYPE").on(logBookType, deviceType).add();
            table.foreignKey("FK_RTUTYPEID_LBT_RTUTYPE_JOIN").on(deviceType).references(EISSYSRTUTYPE.name()).map("deviceType").reverseMap("logBookTypeUsages").composition().add();
            table.foreignKey("FK_LBTYPEID_LBTT_RTUTYPE_JOIN").on(logBookType).references(MasterDataService.COMPONENTNAME, "EISLOGBOOKTYPE").map("logBookType").add();
        }
    },

    EISDEVICECONFIG {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceConfiguration> table = dataModel.addTable(name(), DeviceConfiguration.class);
            table.map(DeviceConfigurationImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("DESCRIPTION").varChar(4000).map("description").add();
            Column deviceTypeId = table.column("DEVICETYPEID").number().notNull().add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").insert("sysdate").update("sysdate").add();
            table.column("ACTIVE").number().conversion(ColumnConversion.NUMBER2BOOLEAN).map("active").add();
            table.column("COMMUNICATIONFUNCTIONMASK").number().conversion(ColumnConversion.NUMBER2INT).map("communicationFunctionMask").add();
            table.primaryKey("PK_EISDEVICECONFIG").on(id).add();
            table.foreignKey("FK_EISDEVCFG_DEVTYPE").on(deviceTypeId).references(EISSYSRTUTYPE.name()).map("deviceType").reverseMap("deviceConfigurations").composition().onDelete(CASCADE).add();
        }
    },

    EISLOADPROFILESPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileSpec> table = dataModel.addTable(name(), LoadProfileSpec.class);
            table.map(LoadProfileSpecImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceconfigid = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column loadprofiletypeid = table.column("LOADPROFILETYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("OBISCODE").varChar(80).map("overruledObisCodeString").add();
            table.primaryKey("PK_EISLOADPROFILESPECID").on(id).add();
            table.foreignKey("FK_EISLPRFSPEC_LOADPROFTYPE").on(loadprofiletypeid).references(MasterDataService.COMPONENTNAME, "EISLOADPROFILETYPE").map("loadProfileType").add();
            table.foreignKey("FK_EISLPRFSPEC_DEVCONFIG").on(deviceconfigid).references(EISDEVICECONFIG.name()).map("deviceConfiguration").reverseMap("loadProfileSpecs").composition().onDelete(CASCADE).add();
        }
    },

    EISCHANNELSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ChannelSpec> table = dataModel.addTable(name(), ChannelSpec.class);
            table.map(ChannelSpecImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar(80).notNull().map("name").add();
            Column deviceconfigid = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column rturegistermappingid = table.column("RTUREGISTERMAPPINGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("OBISCODE").varChar(80).map("overruledObisCodeString").add();
            table.column("FRACTIONDIGITS").number().conversion(ColumnConversion.NUMBER2INT).map("nbrOfFractionDigits").add();
            table.column("OVERFLOWVALUE").number().map("overflow").add();
            Column phenomenonid = table.column("PHENOMENONID").number().notNull().add();
            table.column("READINGMETHOD").number().conversion(ColumnConversion.NUMBER2ENUM).notNull().map("readingMethod").add();
            table.column("MULTIPLIERMODE").number().conversion(ColumnConversion.NUMBER2ENUM).notNull().map("multiplierMode").add();
            table.column("MULTIPLIER").number().notNull().map("multiplier").add();
            table.column("VALUECALCULATIONMETHOD").number().conversion(ColumnConversion.NUMBER2ENUM).notNull().map("valueCalculationMethod").add();
            Column loadprofilespecid = table.column("LOADPROFILESPECID").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("INTERVAL").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.count").add();
            table.column("INTERVALCODE").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.timeUnitCode").add();
            table.primaryKey("PK_EISCHANNELSPECID").on(id).add();
            table.foreignKey("FK_EISCHNSPEC_DEVCONFIG").on(deviceconfigid).references(EISDEVICECONFIG.name()).map("deviceConfiguration").reverseMap("channelSpecs").composition().onDelete(CASCADE).add();
            table.foreignKey("FK_EISCHNSPEC_REGMAP").on(rturegistermappingid).references(MasterDataService.COMPONENTNAME, "EISRTUREGISTERMAPPING").map("registerMapping").add();
            table.foreignKey("FK_EISCHNSPEC_PHENOM").on(phenomenonid).references(MasterDataService.COMPONENTNAME, "EISPHENOMENON").map("phenomenon").add();
            table.foreignKey("FK_EISCHNSPEC_LPROFSPEC").on(loadprofilespecid).references(EISLOADPROFILESPEC.name()).map("loadProfileSpec").add();
        }
    },

    EISRTUREGISTERSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterSpec> table = dataModel.addTable(name(), RegisterSpec.class);
            table.map(RegisterSpecImpl.class);
            Column id = table.addAutoIdColumn();
            Column registerMapping = table.column("REGISTERMAPPINGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("NUMBEROFDIGITS").number().conversion(ColumnConversion.NUMBER2INT).notNull().map(RegisterSpecImpl.Fields.NUMBER_OF_DIGITS.fieldName()).add();
            table.column("MOD_DATE").type("DATE").notNull().map("modificationDate").insert("sysdate").update("sysdate").add();
            table.column("DEVICEOBISCODE").varChar(80).map("overruledObisCodeString").add();
            table.column("NUMBEROFFRACTIONDIGITS").number().conversion(ColumnConversion.NUMBER2INT).map(RegisterSpecImpl.Fields.NUMBER_OF_FRACTION_DIGITS.fieldName()).add();
            table.column("OVERFLOWVALUE").number().map("overflow").add();
            Column deviceConfiguration = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("MULTIPLIER").number().map(RegisterSpecImpl.Fields.MULTIPLIER.fieldName()).add();
            table.column("MULTIPLIERMODE").number().conversion(ColumnConversion.NUMBER2ENUM).notNull().map("multiplierMode").add();
            table.primaryKey("PK_RTUREGISTERSPEC").on(id).add();
            table.foreignKey("FK_EISRTUREGSPEC_REGMAP").on(registerMapping).references(MasterDataService.COMPONENTNAME, "EISRTUREGISTERMAPPING").map(RegisterSpecImpl.Fields.REGISTER_MAPPING.fieldName()).add();
            table.foreignKey("FK_EISRTUREGSPEC_DEVCFG").on(deviceConfiguration).references(EISDEVICECONFIG.name()).map("deviceConfig").reverseMap("registerSpecs").composition().onDelete(CASCADE).add();
        }
    },

    EISLOGBOOKSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBookSpec> table = dataModel.addTable(name(), LogBookSpec.class);
            table.map(LogBookSpecImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceconfigid = table.column("DEVICECONFIGID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column logbooktypeid = table.column("LOGBOOKTYPEID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("OBISCODE").varChar(80).map("overruledObisCodeString").add();
            table.primaryKey("PK_EISLOGBOOKSPECID").on(id).add();
            table.foreignKey("FK_EISLGBSPEC_DEVCONFIG").on(deviceconfigid).references(EISDEVICECONFIG.name()).map("deviceConfiguration").reverseMap("logBookSpecs").composition().onDelete(CASCADE).add();
            table.foreignKey("FK_EISLGBSPEC_LOGBOOKTYPE").on(logbooktypeid).references(MasterDataService.COMPONENTNAME, "EISLOGBOOKTYPE").map("logBookType").add();
        }
    },

    MDCDEVICECOMMCONFIG {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceCommunicationConfiguration> table = dataModel.addTable(name(), DeviceCommunicationConfiguration.class);
            table.map(DeviceCommunicationConfigurationImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceconfiguration = table.column("DEVICECONFIGURATION").number().add();
            table.column("SUPPORTALLCATEGORIES").number().conversion(NUMBER2BOOLEAN).notNull().map("supportsAllMessageCategories").add();
            table.column("USERACTIONS").number().conversion(NUMBER2LONG).notNull().map("userActions").add();
            table.foreignKey("FK_MDCDEVICECOMMCONFIG_DCONFIG").on(deviceconfiguration).references(EISDEVICECONFIG.name()).map("deviceConfiguration").add();
            table.primaryKey("PK_MDCDEVICECOMMCONFIG").on(id).add();
        }
    },

    MDCDIALECTCONFIGPROPERTIES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProtocolDialectConfigurationProperties> table = dataModel.addTable(name(), ProtocolDialectConfigurationProperties.class);
            table.map(ProtocolDialectConfigurationPropertiesImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceConfiguration = table.column("DEVICECONFIGURATION").number().notNull().add(); // TODO remove map when enabling foreign key constraint
            table.column("DEVICEPROTOCOLDIALECT").varChar(255).notNull().map("protocolDialectName").add();
            table.column("MOD_DATE").type("DATE").map("modDate").add();
            table.column("NAME").varChar(255).notNull().map("name").add();
            table.foreignKey("FK_MDCDEVICECONFIG_CONFIGID").on(deviceConfiguration).references(MDCDEVICECOMMCONFIG.name()).map("deviceCommunicationConfiguration").reverseMap("configurationPropertiesList").onDelete(CASCADE).composition().add();
            table.primaryKey("PK_MDCDIALECTCONFIGPROPS").on(id).add();
        }
    },

    MDCDIALECTCONFIGPROPERTIESATTR {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProtocolDialectConfigurationProperty> table = dataModel.addTable(name(), ProtocolDialectConfigurationProperty.class);
            table.map(ProtocolDialectConfigurationProperty.class);
            Column id = table.column("ID").number().notNull().add();
            Column name = table.column("NAME").varChar(255).notNull().map("name").add();
            table.column("VALUE").varChar(4000).notNull().map("value").add();
            table.foreignKey("FK_MDCDCONFPROPSATTR_CONFIGID").on(id).references(MDCDIALECTCONFIGPROPERTIES.name()).map("properties").composition().reverseMap("propertyList").add();
            table.primaryKey("PK_MDCDIALECTCONFIGPROPSATTR").on(id,name).add();
        }
    },

    MDCPARTIALCONNECTIONTASK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<PartialConnectionTask> table = dataModel.addTable(name(), PartialConnectionTask.class);
            table.map(PartialConnectionTaskImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar(255).notNull().map("name").add();
            table.addDiscriminatorColumn("DISCRIMINATOR", "number");
            Column deviceComConfig = table.column("DEVICECOMCONFIG").number().add();
            Column connectionType = table.column("CONNECTIONTYPE").number().conversion(NUMBER2LONG).map("pluggableClassId").add();
            Column initiator = table.column("INITIATOR").number().add();
            table.column("COMWINDOWSTART").number().conversion(NUMBER2INT).map("comWindowStart").add();
            table.column("COMWINDOWEND").number().conversion(NUMBER2INT).map("comWindowEnd").add();
            table.column("CONNECTIONSTRATEGY").number().conversion(NUMBER2ENUM).map("connectionStrategy").add();
            table.column("SIMULTANEOUSCONNECTIONS").number().conversion(NUMBER2BOOLEAN).map("allowSimultaneousConnections").add();
            table.column("ISDEFAULT").number().conversion(NUMBER2BOOLEAN).map("isDefault").add();
            Column nextexecutionspecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("RESCHEDULERETRYDELAY").number().conversion(NUMBER2INT).map("rescheduleRetryDelay.count").add();
            table.column("MOD_DATE").type("DATE").map("modDate").insert("sysdate").update("sysdate").add();
            Column comportpool = table.column("COMPORTPOOL").number().add();
            table.column("RESCHEDULERETRYDELAYCODE").number().conversion(NUMBER2INT).map("rescheduleRetryDelay.timeUnitCode").add();
            table.primaryKey("PK_MDCPARTIALCONNTASK").on(id).add();
            table.foreignKey("FK_MDCPARTIALCT_PLUGGABLE").on(connectionType).references(PluggableService.COMPONENTNAME, "EISPLUGGABLECLASS").map("pluggableClass").add();
            table.foreignKey("FK_MDCPARTIALCT_COMPORTPOOL").on(comportpool).references(EngineModelService.COMPONENT_NAME, "MDCCOMPORTPOOL").map("comPortPool").add();
            table.foreignKey("FK_MDCPARTCONTASK_DCOMCONFIG").on(deviceComConfig).references(MDCDEVICECOMMCONFIG.name()).map("configuration").reverseMap("partialConnectionTasks").onDelete(CASCADE).composition().add();
            table.foreignKey("FK_MDCPARTIALCONNTASK_NEXTEX").on(nextexecutionspecs).references(SchedulingService.COMPONENT_NAME, "MDCNEXTEXECUTIONSPEC").onDelete(CASCADE).map("nextExecutionSpecs").add();
            table.foreignKey("FK_MDCPARTIALCONNTASK_INIT").on(initiator).references(MDCPARTIALCONNECTIONTASK.name()).map("initiator").add();
        }
    },
    MDCPARTIALCONNECTIONTASKPROPS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<PartialConnectionTaskPropertyImpl> table = dataModel.addTable(name(), PartialConnectionTaskPropertyImpl.class);
            table.map(PartialConnectionTaskPropertyImpl.class);
            Column partialconnectiontask = table.column("PARTIALCONNECTIONTASK").number().notNull().add();
            Column name = table.column("NAME").varChar(255).notNull().map("name").add();
            table.column("VALUE").varChar(4000).notNull().map("value").add();
            table.foreignKey("FK_MDCPARTIALPROPS_TASK").on(partialconnectiontask).references(MDCPARTIALCONNECTIONTASK.name()).map("partialConnectionTask").reverseMap("properties").onDelete(CASCADE).composition().add();
            table.primaryKey("PK_MDCPARTIALPROPS").on(partialconnectiontask,name).add();
        }
    },
    MDCSECURITYPROPERTYSET {
        @Override
        public void addTo(DataModel dataModel) {
            Table<SecurityPropertySet> table = dataModel.addTable(name(), SecurityPropertySet.class);
            table.map(SecurityPropertySetImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar(255).notNull().map("name").add();
            Column devicecomconfig = table.column("DEVICECOMCONFIG").conversion(NUMBER2LONG).number().notNull().add();
            table.column("AUTHENTICATIONLEVEL").number().conversion(NUMBER2INT).notNull().map("authenticationLevelId").add();
            table.column("ENCRYPTIONLEVEL").number().conversion(NUMBER2INT).notNull().map("encryptionLevelId").add();
            table.
                foreignKey("FK_MDCSECPROPSET_DEVCOMCONFIG").
                on(devicecomconfig).references(MDCDEVICECOMMCONFIG.name()).
                    map("deviceCommunicationConfiguration").
                    reverseMap(DeviceCommunicationConfigurationImpl.Fields.SECURITY_PROPERTY_SETS.fieldName()).composition().add();
            table.primaryKey("PK_MDCSECURITYPROPERTYSET").on(id).add();
        }
    },
    MDCSECURITYPROPSETUSERACTION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<SecurityPropertySetImpl.UserActionRecord> table = dataModel.addTable(name(), SecurityPropertySetImpl.UserActionRecord.class);
            table.map(SecurityPropertySetImpl.UserActionRecord.class);
            Column useraction = table.column("USERACTION").number().conversion(NUMBER2ENUM).notNull().map("userAction").add();
            Column securitypropertyset = table.column("SECURITYPROPERTYSET").number().notNull().add();
            table.foreignKey("FK_MDCSECPROPSETUSRACT_SPS").on(securitypropertyset).references(MDCSECURITYPROPERTYSET.name()).reverseMap("userActionRecords").onDelete(CASCADE).composition().map("set").add();
            table.primaryKey("PK_MDCSECURITYPROPETUSERACTION").on(useraction,securitypropertyset).add();
        }
    },
    MDCCOMTASKENABLEMENT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComTaskEnablement> table = dataModel.addTable(name(), ComTaskEnablement.class);
            table.map(ComTaskEnablementImpl.class);
            Column id = table.addAutoIdColumn();
            Column comtask = table.column("COMTASK").number().notNull().add();
            Column deviceCommunicationConfigation = table.column("DEVICECOMCONFIG").number().notNull().add();
            Column securityPropertySet = table.column("SECURITYPROPERTYSET").number().notNull().add();
            Column nextExecutionSpecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("SUSPENDED").number().notNull().conversion(NUMBER2BOOLEAN).map(ComTaskEnablementImpl.Fields.SUSPENDED.fieldName()).add();
            Column partialConnectionTask = table.column("PARTIALCONNECTIONTASK").number().add();
            table.column("USEDEFAULTCONNECTIONTASK").number().notNull().conversion(NUMBER2BOOLEAN).map(ComTaskEnablementImpl.Fields.USE_DEFAULT_CONNECTION_TASK.fieldName()).add();
            table.column("PRIORITY").number().notNull().conversion(NUMBER2INT).map(ComTaskEnablementImpl.Fields.PRIORITY.fieldName()).add();
            Column dialectConfigurationProperties = table.column("DIALECTCONFIGPROPERTIES").number().add();
            table.column("IGNORENEXTEXECSPECS").number().notNull().conversion(NUMBER2BOOLEAN).map(ComTaskEnablementImpl.Fields.IGNORE_NEXT_EXECUTION_SPECS_FOR_INBOUND.fieldName()).add();
            table.
                foreignKey("FK_MDCCOMTASKENABLMNT_OPARTCT").
                on(partialConnectionTask).
                references(MDCPARTIALCONNECTIONTASK.name()).
                    map(ComTaskEnablementImpl.Fields.PARTIAL_CONNECTION_TASK.fieldName()).add();
            table.
                foreignKey("FK_MDCCOMTASKSEC_SECPROPSET").
                on(securityPropertySet).
                references(MDCSECURITYPROPERTYSET.name()).
                    map(ComTaskEnablementImpl.Fields.SECURITY_PROPERTY_SET.fieldName()).add();
            table.
                foreignKey("FK_MDCCOMTASKSEC_COMTASK").
                on(comtask).
                references(TaskService.COMPONENT_NAME, "MDCCOMTASK").
                    map(ComTaskEnablementImpl.Fields.COM_TASK.fieldName()).add();
            table.
                foreignKey("FK_MDCCOMTASKENBLMNT_DCOMCONF").
                on(deviceCommunicationConfigation).
                references(MDCDEVICECOMMCONFIG.name()).
                    map(ComTaskEnablementImpl.Fields.CONFIGURATION.fieldName()).
                    reverseMap(DeviceCommunicationConfigurationImpl.Fields.COM_TASK_ENABLEMENTS.fieldName()).composition().add();
            table.
                foreignKey("FK_MDCCOMTASKENABLMNT_NEXTEXEC").
                on(nextExecutionSpecs).
                references(SchedulingService.COMPONENT_NAME, "MDCNEXTEXECUTIONSPEC").
                    map(ComTaskEnablementImpl.Fields.NEXT_EXECUTION_SPECS.fieldName()).add();
            table.
                foreignKey("FK_MDCCOMTASKENABLMNT_PDCP").
                on(dialectConfigurationProperties).
                references(MDCDIALECTCONFIGPROPERTIES.name()).
                    map(ComTaskEnablementImpl.Fields.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES.fieldName()).add();
            table.unique("UK_MDCCOMTASKENABLEMENT").on(comtask,deviceCommunicationConfigation).add();
            table.primaryKey("PK_MDCCOMTASKENABLEMENT").on(id).add();
        }
    }
    ;

    abstract void addTo(DataModel component);

}