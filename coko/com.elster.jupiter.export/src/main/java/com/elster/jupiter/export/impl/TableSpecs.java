/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.EndDeviceEventTypeFilter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.RelativePeriod;

import com.google.common.collect.ImmutableMap;

import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

enum TableSpecs {

    DES_DATAEXPORTTASK(IExportTask.class) {
        @Override
        void describeTable(Table table) {
            table.map(ExportTaskImpl.class);
            table.setJournalTableName("DES_DATAEXPORTTASKJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("DATAFORMATTER").varChar(NAME_LENGTH).notNull().map("dataFormatter").add();
            table.column("DATASELECTOR").varChar(NAME_LENGTH).notNull().map("dataSelector").add();
            Column recurrentTaskId = table.column("RECURRENTTASK").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").notAudited().add();
            table.addAuditColumns();

            table.foreignKey("DES_FK_RTET_RECURRENTTASK")
                    .on(recurrentTaskId)
                    .references(RecurrentTask.class)
                    .map("recurrentTask")
                    .add();
            table.primaryKey("DES_PK_DATAEXPORTTASK").on(idColumn).add();
        }
    },
    DES_RTDATASELECTOR(DataSelectorConfig.class) {
        @Override
        void describeTable(Table table) {
            table.map(
                    ImmutableMap.of(
                            MeterReadingSelectorConfigImpl.IMPLEMENTOR_NAME, MeterReadingSelectorConfigImpl.class,
                            UsagePointReadingSelectorConfigImpl.IMPLEMENTOR_NAME, UsagePointReadingSelectorConfigImpl.class,
                            EventSelectorConfigImpl.IMPLEMENTOR_NAME, EventSelectorConfigImpl.class
                    )
            );
            table.setJournalTableName("DES_RTDATASELECTORJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("TYPE").varChar(NAME_LENGTH).map(Column.TYPEFIELDNAME).since(version(10, 3)).add();// discriminator column
            Column taskColumn = table.column("EXPORTTASK").number().notNull().add();
            Column exportPeriod = table.column("EXPORT_PERIOD").number().notNull().add();
            Column updatePeriod = table.column("UPDATE_PERIOD").number().add();
            Column updateWindow = table.column("UPDATE_WINDOW").number().add();
            Column endDeviceGroupId_10_2 = table.column("ENDDEVICEGROUP").number().notNull().conversion(ColumnConversion.NUMBER2LONG).upTo(version(10, 3)).add();
            Column endDeviceGroup = table.column("ENDDEVICEGROUP").number().conversion(ColumnConversion.NUMBER2LONG).since(version(10, 3)).previously(endDeviceGroupId_10_2).add();
            Column usagePointGroup = table.column("USAGEPOINTGROUP").number().conversion(ColumnConversion.NUMBER2LONG).since(version(10, 3)).add();
            Column exportUpdate_10_2 =
                    table.column("EXPORT_UPDATE").bool().map("exportUpdate").upTo(version(10, 3)).add();
            table.column("EXPORT_UPDATE").bool().map("exportUpdate").bool().notNull(false).since(version(10, 3)).previously(exportUpdate_10_2).add();
            Column exportComplete_10_2 =
                    table.column("EXPORT_COMPLETE").bool().map("exportOnlyIfComplete").upTo(version(10, 3)).add();
            table.column("EXPORT_COMPLETE").bool().notNull(false).map("exportOnlyIfComplete").since(version(10, 3)).previously(exportComplete_10_2).add();
            table.column("EXPORT_CONTINUOUS_DATA").bool().map("exportContinuousData").add();
            table.column("VALIDATED_DATA_OPTION").number().conversion(ColumnConversion.NUMBER2ENUM).map("validatedDataOption").add();

            table.addAuditColumns();

            table.unique("DES_UK_RTDS_TASK").on(taskColumn).add();
            table.foreignKey("DES_FK_RTDS_TASK")
                    .on(taskColumn)
                    .references("DES_DATAEXPORTTASK")
                    .map("exportTask")
                    .reverseMap("dataSelectorConfig")
                    .add();
            table.foreignKey("DES_FK_RTDS_EXPORTPERIOD")
                    .on(exportPeriod)
                    .references(RelativePeriod.class)
                    .map("exportPeriod")
                    .add();
            table.foreignKey("DES_FK_RTDS_UPDATEPERIOD")
                    .on(updatePeriod)
                    .references(RelativePeriod.class)
                    .map("updatePeriod")
                    .add();
            table.foreignKey("DES_FK_RTDS_UPDATEWINDOW")
                    .on(updateWindow)
                    .references(RelativePeriod.class)
                    .map("updateWindow")
                    .add();
            table.foreignKey("DES_FK_RTDS_ENDDEVICEFROUP")
                    .on(endDeviceGroup)
                    .references(EndDeviceGroup.class)
                    .map("endDeviceGroup")
                    .add();
            table.foreignKey("DES_FK_RTDS_USAGEPOIINTGROUP")
                    .on(usagePointGroup)
                    .references(UsagePointGroup.class)
                    .map("usagePointGroup")
                    .since(version(10, 3))
                    .add();
            table.primaryKey("DES_PK_RTDATASELECTOR").on(idColumn).add();
        }
    },
    DES_READINGTYPE_IN_SELECTOR(ReadingTypeInDataSelector.class) {
        @Override
        void describeTable(Table table) {
            table.map(ReadingTypeInDataSelector.class);
            table.setJournalTableName("DES_READINGTYPE_IN_TASKJRNL");
            Column dataSelector = table.column("RTDATASELECTOR").number().notNull().add();
            Column readingType = table.column("READINGTYPE").varChar(Table.NAME_LENGTH).notNull().map("readingTypeMRID").add();
            table.addAuditColumns();

            table.primaryKey("DES_PK_RT_RTDATASELECTOR").on(dataSelector, readingType).add();
            table.foreignKey("DES_FK_RTINDS_RTSELECTOR").on(dataSelector).references(DES_RTDATASELECTOR.name())
                    .map("readingTypeDataSelector").reverseMap("readingTypes").composition().add();
            table.foreignKey("DES_FK_RTINET_READINGTYPE").on(readingType).references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE").onDelete(DeleteRule.RESTRICT)
                    .map("readingType").add();
        }
    },
    DES_EVENTFILTER(EndDeviceEventTypeFilter.class) {
        @Override
        void describeTable(Table table) {
            table.map(FieldBasedEndDeviceEventTypeFilter.class);
            table.setJournalTableName("DES_EVENTFILTERJRNL");
            Column dataSelector = table.column("RTDATASELECTOR").number().notNull().add();
            Column code = table.column("CODE").varChar(15).notNull().map("code").add();

            table.addAuditColumns();
            table.primaryKey("DES_PK_EVENTFILTER").on(dataSelector, code).add();
            table.foreignKey("DES_FK_EV_IN_SELECTOR").on(dataSelector)
                    .references(DES_RTDATASELECTOR.name())
                    .map("dataSelector")
                    .reverseMap("eventTypeFilters")
                    .composition()
                    .add();
        }
    },
    DES_PROPERTY_IN_TASK(DataExportProperty.class) {
        @Override
        void describeTable(Table table) {
            table.map(DataExportPropertyImpl.class);
            table.setJournalTableName("DES_PROPERTY_IN_TASKJRNL");
            Column taskColumn = table.column("TASK").number().notNull().add();
            Column nameColumn = table.column("NAME").varChar(Table.NAME_LENGTH).notNull().map("name").add();
            table.column("VALUE").varChar(Table.DESCRIPTION_LENGTH).map("stringValue").add();
            table.addAuditColumns();

            table.primaryKey("DES_PK_RTETPROPERTY").on(taskColumn, nameColumn).add();
            table.foreignKey("DES_FK_PRPINET_RTEXPORTTASK").on(taskColumn).references(DES_DATAEXPORTTASK.name())
                    .map("task").reverseMap("properties").composition().add();
        }
    },
    DES_OCCURRENCE(DataExportOccurrence.class) {
        @Override
        void describeTable(Table table) {
            table.map(DataExportOccurrenceImpl.class);
            Column taskOccurrence = table.column("TASKOCC").number().notNull().add();
            Column task = table.column("RTEXPORTTASK").number().notNull().add();
            table.addIntervalColumns("exportedDataInterval");
            table.column("INTERVALENDPTBEHAVIOUR").number().conversion(ColumnConversion.NUMBER2ENUM).map("exportedDataBoundaryType").add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map("status").add();
            table.column("MESSAGE").varChar(Table.DESCRIPTION_LENGTH).map("failureReason").add();
            table.column("SUMMARY").type("CLOB").conversion(CLOB2STRING).map("summary").add();

            table.primaryKey("DES_PK_EXPOCC").on(taskOccurrence).add();
            table.foreignKey("DES_FK_EXPOCC_TSKOCC").on(taskOccurrence).references(TaskService.COMPONENTNAME, "TSK_TASK_OCCURRENCE")
                    .map("taskOccurrence").refPartition().add();
            table.foreignKey("DES_FK_EXPOCC_RTEXPORTTASK").on(task).references(DES_DATAEXPORTTASK.name())
                    .map("readingTask").add();
        }
    },
    DES_RTDATAEXPORTITEM(IReadingTypeDataExportItem.class) {
        @Override
        void describeTable(Table table) {
            table.map(ReadingTypeDataExportItemImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.addRefAnyColumns("READINGCONT", true, "readingContainer");
            Column selector = table.column("SELECTOR").number().notNull().add();
            table.column("LASTRUN").number().conversion(ColumnConversion.NUMBER2INSTANT).map("lastRun").add();
            table.column("LASTEXPORTED").number().conversion(ColumnConversion.NUMBER2INSTANT).map("lastExportedDate").add();
            table.column("READINGTYPEMRID").varChar(NAME_LENGTH).notNull().map("readingTypeMRId").add();
            table.column("ACTIVE").bool().notNull().map("active").add();

            table.primaryKey("DES_PK_RTEXPITEM").on(idColumn).add();
            table.foreignKey("DES_FK_RTEXPITEM_SELECTOR").on(selector).references(DES_RTDATASELECTOR.name())
                    .map("selector").reverseMap("exportItems").composition().add();
        }
    },
    DES_DIR4APPSERVER(DirectoryForAppServer.class) {
        @Override
        void describeTable(Table table) {
            table.map(DirectoryForAppServer.class);

            Column idColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().add();
            table.column("PATH").varChar(DESCRIPTION_LENGTH).map("pathString").add();

            table.primaryKey("DES_PK_DIR4APPSERVER").on(idColumn).add();
            table.foreignKey("DES_FK_DIR4APPSERVER_APS").on(idColumn).references("APS", "APS_APPSERVER").map("appServer").add();
        }
    },
    DES_DESTINATION(IDataExportDestination.class) {
        @Override
        void describeTable(Table table) {
            table.map(AbstractDataExportDestination.IMPLEMENTERS);
            table.setJournalTableName("DES_DESTINATIONJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(5)");

            Column taskColumn = table.column("TASK").number().notNull().add();
            table.column("FILENAME").varChar(Table.NAME_LENGTH).map("fileName").add();
            table.column("FILEEXTENSION").varChar(Table.NAME_LENGTH).map("fileExtension").add();
            table.column("FILELOCATION").varChar(Table.DESCRIPTION_LENGTH).map("fileLocation").add();

            table.column("RECIPIENTS").varChar(Table.DESCRIPTION_LENGTH).map("recipients").add();
            table.column("SUBJECT").varChar(Table.NAME_LENGTH).map("subject").add();
            table.column("ATTACHMENTNAME").varChar(Table.NAME_LENGTH).map("attachmentName").add();
            table.column("ATTACHMENTEXTENSION").varChar(Table.NAME_LENGTH).map("attachmentExtension").add();

            table.column("SERVER").varChar(Table.DESCRIPTION_LENGTH).map("server").add();
            table.column("USERID").varChar(Table.DESCRIPTION_LENGTH).map("user").add();
            table.column("PASSWORD").varChar(Table.DESCRIPTION_LENGTH).map("password").add();
            table.column("PORT").number().conversion(NUMBER2INT).map("port").add();

            table.addAuditColumns();

            table.primaryKey("DES_PK_DESTINATION").on(idColumn).add();
            table.foreignKey("DES_DEST_TASK")
                    .on(taskColumn)
                    .references(DES_DATAEXPORTTASK.toString())
                    .map("task")
                    .reverseMap("destinations")
                    .composition()
                    .add();
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
