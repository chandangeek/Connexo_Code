package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

enum TableSpecs {

    DES_RTDATAEXPORTTASK(IReadingTypeDataExportTask.class) {
        @Override
        void describeTable(Table table) {
            table.map(ReadingTypeDataExportTaskImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("dataProcessor").varChar(NAME_LENGTH).notNull().map("dataProcessor").add();
            Column exportPeriod = table.column("EXPORT_PERIOD").number().notNull().add();
            Column updatePeriod = table.column("UPDATE_PERIOD").number().add();
            Column recurrentTaskId = table.column("RECURRENTTASK").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column endDeviceGroupId = table.column("ENDDEVICEGROUP").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("EXPORT_UPDATE").bool().map("exportUpdate").add();
            table.column("EXPORT_CONTINUOUS_DATA").bool().map("exportContinuousData").add();
            table.column("VALIDATED_DATA_OPTION").number().conversion(ColumnConversion.NUMBER2ENUM).map("validatedDataOption").add();

            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").add();

            table.foreignKey("DES_FK_RTET_EXPORTPERIOD")
                    .on(exportPeriod)
                    .references(TimeService.COMPONENT_NAME, "TME_RELATIVEPERIOD")
                    .map("exportPeriod")
                    .add();
            table.foreignKey("DES_FK_RTET_UPDATEPERIOD")
                    .on(updatePeriod)
                    .references(TimeService.COMPONENT_NAME, "TME_RELATIVEPERIOD")
                    .map("updatePeriod")
                    .add();
            table.foreignKey("DES_FK_RTET_RECURRENTTASK")
                    .on(recurrentTaskId)
                    .references(TaskService.COMPONENTNAME, "TSK_RECURRENT_TASK")
                    .map("recurrentTask")
                    .add();
            table.foreignKey("DES_FK_RTET_ENDDEVICEFROUP")
                    .on(endDeviceGroupId)
                    .references(MeteringGroupsService.COMPONENTNAME, "MTG_ED_GROUP")
                    .map("endDeviceGroup")
                    .add();
            table.primaryKey("DES_PK_RTDATAEXPORTTASK").on(idColumn).add();
        }

    },
    DES_READINGTYPE_IN_TASK(ReadingTypeInExportTask.class) {
        @Override
        void describeTable(Table table) {
            table.map(ReadingTypeInExportTask.class);
            Column exportTask = table.column("RTEXPORTTASK").number().notNull().add();
            Column readingType = table.column("READINGTYPE").varChar(Table.NAME_LENGTH).notNull().map("readingTypeMRID").add();

            table.primaryKey("DES_PK_RT_RTEXPORTTASK").on(exportTask, readingType).add();
            table.foreignKey("DES_FK_RTINET_RTEXPORTTASK").on(exportTask).references(DES_RTDATAEXPORTTASK.name()).onDelete(DeleteRule.CASCADE)
                    .map("readingTypeDataExportTask").reverseMap("readingTypes").composition().add();
            table.foreignKey("DES_FK_RTINET_READINGTYPE").on(readingType).references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE").onDelete(DeleteRule.RESTRICT)
                    .map("readingType").add();
        }
    },
    DES_PROPERTY_IN_TASK(DataExportProperty.class) {
        @Override
        void describeTable(Table table) {
            table.map(DataExportPropertyImpl.class);
            Column taskColumn = table.column("TASK").number().notNull().add();
            Column nameColumn = table.column("NAME").varChar(Table.NAME_LENGTH).notNull().map("name").add();
            table.column("VALUE").varChar(Table.DESCRIPTION_LENGTH).map("stringValue").add();

            table.primaryKey("DES_PK_RTETPROPERTY").on(taskColumn, nameColumn).add();
            table.foreignKey("DES_FK_PRPINET_RTEXPORTTASK").on(taskColumn).references(DES_RTDATAEXPORTTASK.name()).onDelete(DeleteRule.CASCADE)
                    .map("task").reverseMap("properties").composition().add();
        }
    },
    DES_OCCURRENCE(DataExportOccurrence.class) {
        @Override
        void describeTable(Table table) {
            table.map(DataExportOccurrenceImpl.class);
            Column taskOccurrence = table.column("TASKOCC").number().notNull().add();
            Column task = table.column("RTEXPORTTASK").number().notNull().add();
            table.column("STARTDATE").number().conversion(ColumnConversion.NUMBER2INSTANT).map("startDate").add();
            table.column("ENDDATE").number().conversion(ColumnConversion.NUMBER2INSTANT).map("endDate").add();
            table.addIntervalColumns("exportedDataInterval");
            table.column("INTERVALENDPTBEHAVIOUR").number().conversion(ColumnConversion.NUMBER2ENUM).map("exportedDataBoundaryType").add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map("status").add();
            table.column("MESSAGE").varChar(Table.SHORT_DESCRIPTION_LENGTH).map("failureReason").add();

            table.primaryKey("DES_PK_EXPOCC").on(taskOccurrence).add();
            table.foreignKey("DES_FK_EXPOCC_TSKOCC").on(taskOccurrence).references(TaskService.COMPONENTNAME, "TSK_TASK_OCCURRENCE").onDelete(DeleteRule.CASCADE)
                    .map("taskOccurrence").add();
            table.foreignKey("DES_FK_EXPOCC_RTEXPORTTASK").on(task).references(DES_RTDATAEXPORTTASK.name()).onDelete(DeleteRule.CASCADE)
                    .map("readingTask").add();

        }
    },
    DES_RTDATAEXPORTITEM(ReadingTypeDataExportItem.class) {
        @Override
        void describeTable(Table table) {
            table.map(ReadingTypeDataExportItemImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.addRefAnyColumns("READINGCONT", true, "readingContainer");
            Column task = table.column("TASK").number().notNull().add();
            table.column("LASTRUN").number().conversion(ColumnConversion.NUMBER2INSTANT).map("lastRun").add();
            table.column("LASTEXPORTED").number().conversion(ColumnConversion.NUMBER2INSTANT).map("lastExportedDate").add();
            table.column("READINGTYPEMRID").varChar(NAME_LENGTH).notNull().map("readingTypeMRId").add();
            table.column("ACTIVE").bool().notNull().map("active").add();

            table.primaryKey("DES_PK_RTEXPITEM").on(idColumn).add();
            table.foreignKey("DES_FK_RTEXPITEM_TASK").on(task).references(DES_RTDATAEXPORTTASK.name()).onDelete(DeleteRule.CASCADE)
                    .map("task").reverseMap("exportItems").composition().add();
        }
    };


    private final Class<?> api;

    TableSpecs(Class<?> api) {
        this.api = api;
    }

    public void addTo(DataModel component) {
        Table table = component.addTable(name(), api);
        describeTable(table);
    }

    abstract void describeTable(Table table);

}
