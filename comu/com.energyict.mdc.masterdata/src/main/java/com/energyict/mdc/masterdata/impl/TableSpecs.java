package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeChannelTypeUsage;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterGroup;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:39)
 */
public enum TableSpecs {

    MDS_PHENOMENON {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Phenomenon> table = dataModel.addTable(name(), Phenomenon.class);
            table.map(PhenomenonImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar().notNull().map(PhenomenonImpl.Fields.NAME.fieldName()).add();
            Column unit = table.column("UNIT").varChar(StringColumnLengthConstraints.PHENOMENON_UNIT).notNull().map(PhenomenonImpl.Fields.UNIT.fieldName()).add();
            table.column("MEASUREMENTCODE").varChar().map(PhenomenonImpl.Fields.MEASUREMENT_CODE.fieldName()).add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map(PhenomenonImpl.Fields.MODIFICATION_DATE.fieldName()).insert("sysdate").update("sysdate").add();
            table.primaryKey("PK_MDS_PHENOMENON").on(id).add();
            table.unique("UK_MDS_PHENOMENON").on(unit).add(); // Done so phenomenon can be identified solely by unit, cfr gna
        }
    },

    MDS_LOADPROFILETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileType> table = dataModel.addTable(this.name(), LoadProfileType.class);
            table.map(LoadProfileTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.column("OBISCODE").varChar(StringColumnLengthConstraints.DEFAULT_OBISCODE_LENGTH).notNull().map(LoadProfileTypeImpl.Fields.OBIS_CODE.fieldName()).add();
            table.column("INTERVALCOUNT").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.count").add();
            table.column("INTERVALUNIT").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.timeUnitCode").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            table.unique("UK_MDS_LOADPROFILETYPE").on(name).add();
            table.primaryKey("PK_MDS_LOADPROFILETYPE").on(id).add();
        }
    },

    MDS_REGISTERGROUP {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterGroup> table = dataModel.addTable(this.name(), RegisterGroup.class);
            table.map(RegisterGroupImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            table.unique("UK_MDS_REGISTERGROUP").on(name).add();
            table.primaryKey("PK_MDS_REGISTERGROUP").on(id).add();
        }
    },

    MDS_MEASUREMENTTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<MeasurementType> table = dataModel.addTable(this.name(), MeasurementType.class);
            table.map(MeasurementTypeImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            table.column("OBISCODE").varChar(StringColumnLengthConstraints.DEFAULT_OBISCODE_LENGTH).notNull().map(MeasurementTypeImpl.Fields.OBIS_CODE.fieldName()).add();
            Column phenomenon = table.column("PHENOMENONID").number().conversion(ColumnConversion.NUMBER2INT).notNull().add();
            Column readingType = table.column("READINGTYPE").varChar(Table.NAME_LENGTH).add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            table.column("CUMULATIVE").number().conversion(NUMBER2BOOLEAN).notNull().map("cumulative").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.column("TIMEOFUSE").number().map("timeOfUse").conversion(ColumnConversion.NUMBER2INT).add();
            table.column("INTERVAL").number().conversion(ColumnConversion.NUMBER2INT).map("interval.count").add();
            table.column("INTERVALCODE").number().conversion(ColumnConversion.NUMBER2INT).map("interval.timeUnitCode").add();
            table.column("TEMPLATEREGISTER").number().conversion(ColumnConversion.NUMBER2INT).map("templateRegisterId").add();
            table.foreignKey("FK_MDS_MEASTP_PHENOMENON").on(phenomenon).references(MDS_PHENOMENON.name()).map("phenomenon").add();
            table.foreignKey("FK_MDS_MEASTP_READINGTYPE").on(readingType).references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE").map("readingType").add();
            table.unique("UK_MDS_MEASTYPENAME").on(name).add();
            table.unique("UK_MDS_MTREADINGTYPE").on(readingType).add();
            table.primaryKey("PK_MDS_MEASUREMENTTYPE").on(id).add();
        }
    },

    MDS_REGISTERTYPEINGROUP {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterTypeInGroup> table = dataModel.addTable(this.name(), RegisterTypeInGroup.class);
            table.map(RegisterTypeInGroup.class);
            Column registerType = table.column("REGISTERTYPEID").number().notNull().add();
            Column registerGroup = table.column("REGISTERGROUPID").number().notNull().add();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.primaryKey("USR_PK_REGTYPEINGROUP").on(registerType , registerGroup).add();
            table.foreignKey("FK_REGTYPEINGROUP2TYPE").
                    on(registerType).
                    references(MDS_MEASUREMENTTYPE.name()).
                    onDelete(CASCADE).map("registerType").
                    add();
            table.foreignKey("FK_REGTYPEINGROUP2GROUP").
                    on(registerGroup).
                    references(MDS_REGISTERGROUP.name()).
                    onDelete(CASCADE).
                    map("registerGroup").
                    reverseMap("registerTypeInGroups").
                    add();
        }
    },

    MDS_CHNTYPEINLOADPROFILETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileTypeChannelTypeUsage> table = dataModel.addTable(name(), LoadProfileTypeChannelTypeUsage.class);
            table.map(LoadProfileTypeChannelTypeUsageImpl.class);
            Column loadProfileType = table.column("LOADPROFILETYPEID").number().notNull().add();
            Column channelType = table.column("CHTYPEID").number().notNull().add();
            table.primaryKey("PK_CHTYPEINLOADPROFILETYPE").on(loadProfileType, channelType).add();
            table.foreignKey("FK_CHTPLPT_LOADPROFILETYPEID").on(loadProfileType).references(MDS_LOADPROFILETYPE.name()).map("loadProfileType").reverseMap("channelTypeUsages").composition().add();
            table.foreignKey("FK_CHTPLPT_CHANTYPEID").on(channelType).references(MDS_MEASUREMENTTYPE.name()).map("channelType").add();
        }
    },

    MDS_LOGBOOKTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBookType> table = dataModel.addTable(this.name(), LogBookType.class);
            table.map(LogBookTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.column("OBISCODE").varChar(StringColumnLengthConstraints.DEFAULT_OBISCODE_LENGTH).notNull().map(LogBookTypeImpl.Fields.OBIS_CODE.fieldName()).add();
            table.unique("UK_MDS_LOGBOOKTYPE").on(name).add();
            table.primaryKey("PK_MDS_LOGBOOKTYPE").on(id).add();
        }
    };

    abstract void addTo(DataModel component);

}