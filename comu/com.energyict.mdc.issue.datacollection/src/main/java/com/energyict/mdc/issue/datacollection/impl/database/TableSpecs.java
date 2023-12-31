/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.database;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.issue.datacollection.DataCollectionEventMetadata;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.records.DataCollectionEventMetadataImpl;
import com.energyict.mdc.issue.datacollection.impl.records.HistoricalIssueDataCollectionImpl;
import com.energyict.mdc.issue.datacollection.impl.records.IssueDataCollectionImpl;
import com.energyict.mdc.issue.datacollection.impl.records.OpenIssueDataCollectionImpl;

import java.time.Instant;
import java.util.Arrays;
import java.util.ListIterator;

import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_CREATEDATETIME;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.DATACOLLECTION_COLUMN_DEVICE;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.DATACOLLECTION_COLUMN_EVENTTYPE;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.DATACOLLECTION_EVENT_DESCRIPTION_FK_TO_DEVICE;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.DATACOLLECTION_EVENT_DESCRIPTION_FK_TO_EVENTTYPE;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.DATACOLLECTION_EVENT_DESCRIPTION_PK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_BASE_ISSUE;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_COMMUNICATION_TASK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_COM_SESSION;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_CONNECTION_TASK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_DEVICE_MRID;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_FIRST_TRY;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ID;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_FK_TO_COM_SESSION;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_FK_TO_COM_TASK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_FK_TO_CONNECTION_TASK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_FK_TO_ISSUE;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_HISTORY_FK_TO_COM_SESSION;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_HISTORY_FK_TO_COM_TASK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_HISTORY_FK_TO_CONNECTION_TASK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_HISTORY_FK_TO_ISSUE;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_HISTORY_PK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_OPEN_FK_TO_COM_SESSION;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_OPEN_FK_TO_COM_TASK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_OPEN_FK_TO_CONNECTION_TASK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_OPEN_FK_TO_ISSUE;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_OPEN_PK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_ISSUE_PK;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_LAST_GATEWAY_MRID;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_LAST_TRY;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.IDC_NUMBER_TRIES;

public enum TableSpecs {
    IDC_ISSUE_HISTORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalIssueDataCollection> table = dataModel.addTable(name(), HistoricalIssueDataCollection.class);
            table.map(HistoricalIssueDataCollectionImpl.class);
            table.setJournalTableName("IDC_ISSUE_HISTORY_JRNL").upTo(version(10, 2));
            Column idColumn = table.column(IDC_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ISSUE_HISTORY", IDC_ISSUE_HISTORY_PK,
                    // Foreign keys
                    IDC_ISSUE_HISTORY_FK_TO_ISSUE,
                    IDC_ISSUE_HISTORY_FK_TO_CONNECTION_TASK,
                    IDC_ISSUE_HISTORY_FK_TO_COM_TASK,
                    IDC_ISSUE_HISTORY_FK_TO_COM_SESSION);
            table.addAuditColumns();
        }
    },
    IDC_ISSUE_OPEN {
        @Override
        public void addTo(DataModel dataModel) {
            Table<OpenIssueDataCollection> table = dataModel.addTable(name(), OpenIssueDataCollection.class);
            table.map(OpenIssueDataCollectionImpl.class);
            table.setJournalTableName("IDC_ISSUE_OPEN_JRNL").upTo(version(10, 2));
            Column idColumn = table.column(IDC_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ISSUE_OPEN", IDC_ISSUE_OPEN_PK,
                    // Foreign keys
                    IDC_ISSUE_OPEN_FK_TO_ISSUE,
                    IDC_ISSUE_OPEN_FK_TO_CONNECTION_TASK,
                    IDC_ISSUE_OPEN_FK_TO_COM_TASK,
                    IDC_ISSUE_OPEN_FK_TO_COM_SESSION);
            table.addAuditColumns();
        }
    },
    IDC_ISSUE_ALL {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueDataCollection> table = dataModel.addTable(name(), IssueDataCollection.class);
            table.map(IssueDataCollectionImpl.class);
            table.doNotAutoInstall();
            Column idColumn = table.column(IDC_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ISSUE_ALL", IDC_ISSUE_PK,
                    // Foreign keys
                    IDC_ISSUE_FK_TO_ISSUE,
                    IDC_ISSUE_FK_TO_CONNECTION_TASK,
                    IDC_ISSUE_FK_TO_COM_TASK,
                    IDC_ISSUE_FK_TO_COM_SESSION);
            table.addAuditColumns();
        }
    },
    IDC_DATACOLLECTION_EVENT {
        @Override
        public void addTo(final DataModel dataModel) {
            Table<DataCollectionEventMetadata> table = dataModel.addTable(name(), DataCollectionEventMetadata.class);
            table.map(DataCollectionEventMetadataImpl.class);
            table.since(version(10, 7));
            table.setJournalTableName("IDC_DATACOLLECTION_EVENT_JRNL");

            Column idColumn = table.addAutoIdColumn();

            TableBuilder.buildDataCollectionEventDescriptionTable(
                    table,
                    idColumn,
                    DATACOLLECTION_EVENT_DESCRIPTION_PK,
                    DATACOLLECTION_EVENT_DESCRIPTION_FK_TO_EVENTTYPE,
                    DATACOLLECTION_EVENT_DESCRIPTION_FK_TO_DEVICE
            );

            table.addAuditColumns();
        }
    };

    public abstract void addTo(DataModel dataModel);

    private static class TableBuilder {
        private static final int EXPECTED_FK_KEYS_LENGTH = 4;

        static void buildIssueTable(Table<?> table, Column idColumn, String issueTable, String pkKey, String... fkKeys) {
            Column issueColRef = table.column(IDC_BASE_ISSUE).number().conversion(NUMBER2LONG).notNull().add();
            Column connectionTaskColRef = table.column(IDC_CONNECTION_TASK).number().conversion(NUMBER2LONG).add();
            Column comTaskColRef = table.column(IDC_COMMUNICATION_TASK).number().conversion(NUMBER2LONG).add();
            Column comSessionColRef = table.column(IDC_COM_SESSION).number().conversion(NUMBER2LONG).add();
            table.column(IDC_DEVICE_MRID).varChar(NAME_LENGTH).map("deviceMRID").add();
            table.column(IDC_FIRST_TRY).number().conversion(NUMBER2INSTANT).map("firstConnectionAttemptTimestamp").since(version(10, 2)).add();
            table.column(IDC_LAST_TRY).number().conversion(NUMBER2INSTANT).map("lastConnectionAttemptTimestamp").since(version(10, 2)).add();
            table.column(IDC_NUMBER_TRIES).number().conversion(NUMBER2LONG).map("connectionAttempt").since(version(10, 2)).add();
            table.column(IDC_LAST_GATEWAY_MRID).varChar(NAME_LENGTH).map("lastGatewayMRID").since(version(10, 4)).add();

            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != EXPECTED_FK_KEYS_LENGTH) {
                throw new IllegalArgumentException("Passed arguments don't match foreign keys");
            }
            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next()).map("baseIssue").on(issueColRef).references(IssueService.COMPONENT_NAME, issueTable).add();
            table.foreignKey(fkKeysIter.next()).map("connectionTask").on(connectionTaskColRef).references(DeviceDataServices.COMPONENT_NAME, "DDC_CONNECTIONTASK").add();
            table.foreignKey(fkKeysIter.next()).map("comTask").on(comTaskColRef).references(DeviceDataServices.COMPONENT_NAME, "DDC_COMTASKEXEC").add();
            table.foreignKey(fkKeysIter.next()).map("comSession").on(comSessionColRef).references(DeviceDataServices.COMPONENT_NAME, "DDC_COMSESSION").add();
        }

        static void buildDataCollectionEventDescriptionTable(Table<?> table, Column idColumn, String pkKey, String... fkKeys) {
            Column createdDateTimeColumn = table.column(ISSUE_CREATEDATETIME)
                    .number()
                    .notNull()
                    .conversion(NUMBER2INSTANT)
                    .map("createDateTime")
                    .installValue(String.valueOf(Instant.EPOCH.toEpochMilli()))
                    .add();

            Column eventTypeRefColumn = table.column(DATACOLLECTION_COLUMN_EVENTTYPE).varChar(NAME_LENGTH).map("eventType").add();

            Column deviceRefColumn = table.column(DATACOLLECTION_COLUMN_DEVICE).number().conversion(NUMBER2LONG).add();

            table.partitionOn(createdDateTimeColumn);
            table.primaryKey(pkKey).on(idColumn).add();

            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next()).on(eventTypeRefColumn).references(EventType.class).map("eventType").onDelete(DeleteRule.CASCADE).upTo(version(10, 8)).add();
            table.foreignKey(fkKeysIter.next()).on(deviceRefColumn).references(Device.class).map(DataCollectionEventMetadataImpl.Fields.DEVICE.fieldName()).onDelete(DeleteRule.CASCADE).add();
        }
    }
}