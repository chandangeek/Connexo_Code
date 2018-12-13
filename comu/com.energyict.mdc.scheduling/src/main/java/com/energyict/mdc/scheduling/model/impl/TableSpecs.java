/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import static com.elster.jupiter.orm.ColumnConversion.DATE2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    SCH_NEXTEXECUTIONSPEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<NextExecutionSpecs> table = dataModel.addTable(name(), NextExecutionSpecs.class);
            table.map(NextExecutionSpecsImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("FREQUENCYVALUE").number().conversion(ColumnConversion.NUMBER2INT).map(NextExecutionSpecsImpl.Fields.TEMPORAL_EXPRESSION.fieldName()+".every.count").add();
            table.column("FREQUENCYUNIT").number().conversion(ColumnConversion.NUMBER2INT).map(NextExecutionSpecsImpl.Fields.TEMPORAL_EXPRESSION.fieldName()+".every.timeUnitCode").add();
            table.column("OFFSETVALUE").number().conversion(ColumnConversion.NUMBER2INT).map(NextExecutionSpecsImpl.Fields.TEMPORAL_EXPRESSION.fieldName()+".offset.count").add();
            table.column("OFFSETUNIT").number().conversion(ColumnConversion.NUMBER2INT).map(NextExecutionSpecsImpl.Fields.TEMPORAL_EXPRESSION.fieldName()+".offset.timeUnitCode").add();
            table.setJournalTableName("SCH_NEXTEXECUTIONSPECJRNL").since(version(10, 2));
            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table.primaryKey("PK_SCH_NEXTEXECUTIONSPEC").on(id).add();
        }
    },
    SCH_COMSCHEDULE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComSchedule> table = dataModel.addTable(name(), ComSchedule.class);
            table.map(ComScheduleImpl.class);
            table.cache();
            Column idColumn = table.addAutoIdColumn();
            table.addAuditColumns();
            table.column("NAME").varChar().map(ComScheduleImpl.Fields.NAME.fieldName()).add();
            table.column("MRID").varChar().map(ComScheduleImpl.Fields.MRID.fieldName()).add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map(ComScheduleImpl.Fields.STATUS.fieldName()).add();
            table.column("STARTDATE").number().conversion(NUMBER2INSTANT).map(ComScheduleImpl.Fields.START_DATE.fieldName()).add();
            table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2INSTANT).map(ComScheduleImpl.Fields.OBSOLETE_DATE.fieldName()).add();
            Column nextExecutionSpec = table.column("NEXTEXECUTIONSPEC").number().conversion(NUMBER2LONG).add(); // DO NOT MAP

            table.primaryKey("PK_SCH_COMSCHEDULE").on(idColumn).add();
            table.foreignKey("FK_SCH_NEXTEXECUTIONSPEC")
                    .on(nextExecutionSpec)
                    .references(SCH_NEXTEXECUTIONSPEC.name())
                    .map(ComScheduleImpl.Fields.NEXT_EXECUTION_SPEC.fieldName())
                    .add();
        }
    },
    SCH_COMTASKINCOMSCHEDULE {
            @Override
            void addTo(DataModel dataModel) {
                Table<ComTaskInComSchedule> table = dataModel.addTable(name(), ComTaskInComSchedule.class);
                table.map(ComTaskInComScheduleImpl.class);
                Column comTaskId = table.column("COMTASK").number().conversion(NUMBER2LONG).notNull().add(); // DO NOT MAP
                Column comScheduleId = table.column("COMSCHEDULE").number().conversion(NUMBER2LONG).notNull().add(); // DO NOT MAP
                table.setJournalTableName("SCH_COMTASKINCOMSCHEDULEJRNL").since(version(10, 2));
                table.addAuditColumns();

                table.primaryKey("PK_SCH_COMTASKINCOMSCHDLE").on(comTaskId, comScheduleId).add();
                table.foreignKey("FK_SCH_COMTASKINCS_COMSCHEDULE")
                        .on(comScheduleId).references(SCH_COMSCHEDULE.name())
                        .map(ComTaskInComScheduleImpl.Fields.COM_SCHEDULE_REFERENCE.fieldName())
                        .reverseMap(ComScheduleImpl.Fields.COM_TASK_IN_COM_SCHEDULE.fieldName())
                        .composition()
                        .add();
                table.foreignKey("FK_SCH_COMTASKINCOMSCH_COMTASK")
                        .on(comTaskId)
                        .references(ComTask.class)
                        .map(ComTaskInComScheduleImpl.Fields.COM_TASK_REFERENCE.fieldName())
                        .add();
            }
        };

    abstract void addTo(DataModel component);

}