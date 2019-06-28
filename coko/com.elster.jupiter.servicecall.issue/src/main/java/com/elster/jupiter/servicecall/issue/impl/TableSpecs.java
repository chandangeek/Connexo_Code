/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.issue.HistoricalIssueServiceCall;
import com.elster.jupiter.servicecall.issue.OpenIssueServiceCall;
import com.elster.jupiter.servicecall.issue.impl.entity.HistoricalIssueServiceCallImpl;
import com.elster.jupiter.servicecall.issue.impl.entity.OpenIssueServiceCallImpl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

public enum TableSpecs {

    ISC_ISSUE_OPEN() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenIssueServiceCall> table = dataModel.addTable(name(), OpenIssueServiceCall.class);
            table.map(OpenIssueServiceCallImpl.class);
            table.setJournalTableName(name() + "JRNL");

            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("ISC_ISSUE_OPEN_PK").on(issueColRef).add();
            table
                .foreignKey("ISC_ISSUE_OPEN_FK_TO_ISSUE")
                .on(issueColRef)
                .references(OpenIssue.class)
                .map(OpenIssueServiceCallImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    ISC_ISSUE_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<HistoricalIssueServiceCall> table = dataModel.addTable(name(), HistoricalIssueServiceCall.class);
            table.map(HistoricalIssueServiceCallImpl.class);
            table.setJournalTableName(name() + "JRNL");

            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("ISC_ISSUE_HIST_PK").on(issueColRef).add();
            table
                .foreignKey("ISC_ISSUE_HIST_FK_TO_ISSUE")
                .on(issueColRef)
                .references(HistoricalIssue.class)
                .map(HistoricalIssueServiceCallImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    ISC_ISSUE_ALL() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenIssueServiceCall> table = dataModel.addTable(name(), OpenIssueServiceCall.class);
            table.map(OpenIssueServiceCallImpl.class);
            table.doNotAutoInstall();//because it is mapped to view

            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("ISC_ISSUE_PK").on(issueColRef).add();
            table
                .foreignKey("ISC_ISSUE_FK_TO_ISSUE")
                .on(issueColRef)
                .references(IssueService.COMPONENT_NAME, "ISU_ISSUE_ALL")
                .map(OpenIssueServiceCallImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    };
    abstract void addTo(DataModel dataModel);

}