package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.TaskService;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

public enum TableSpecs {
    MDCCOMSCHEDULE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComSchedule> table = dataModel.addTable(name(), ComSchedule.class);
            table.map(ComScheduleImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(256)").map(ComScheduleImpl.Fields.NAME.fieldName()).add();
            table.column("MOD_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map(ComScheduleImpl.Fields.MOD_DATE.fieldName()).insert("sysdate").update("sysdate").add();
            Column nextExecutionSpec = table.column("NEXTEXECUTIONSPEC").number().conversion(NUMBER2LONG).add(); // DO NOT MAP

            table.foreignKey("FK_NEXTEXECUTIONSPEC").on(nextExecutionSpec).references(MDCNEXTEXECUTIONSPEC.name()).map(ComScheduleImpl.Fields.NEXT_EXECUTION_SPEC.fieldName()).add();
            table.primaryKey("PK_COMSCHULE").on(idColumn).add();
        }
    },
    MDCCOMTASKINCOMSCHEDULE {
            @Override
            void addTo(DataModel dataModel) {
                Table<ComTaskInComSchedule> table = dataModel.addTable(name(), ComTaskInComSchedule.class);
                table.map(ComTaskInComScheduleImpl.class);
                Column comTaskId = table.column("COMTASK").number().conversion(NUMBER2LONG).add(); // DO NOT MAP
                Column comScheduleId = table.column("COMSCHEDULE").number().conversion(NUMBER2LONG).add(); // DO NOT MAP

                table.foreignKey("FK_COMSCHEDULE").
                        on(comScheduleId).references(MDCCOMSCHEDULE.name()).
                        map(ComTaskInComScheduleImpl.Fields.COM_SCHEDULE_REFERENCE.fieldName()).
                        reverseMap(ComScheduleImpl.Fields.COM_TASK_IN_COM_SCHEDULE.fieldName()).
                        composition().
                        add();
                table.foreignKey("FK_COMTASK").on(comTaskId).references(TaskService.COMPONENT_NAME, "MDCCOMTASK").map(ComTaskInComScheduleImpl.Fields.COM_TASK_REFERENCE.fieldName()).add();
                table.primaryKey("PK_REGISTERGROUPUSAGE").on(comTaskId, comScheduleId).add();
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
    };



    abstract void addTo(DataModel component);

}
