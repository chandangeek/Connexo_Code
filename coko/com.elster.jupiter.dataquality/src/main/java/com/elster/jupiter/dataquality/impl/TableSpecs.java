/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.RecurrentTask;

import java.util.HashMap;
import java.util.Map;

import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    DQK_DATAQUALITYKPI {
        @Override
        void addTo(DataModel dataModel) {
            Table<DataQualityKpi> table = dataModel.addTable(name(), DataQualityKpi.class);
            table.since(version(10, 3));
            table.setJournalTableName(name() + "JRNL");

            Map<String, Class<? extends DataQualityKpi>> implementers = new HashMap<>();
            implementers.put("EDDQ", DeviceDataQualityKpiImpl.class);
            implementers.put("UPDQ", UsagePointDataQualityKpiImpl.class);
            table.map(implementers);

            Column id = table.addAutoIdColumn();
            table.addAuditColumns();

            table.column("DISCRIMINATOR").varChar(4).notNull().map(Column.TYPEFIELDNAME).add(); // discriminator column
            Column endDeviceGroup = table.column("ENDDEVICEGROUP").number().conversion(ColumnConversion.NUMBER2LONG).add();
            Column usagePointGroup = table.column("USAGEPOINTGROUP").number().conversion(ColumnConversion.NUMBER2LONG).add();
            Column metrologyPurpose = table.column("METROLOGYPURPOSE").number().conversion(ColumnConversion.NUMBER2LONG).add();
            Column dataQualityKpiTask = table.column("DATAQUALITYKPITASK").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("OBSOLETETIME").number().map(DataQualityKpiImpl.Fields.OBSOLETE_TIME.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();

            table.primaryKey("PK_DQK_DATA_QUALITY_KPI").on(id).add();
            table.foreignKey("FK_DQK_ENDDEVICEGROUP")
                    .on(endDeviceGroup)
                    .references(EndDeviceGroup.class)
                    .map(DeviceDataQualityKpiImpl.Fields.ENDDEVICE_GROUP.fieldName())
                    .add();
            table.foreignKey("FK_DQK_USAGEPOINTGROUP").
                    on(usagePointGroup).
                    references(UsagePointGroup.class).
                    map(UsagePointDataQualityKpiImpl.Fields.USAGEPOINT_GROUP.fieldName()).
                    add();
            table.foreignKey("FK_DQK_METROLOGYPURPOSE")
                    .on(metrologyPurpose)
                    .references(MetrologyPurpose.class)
                    .map(UsagePointDataQualityKpiImpl.Fields.METROLOGY_PURPOSE.fieldName())
                    .add();
            table.foreignKey("FK_DQK_RECURRENTTASK")
                    .on(dataQualityKpiTask)
                    .references(RecurrentTask.class)
                    .map(DataQualityKpiImpl.Fields.DATA_QUALITY_KPI_TASK.fieldName())
                    .add();
        }
    },

    DQK_DATAQUALITYKPIMEMBER {
        @Override
        void addTo(DataModel dataModel) {
            Table<DataQualityKpiMember> table = dataModel.addTable(name(), DataQualityKpiMember.class);
            table.map(DataQualityKpiMemberImpl.class);
            table.since(version(10, 3));

            Column dataValidationKpiColumn = table.column("DATAQUALITYKPI").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column childKpiColumn = table.column("CHILDKPI").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.primaryKey("PK_DQK_DATAQUALITYKPIMEMBER").on(dataValidationKpiColumn, childKpiColumn).add();
            table.foreignKey("FK_DQK_KPIMEMBER_KPI")
                    .references(DQK_DATAQUALITYKPI.name())
                    .on(dataValidationKpiColumn)
                    .onDelete(CASCADE)
                    .map(DataQualityKpiMemberImpl.Fields.DATA_QUALITY_KPI.fieldName())
                    .reverseMap(DataQualityKpiImpl.Fields.KPI_MEMBERS.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_DQK_KPIMEMBER_CHILDKPI")
                    .references(Kpi.class)
                    .on(childKpiColumn)
                    .onDelete(CASCADE)
                    .map(DataQualityKpiMemberImpl.Fields.CHILD_KPI.fieldName())
                    .composition()
                    .add();
        }
    };

    abstract void addTo(DataModel component);
}
