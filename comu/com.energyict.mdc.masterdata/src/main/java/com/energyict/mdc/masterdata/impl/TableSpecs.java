package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
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

    MDS_LOADPROFILETYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileType> table = dataModel.addTable(this.name(), LoadProfileType.class);
            table.map(LoadProfileTypeImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.column("OBISCODE").varChar(StringColumnLengthConstraints.DEFAULT_OBISCODE_LENGTH).notNull().map(LoadProfileTypeImpl.Fields.OBIS_CODE.fieldName()).add();
            table.column("INTERVALCOUNT").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.count").add();
            table.column("INTERVALUNIT").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("interval.timeUnitCode").add();
            table.unique("UK_MDS_LOADPROFILETYPE").on(name).add();
            table.primaryKey("PK_MDS_LOADPROFILETYPE").on(id).add();
        }
    },

    MDS_REGISTERGROUP {
        @Override
        public void addTo(DataModel dataModel) {
            Table<RegisterGroup> table = dataModel.addTable(this.name(), RegisterGroup.class);
            table.map(RegisterGroupImpl.class);
            table.cache();
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.unique("UK_MDS_REGISTERGROUP").on(name).add();
            table.primaryKey("PK_MDS_REGISTERGROUP").on(id).add();
        }
    },

    MDS_MEASUREMENTTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<MeasurementType> table = dataModel.addTable(this.name(), MeasurementType.class);
            table.map(MeasurementTypeImpl.IMPLEMENTERS);
            table.cache();
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            table.column("OBISCODE").varChar(StringColumnLengthConstraints.DEFAULT_OBISCODE_LENGTH).notNull().map(MeasurementTypeImpl.Fields.OBIS_CODE.fieldName()).add();
            Column readingType = table.column("READINGTYPE").varChar(Table.NAME_LENGTH).add();
            table.column("CUMULATIVE").number().conversion(NUMBER2BOOLEAN).notNull().map("cumulative").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.column("INTERVAL").number().conversion(ColumnConversion.NUMBER2INT).map("interval.count").add();
            table.column("INTERVALCODE").number().conversion(ColumnConversion.NUMBER2INT).map("interval.timeUnitCode").add();
            table.column("TEMPLATEREGISTER").number().conversion(ColumnConversion.NUMBER2LONG).map("templateRegisterId").add();
            table.foreignKey("FK_MDS_MEASTP_READINGTYPE").on(readingType).references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE").map("readingType").add();
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
            table.addAuditColumns();
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
                    composition().
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
            table.addAuditColumns();
            table.primaryKey("PK_CHTYPEINLOADPROFILETYPE").on(loadProfileType, channelType).add();
            table.foreignKey("FK_CHTPLPT_LOADPROFILETYPEID").on(loadProfileType).references(MDS_LOADPROFILETYPE.name()).map("loadProfileType").reverseMap(LoadProfileTypeImpl.Fields.REGISTER_TYPES.fieldName()).composition().add();
            table.foreignKey("FK_CHTPLPT_CHANTYPEID").on(channelType).references(MDS_MEASUREMENTTYPE.name()).map("channelType").add();
        }
    },

    MDS_LOGBOOKTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBookType> table = dataModel.addTable(this.name(), LogBookType.class);
            table.map(LogBookTypeImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            table.column("DESCRIPTION").varChar().map("description").add();
            table.column("OBISCODE").varChar(StringColumnLengthConstraints.DEFAULT_OBISCODE_LENGTH).notNull().map(LogBookTypeImpl.Fields.OBIS_CODE.fieldName()).add();
            table.unique("UK_MDS_LOGBOOKTYPE").on(name).add();
            table.primaryKey("PK_MDS_LOGBOOKTYPE").on(id).add();
        }
    };

    abstract void addTo(DataModel component);

}