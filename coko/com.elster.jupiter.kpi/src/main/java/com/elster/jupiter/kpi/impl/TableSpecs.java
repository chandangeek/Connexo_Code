/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;

enum TableSpecs {
    KPI_KPI {
        @Override
        void addTo(DataModel dataModel) {
            Table<Kpi> table = dataModel.addTable(name(), Kpi.class);
            table.map(KpiImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar(Table.NAME_LENGTH).map("name").add();
            table.addAuditColumns().forEach(c -> c.since(Version.version(10, 2)));
            table.primaryKey("KPI_PK_KPI").on(idColumn).add();
            table.unique("KPI_U_NAME").on(nameColumn).add();
        }
    },
    KPI_KPIMEMBER {
        @Override
        void addTo(DataModel dataModel) {
            Table<IKpiMember> table = dataModel.addTable(name(), IKpiMember.class);
            table.map(KpiMemberImpl.class);
            table.setJournalTableName("KPI_KPIMEMBERJRNL");
            Column kpiColumn = table.column("KPI").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column positionColumn = table.column("POSITION").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("position").add();
            table.column("NAME").varChar(Table.NAME_LENGTH).map("name").add();
            table.column("DYNAMIC").bool().map("dynamic").add();
            table.column("TARGET").number().map("targetValue").add();
            table.column("MINIMUM").bool().map("targetIsMinimum").add();
            Column timeseriesColumn = table.column("TIMESERIES").number().add();
            table.addAuditColumns().forEach(c -> c.since(Version.version(10, 2)));
            table.primaryKey("KPI_PK_KPIMEMBER").on(kpiColumn, positionColumn).add();
            table.foreignKey("KPI_FK_KPI_MEMBER").on(kpiColumn).references(KPI_KPI.name()).composition().reverseMap("members").reverseMapOrder("position").map("kpi").add();
            table.foreignKey("KPI_FK_MEMBER_TIMESERIES").on(timeseriesColumn).references(TimeSeries.class).map("timeSeries").add();
        }
    };

    abstract void addTo(DataModel dataModel);
}
