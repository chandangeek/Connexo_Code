/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl;

import com.elster.jupiter.issue.servicecall.HistoricalServiceCallIssue;
import com.elster.jupiter.issue.servicecall.OpenServiceCallIssue;
import com.elster.jupiter.issue.servicecall.ServiceCallIssue;
import com.elster.jupiter.issue.servicecall.impl.entity.HistoricalServiceCallIssueImpl;
import com.elster.jupiter.issue.servicecall.impl.entity.OpenServiceCallIssueImpl;
import com.elster.jupiter.issue.servicecall.impl.entity.ServiceCallIssueImpl;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCall;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

public enum TableSpecs {

    ISC_ISSUE_OPEN() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenServiceCallIssue> table = dataModel.addTable(name(), OpenServiceCallIssue.class);
            table.map(OpenServiceCallIssueImpl.class);
            table.setJournalTableName(name() + "JRNL");

            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column serviceCallColRef = table.column(ServiceCallIssueImpl.Fields.SERVICECALL.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();

            table.column(ServiceCallIssueImpl.Fields.STATE.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2ENUM)
                    .map(ServiceCallIssueImpl.Fields.STATE.fieldName())
                    .add();

            table.addAuditColumns();
            table.primaryKey("ISC_ISSUE_OPEN_PK").on(issueColRef).add();
            table
                .foreignKey("ISC_ISSUE_OPEN_FK_TO_ISSUE")
                .on(issueColRef)
                .references(OpenIssue.class)
                .map(OpenServiceCallIssueImpl.Fields.BASEISSUE.fieldName())
                .add();
            table.foreignKey("ISC_ISSUE_OPEN_FK_TO_SC")
                    .on(serviceCallColRef)
                    .references(ServiceCall.class)
                    .map(ServiceCallIssueImpl.Fields.SERVICECALL.fieldName())
                    .add();
        }
    },

    ISC_ISSUE_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<HistoricalServiceCallIssue> table = dataModel.addTable(name(), HistoricalServiceCallIssue.class);
            table.map(HistoricalServiceCallIssueImpl.class);
            table.setJournalTableName(name() + "JRNL");

            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column serviceCallColRef = table.column(ServiceCallIssueImpl.Fields.SERVICECALL.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();

            table.column(ServiceCallIssueImpl.Fields.STATE.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2ENUM)
                    .map(ServiceCallIssueImpl.Fields.STATE.fieldName())
                    .add();

            table.addAuditColumns();
            table.primaryKey("ISC_ISSUE_HIST_PK").on(issueColRef).add();
            table
                .foreignKey("ISC_ISSUE_HIST_FK_TO_ISSUE")
                .on(issueColRef)
                .references(HistoricalIssue.class)
                .map(HistoricalServiceCallIssueImpl.Fields.BASEISSUE.fieldName())
                .add();
            table.foreignKey("ISC_ISSUE_HIST_FK_TO_SC")
                    .on(serviceCallColRef)
                    .references(ServiceCall.class)
                    .map(ServiceCallIssueImpl.Fields.SERVICECALL.fieldName())
                    .add();
        }
    },

    ISC_ISSUE_ALL() {
        @Override
        void addTo(DataModel dataModel) {
            Table<ServiceCallIssue> table = dataModel.addTable(name(), ServiceCallIssue.class);
            table.map(ServiceCallIssueImpl.class);
            table.doNotAutoInstall();//because it is mapped to view

            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column serviceCallColRef = table.column(ServiceCallIssueImpl.Fields.SERVICECALL.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();

            table.column(ServiceCallIssueImpl.Fields.STATE.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2ENUM)
                    .map(ServiceCallIssueImpl.Fields.STATE.fieldName())
                    .add();
            table.addAuditColumns();
            table.primaryKey("ISC_ISSUE_PK").on(issueColRef).add();
            table
                .foreignKey("ISC_ISSUE_FK_TO_ISSUE")
                .on(issueColRef)
                .references(IssueService.COMPONENT_NAME, "ISU_ISSUE_ALL")
                .map(ServiceCallIssueImpl.Fields.BASEISSUE.fieldName())
                .add();
            table.foreignKey("ISC_ISSUE_FK_TO_SERVICECALL")
                    .on(serviceCallColRef)
                    .references(ServiceCall.class)
                    .map(ServiceCallIssueImpl.Fields.SERVICECALL.fieldName())
                    .add();
        }
    };
    abstract void addTo(DataModel dataModel);

}