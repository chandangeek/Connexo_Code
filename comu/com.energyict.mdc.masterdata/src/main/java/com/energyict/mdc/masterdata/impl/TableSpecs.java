package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeRegisterMappingUsage;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:39)
 */
public enum TableSpecs {

    EISPHENOMENON {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Phenomenon> table = dataModel.addTable(name(), Phenomenon.class);
            table.map(PhenomenonImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar(80).notNull().map(PhenomenonImpl.Fields.NAME.fieldName()).add();
            Column unit = table.column("UNIT").type("CHAR(7)").notNull().map(PhenomenonImpl.Fields.UNIT.fieldName()).add();
            table.column("MEASUREMENTCODE").varChar(80).map(PhenomenonImpl.Fields.MEASUREMENT_CODE.fieldName()).add();
            table.column("EDICODE").varChar(80).map(PhenomenonImpl.Fields.EDI_CODE.fieldName()).add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map(PhenomenonImpl.Fields.MODIFICATION_DATE.fieldName()).insert("sysdate").update("sysdate").add();
            table.primaryKey("PK_PHENOMENON").on(id).add();
            table.unique("UK_EISPHENOMENON").on(unit).add(); // Done so phenomenon can be identified solely by unit, cfr gna
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
            table.column("OBISCODE").varChar(80).notNull().map(RegisterMappingImpl.Fields.OBIS_CODE.fieldName()).add();
            Column phenomenon = table.column("PHENOMENONID").number().conversion(ColumnConversion.NUMBER2INT).notNull().add();
            Column readingType = table.column("READINGTYPE").varChar(100).add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            table.column("CUMULATIVE").number().conversion(NUMBER2BOOLEAN).notNull().map("cumulative").add();
            Column registerGroup = table.column("REGISTERGROUPID").number().add();
            table.column("DESCRIPTION").varChar(255).map("description").add();
            table.column("TIMEOFUSE").number().map("timeOfUse").conversion(ColumnConversion.NUMBER2INT).add();
            table.foreignKey("FK_EISREGMAP_REGGROUP").on(registerGroup).references(EISRTUREGISTERGROUP.name()).map("registerGroup").add();
            table.foreignKey("FK_EISREGMAP_PHENOMENON").on(phenomenon).references(EISPHENOMENON.name()).map("phenomenon").add();
            table.foreignKey("FK_EISREGMAP_READINGTYPE").on(readingType).references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE").map("readingType").add();
            table.unique("UK_RTUREGMAPPINGNAME").on(name).add();
            table.unique("UK_RTUREGMREADINGTYPE").on(readingType).add();
            table.primaryKey("PK_RTUREGISTERMAPPING").on(id).add();
        }
    },

    EISREGMAPPINGINLOADPROFILETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileTypeRegisterMappingUsage> table = dataModel.addTable(name(), LoadProfileTypeRegisterMappingUsage.class);
            table.map(LoadProfileTypeRegisterMappingUsageImpl.class);
            Column loadProfileType = table.column("LOADPROFILETYPEID").number().notNull().add();
            Column registerMapping = table.column("REGMAPPINGID").number().notNull().add();
            table.primaryKey("PK_REGMAPPINGINLOADPROFILETYPE").on(loadProfileType, registerMapping).add();
            table.foreignKey("FK_REGMAPLPT_LOADPROFILETYPEID").on(loadProfileType).references(EISLOADPROFILETYPE.name()).map("loadProfileType").reverseMap("registerMappingUsages").composition().add();
            table.foreignKey("FK_REGMAPLPT_REGMAPPINGID").on(registerMapping).references(EISRTUREGISTERMAPPING.name()).map("registerMapping").add();
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
    };

    abstract void addTo(DataModel component);

}