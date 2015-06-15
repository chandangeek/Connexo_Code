package com.energyict.mdc.issue.datacollection.impl.database;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.records.HistoricalIssueDataCollectionImpl;
import com.energyict.mdc.issue.datacollection.impl.records.IssueDataCollectionImpl;
import com.energyict.mdc.issue.datacollection.impl.records.OpenIssueDataCollectionImpl;

import java.util.Arrays;
import java.util.ListIterator;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.energyict.mdc.issue.datacollection.impl.database.DatabaseConst.*;

public enum TableSpecs {
    IDC_ISSUE_HISTORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalIssueDataCollection> table = dataModel.addTable(name(), HistoricalIssueDataCollection.class);
            table.map(HistoricalIssueDataCollectionImpl.class);
            table.setJournalTableName(IDC_ISSUE_HISTORY_JRNL_TABLE_NAME);
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
            table.setJournalTableName(IDC_ISSUE_OPEN_JRNL_TABLE_NAME);
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
    }
    ;

	public abstract void addTo(DataModel dataModel);

    private static class TableBuilder{
        private static final int EXPECTED_FK_KEYS_LENGTH = 4;

        static void buildIssueTable(Table<?> table, Column idColumn, String issueTable, String pkKey, String... fkKeys){
            Column issueColRef = table.column(IDC_BASE_ISSUE).number().conversion(NUMBER2LONG).notNull().add();
            Column connectionTaskColRef = table.column(IDC_CONNECTION_TASK).number().conversion(NUMBER2LONG).add();
            Column comTaskColRef = table.column(IDC_COMMUNICATION_TASK).number().conversion(NUMBER2LONG).add();
            Column comSessionColRef = table.column(IDC_COM_SESSION).type("number").conversion(NUMBER2LONG).add();
            table.column(IDC_DEVICE_MRID).varChar(NAME_LENGTH).map("deviceMRID").add();

            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != EXPECTED_FK_KEYS_LENGTH){
                throw new IllegalArgumentException("Passed arguments don't match foreigen keys");
            }
            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next()).map("baseIssue").on(issueColRef).references(IssueService.COMPONENT_NAME, issueTable).add();
            table.foreignKey(fkKeysIter.next()).map("connectionTask").on(connectionTaskColRef).references(DeviceDataServices.COMPONENT_NAME, "DDC_CONNECTIONTASK").add();
            table.foreignKey(fkKeysIter.next()).map("comTask").on(comTaskColRef).references(DeviceDataServices.COMPONENT_NAME, "DDC_COMTASKEXEC").add();
            table.foreignKey(fkKeysIter.next()).map("comSession").on(comSessionColRef).references(DeviceDataServices.COMPONENT_NAME, "DDC_COMSESSION").add();
        }
    }
}