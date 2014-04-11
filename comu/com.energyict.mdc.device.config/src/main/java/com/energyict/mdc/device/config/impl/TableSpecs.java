package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypeFields;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.PluggableService;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (11:17)
 */
public enum TableSpecs {

    EISPHENOMENON {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Phenomenon> table = dataModel.addTable(name(), Phenomenon.class);
            table.map(PhenomenonImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            Column unit = table.column("UNIT").type("CHAR(7)").notNull().map("unitString").add();
            table.column("MEASUREMENTCODE").varChar(80).map("measurementCode").add();
            table.column("EDICODE").varChar(80).map("ediCode").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").insert("sysdate").update("sysdate").add();
            table.primaryKey("PK_PHENOMENON").on(id).add();
            table.unique("UK_EISPHENOMENON").on(unit).add(); // Done so phenomenon can be identified solely by unit, cfr gna
        }
    },

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

    EISLOGBOOKTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBookType> table = dataModel.addTable(this.name(), LogBookType.class);
            table.map(LogBookTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("DESCRIPTION").varChar(255).map("description").add();
            table.column("OBISCODE").varChar(80).notNull().map(LogBookTypeImpl.Fields.OBIS_CODE.fieldName()).add();
            table.unique("UK_EISLOGBOOKTYPE").on(name).add();
            table.primaryKey("PK_EISLOGBOOKTYPE").on(id).add();
        }
    },

    EISLOADPROFILETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileType> table = dataModel.addTable(this.name(), LoadProfileType.class);
            table.map(LoadProfileTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            table.column("DESCRIPTION").varChar(255).map("description").add();
            table.column("OBISCODE").varChar(80).notNull().map(LoadProfileTypeImpl.Fields.OBIS_CODE.fieldName()).add();
            table.column("INTERVALCOUNT").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.count").add();
            table.column("INTERVALUNIT").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.timeUnitCode").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            table.unique("UK_LOADPROFILETYPE").on(name).add();
            table.primaryKey("PK_LOADPROFILETYPE").on(id).add();
        }
    },

    EISRTUREGISTERGROUP {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterGroup> table = dataModel.addTable(this.name(), RegisterGroup.class);
            table.map(RegisterGroupImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(256).notNull().map("name").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            table.unique("UK_RTUREGISTERGROUP").on(name).add();
            table.primaryKey("PK_RTUREGISTERGROUP").on(id).add();
        }
    },

    EISRTUREGISTERMAPPING {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterMapping> table = dataModel.addTable(this.name(), RegisterMapping.class);
            table.map(RegisterMappingImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(128).notNull().map("name").add();
            Column obisCode = table.column("OBISCODE").varChar(80).notNull().map(RegisterMappingImpl.Fields.OBIS_CODE.fieldName()).add();
            Column phenomenon = table.column("PHENOMENONID").number().conversion(ColumnConversion.NUMBER2INT).notNull().add();
            Column readingType = table.column("READINGTYPE").varChar(100).add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            table.column("CUMULATIVE").number().conversion(NUMBER2BOOLEAN).notNull().map("cumulative").add();
            Column registerGroup = table.column("REGISTERGROUPID").number().add();
            table.column("DESCRIPTION").varChar(255).map("description").add();
            Column timeOfUse = table.column("TIMEOFUSE").number().map("timeOfUse").conversion(ColumnConversion.NUMBER2INT).add();
            table.foreignKey("FK_EISREGMAP_REGGROUP").on(registerGroup).references(EISRTUREGISTERGROUP.name()).map("registerGroup").add();
            table.foreignKey("FK_EISREGMAP_PHENOMENON").on(phenomenon).references(EISPHENOMENON.name()).map("phenomenon").add();
            table.foreignKey("FK_EISREGMAP_READINGTYPE").on(readingType).references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE").map("readingType").add();
            table.unique("UK_RTUREGMAPPINGNAME").on(name).add();
            table.unique("UK_RTUREGMREADINGTYPE").on(readingType).add();
            table.primaryKey("PK_RTUREGISTERMAPPING").on(id).add();
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
            table.foreignKey("FK_LPTID_LPT_RTUTYPE_JOIN").on(loadProfileType).references(EISLOADPROFILETYPE.name()).map("loadProfileType").add();
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
            table.foreignKey("FK_MAPID_REGMAP_RTUTYPE_JOIN").on(registermapping).references(EISRTUREGISTERMAPPING.name()).map("registerMapping").add();
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
            table.foreignKey("FK_LBTYPEID_LBTT_RTUTYPE_JOIN").on(logBookType).references(EISLOGBOOKTYPE.name()).map("logBookType").add();
        }
    },

    EISREGMAPPINGINLOADPROFILETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileTypeRegisterMappingUsage> table = dataModel.addTable(name(), LoadProfileTypeRegisterMappingUsage.class);
            table.map(LoadProfileTypeRegisterMappingUsage.class);
            Column loadProfileType = table.column("LOADPROFILETYPEID").number().notNull().add();
            Column registerMapping = table.column("REGMAPPINGID").number().notNull().add();
            table.primaryKey("PK_REGMAPPINGINLOADPROFILETYPE").on(loadProfileType, registerMapping).add();
            table.foreignKey("FK_REGMAPLPT_LOADPROFILETYPEID").on(loadProfileType).references(EISLOADPROFILETYPE.name()).map("loadProfileType").reverseMap("registerMappingUsages").composition().add();
            table.foreignKey("FK_REGMAPLPT_REGMAPPINGID").on(registerMapping).references(EISRTUREGISTERMAPPING.name()).map("registerMapping").add();
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
            table.foreignKey("FK_EISLPRFSPEC_LOADPROFTYPE").on(loadprofiletypeid).references(EISLOADPROFILETYPE.name()).map("loadProfileType").add();
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
            table.foreignKey("FK_EISCHNSPEC_REGMAP").on(rturegistermappingid).references(EISRTUREGISTERMAPPING.name()).map("registerMapping").add();
            table.foreignKey("FK_EISCHNSPEC_PHENOM").on(phenomenonid).references(EISPHENOMENON.name()).map("phenomenon").add();
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
            table.foreignKey("FK_EISRTUREGSPEC_REGMAP").on(registerMapping).references(EISRTUREGISTERMAPPING.name()).map(RegisterSpecImpl.Fields.REGISTER_MAPPING.fieldName()).add();
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
            table.foreignKey("FK_EISLGBSPEC_LOGBOOKTYPE").on(logbooktypeid).references(EISLOGBOOKTYPE.name()).map("logBookType").add();
        }
    },

    MDCNEXTEXECUTIONSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<NextExecutionSpecs> table = dataModel.addTable(name(), NextExecutionSpecs.class);
            table.map(NextExecutionSpecsImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("FREQUENCYVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("temporalExpression.every.count").add();
            table.column("FREQUENCYUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("temporalExpression.every.timeUnitCode").add();
            table.column("OFFSETVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("temporalExpression.offset.count").add();
            table.column("OFFSETUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("temporalExpression.offset.timeUnitCode").add();
            table.primaryKey("PK_MDCNEXTEXEC_SPEC").on(id).add();
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
            Column devicecomconfig = table.column("DEVICECOMCONFIG").number().add();
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
            table.foreignKey("FK_MDCPARTCONTASK_DCOMCONFIG").on(devicecomconfig).references(MDCDEVICECOMMCONFIG.name()).map("configuration").reverseMap("partialConnectionTasks").onDelete(CASCADE).composition().add();
            table.foreignKey("FK_MDCPARTIALCONNTASK_NEXTEX").on(nextexecutionspecs).references(MDCNEXTEXECUTIONSPEC.name()).onDelete(CASCADE).map("nextExecutionSpecs").add();
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
            table.foreignKey("FK_MDCSECPROPSET_DEVCOMCONFIG").on(devicecomconfig).references(MDCDEVICECOMMCONFIG.name()).map("deviceCommunicationConfiguration").reverseMap("securityPropertySets").composition().add();
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
    ;

    abstract void addTo(DataModel component);

}