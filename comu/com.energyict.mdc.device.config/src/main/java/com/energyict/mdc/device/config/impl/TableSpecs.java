package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;

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
            table.column("CHANNELCOUNT").number().notNull().map("channelcount").add();
            table.column("DESCRIPTION").varChar(4000).map("description").add();
            table.column("PROTOTYPEID").number().map("prototypeid").add();
            table.column("USECHANNELJOURNAL").number().notNull().map("usechanneljournal").add();
            table.column("NEEDSPROXY").number().map("needsproxy").add();
            table.column("DEVICEPROTOCOLPLUGGABLEID").number().map("deviceprotocolpluggableid").add();
            table.column("DEVICEUSAGETYPE").number().map("deviceusagetype").add();
            table.column("DEVICECOLLECTIONMETHOD").number().map("devicecollectionmethod").add();
            table.column("COMMUNICATIONFUNCTIONMASK").number().map("communicationfunctionmask").add();
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
            table.column("OBISCODE").varChar(80).notNull().map("obiscode").add();
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
            table.column("OBISCODE").varChar(80).notNull().map("obiscode").add();
            table.column("INTERVALCOUNT").number().notNull().map("intervalcount").add();
            table.column("INTERVALUNIT").number().notNull().map("intervalunit").add();
            table.column("MOD_DATE").type("DATE").notNull().map("modDate").insert("sysdate").update("sysdate").add();
            table.unique("UK_LOADPROFILETYPE").on(name).add();
            table.primaryKey("PK_LOADPROFILETYPE").on(id).add();
        }
    },

    EISRTUREGISTERMAPPING {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterMapping> table = dataModel.addTable(this.name(), RegisterMapping.class);
            table.map(RegisterMappingImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            Column obiscode = table.column("OBISCODE").varChar(80).notNull().map("obisCodeString").add();
            Column productSpec = table.column("PRODUCTSPECID").number().notNull().add();
            table.column("MOD_DATE").type("DATE").notNull().map("modificationDate").add();
            table.column("CUMULATIVE").number().notNull().map("cumulative").add();
            Column registerGroup = table.column("REGISTERGROUPID").number().add();
            table.column("DESCRIPTION").varChar(255).map("description").add();
            table.foreignKey("FK_EISREGMAPREGGROUP").on(registerGroup).references(EISRTUREGISTERGROUP.name()).map("registerGroup").add();
            table.foreignKey("FK_EISREGMAPPRODSPEC").on(productSpec).references(EISPRODUCTSPEC.name()).map("productSpec").add();
            table.unique("UK_RTUREGMAPPINGNAME").on(name).add();
            table.unique("UK_RTUREGMAPPINGOBISPROD").on(obiscode,productSpec).add();
            table.primaryKey("PK_RTUREGISTERMAPPING").on(id).add();
        }
    },

    EISRTUREGISTERGROUP {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterGroup> table = dataModel.addTable(this.name(), RegisterGroup.class);
            table.map(RegisterGroupImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(256).notNull().map("name").add();
            table.column("MOD_DATE").type("DATE").notNull().map("modificationDate").add();
            table.column("USEABLEINMMR").number().map("useableinmmr").add();
            table.unique("UK_RTUREGISTERGROUP").on(name).add();
            table.primaryKey("PK_RTUREGISTERGROUP").on(id).add();
        }
    },

    EISPRODUCTSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProductSpec> table = dataModel.addTable(this.name(), ProductSpec.class);
            table.map(ProductSpecImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("READINGTYPE").varChar(100).map("readingType").add();
            table.column("MOD_DATE").type("DATE").notNull().map("modDate").insert("sysdate").update("sysdate").add();
            table.primaryKey("PK_PRODUCTSPEC").on(id).add();
        }
    },

    EISLOADPRFTYPEFORRTUTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceTypeLoadProfileTypeUsage> table = dataModel.addTable(name(), DeviceTypeLoadProfileTypeUsage.class);
            table.map(DeviceTypeLoadProfileTypeUsage.class);
            Column loadProfileType = table.column("LOADPROFILETYPEID").number().notNull().add();
            Column deviceType = table.column("RTUTYPEID").number().notNull().add();
            table.foreignKey("FK_RTUTYPEID_LPT_RTUTYPE_JOIN").on(deviceType).references(EISSYSRTUTYPE.name()).map("deviceType").reverseMap("loadProfileTypes").composition().add();
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
            table.foreignKey("FK_RTUTPID_REGMAP_RTUTYPE_JOIN").on(deviceType).references(EISSYSRTUTYPE.name()).map("deviceType").reverseMap("registerMappings").composition().add();
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
            table.foreignKey("FK_RTUTYPEID_LBT_RTUTYPE_JOIN").on(deviceType).references(EISSYSRTUTYPE.name()).map("deviceType").add();
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
            table.foreignKey("FK_REGMAPLPT_LOADPROFILETYPEID").on(loadProfileType).references(EISLOADPROFILETYPE.name()).map("loadProfileType").reverseMap("registerMapping").composition().add();
            table.foreignKey("FK_REGMAPLPT_REGMAPPINGID").on(registerMapping).references(EISRTUREGISTERMAPPING.name()).map("registerMapping").add();
        }
    },

    EISRTUREGISTERSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterSpec> table = dataModel.addTable(name(), RegisterSpec.class);
            table.map(RegisterSpecImpl.class);
            Column id = table.addAutoIdColumn();
            Column registerMapping = table.column("REGISTERMAPPINGID").number().notNull().add();
            table.column("NUMBEROFDIGITS").number().conversion(ColumnConversion.NUMBER2INT).notNull().map("numberOfDigits").add();
            table.column("MOD_DATE").type("DATE").notNull().map("modificationDate").add();
            table.column("DEVICEOBISCODE").varChar(80).notNull().map("overruledObisCodeString").add();
            table.column("NUMBEROFFRACTIONDIGITS").number().conversion(ColumnConversion.NUMBER2INT).map("numberOfFractionDigits").add();
            table.column("OVERFLOWVALUE").number().map("overflow").add();
            Column deviceConfiguration = table.column("DEVICECONFIGID").number().add();
            table.column("MULTIPLIER").number().map("multiplier").add();
            table.column("MULTIPLIERMODE").number().conversion(ColumnConversion.NUMBER2ENUM).notNull().map("multiplierMode").add();
            Column channelSpec = table.column("CHANNELSPECID").number().add();
            table.primaryKey("PK_RTUREGISTERSPEC").on(id).add();
            table.foreignKey("FK_EISRTUREGSPEC_REGMAP").on(registerMapping).references(EISRTUREGISTERMAPPING.name()).map("registerMapping").add();
            table.foreignKey("FK_EISRTUREGSPEC_DEVCFG").on(deviceConfiguration).references(EISDEVICECONFIG.name()).map("deviceConfig").add(); // TODO .reverseMap("registerMapping").composition().add();
            table.foreignKey("FK_REGSPEC_CHANNELSPEC").on(channelSpec).references(EISCHANNELSPEC.name()).map("linkedChannelSpec").add();
        }
    },
    ;

    abstract void addTo(DataModel component);

}