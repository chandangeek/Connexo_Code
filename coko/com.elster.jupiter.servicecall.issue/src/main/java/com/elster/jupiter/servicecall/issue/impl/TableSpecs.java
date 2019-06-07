/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.issue.OpenIssueServiceCall;
import com.elster.jupiter.servicecall.issue.impl.entity.OpenIssueServiceCallImpl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    ISC_ISSUE_OPEN() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenIssueServiceCall> table = dataModel.addTable(name(), OpenIssueServiceCall.class);
            table.map(OpenIssueServiceCallImpl.class);
            table.setJournalTableName(name() + "JRNL").upTo(version(10, 2));

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

//    ISC_ISSUE_HISTORY() {
//        @Override
//        void addTo(DataModel dataModel) {
//            Table<HistoricalIssueDataValidation> table = dataModel.addTable(name(), HistoricalIssueDataValidation.class);
//            table.map(HistoricalIssueDataValidationImpl.class);
//            table.setJournalTableName(name() + "JRNL").upTo(version(10, 2));
//
//            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
//
//            table.addAuditColumns();
//            table.primaryKey("ISC_ISSUE_HIST_PK").on(issueColRef).add();
//            table
//                .foreignKey("ISC_ISSUE_HIST_FK_TO_ISSUE")
//                .on(issueColRef)
//                .references(HistoricalIssue.class)
//                .map(IssueDataValidationImpl.Fields.BASEISSUE.fieldName())
//                .add();
//        }
//    },

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
    },

//    ISC_NOTESTIMATEDBLOCK() {
//        @Override
//        void addTo(DataModel dataModel) {
//            Table<Ope> table = dataModel.addTable(name(), OpenIssueNotEstimatedBlock.class);
//            table.map(OpenIssueNotEstimatedBlockImpl.class);
//
//            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
//            Column channelRef = table.column("CHANNEL").number().conversion(NUMBER2LONG).notNull().add();
//            Column readingTypeRef = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
//            Column startTime = table.column("STARTTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(NotEstimatedBlockImpl.Fields.STARTTIME.fieldName()).notNull().add();
//            table.column("ENDTIME").number().map(NotEstimatedBlockImpl.Fields.ENDTIME.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();
//
//            table.primaryKey("ISC_NOTESTBLOCK_PK").on(issueRef, channelRef, readingTypeRef, startTime).add();
//            table.foreignKey("ISC_NOTESTBLOCK_FK_ISSUE")
//                    .on(issueRef)
//                    .references(ISC_ISSUE_OPEN.name())
//                    .map(NotEstimatedBlockImpl.Fields.ISSUE.fieldName())
//                    .reverseMap(IssueDataValidationImpl.Fields.NOTESTIMATEDBLOCKS.fieldName())
//                    .composition()
//                    .onDelete(DeleteRule.CASCADE)
//                    .add();
//            table.foreignKey("ISC_NOTESTBLOCK_FK_CHANNEL")
//                    .on(channelRef)
//                    .references(Channel.class)
//                    .map(NotEstimatedBlockImpl.Fields.CHANNEL.fieldName())
//                    .onDelete(DeleteRule.CASCADE)
//                    .add();
//            table.foreignKey("ISC_NOTESTBLOCK_FK_RT")
//                    .on(readingTypeRef)
//                    .references(ReadingType.class)
//                    .map(NotEstimatedBlockImpl.Fields.READINGTYPE.fieldName())
//                    .onDelete(DeleteRule.CASCADE)
//                    .add();
//        }
//    },

//    ISC_NOTESTIMATEDBLOCK_HISTORY() {
//        @Override
//        void addTo(DataModel dataModel) {
//            Table<HistoricalIssueNotEstimatedBlock> table = dataModel.addTable(name(), HistoricalIssueNotEstimatedBlock.class);
//            table.map(HistoricalIssueNotEstimatedBlockImpl.class);
//
//            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
//            Column channelRef = table.column("CHANNEL").number().conversion(NUMBER2LONG).notNull().add();
//            Column readingTypeRef = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
//            Column startTime = table.column("STARTTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(NotEstimatedBlockImpl.Fields.STARTTIME.fieldName()).notNull().add();
//            table.column("ENDTIME").number().map(NotEstimatedBlockImpl.Fields.ENDTIME.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();
//
//            table.primaryKey("ISC_HISTNOTESTBLOCK_PK").on(issueRef, channelRef, readingTypeRef, startTime).add();
//            table.foreignKey("ISC_HISTNOTESTBLOCK_FK_ISSUE")
//                    .on(issueRef)
//                    .references(ISC_ISSUE_HISTORY.name())
//                    .map(NotEstimatedBlockImpl.Fields.ISSUE.fieldName())
//                    .reverseMap(IssueDataValidationImpl.Fields.NOTESTIMATEDBLOCKS.fieldName())
//                    .composition()
//                    .onDelete(DeleteRule.CASCADE).add();
//            table.foreignKey("ISC_HISTNOTESTBLOCK_FK_CHANNEL")
//                    .on(channelRef)
//                    .references(Channel.class)
//                    .map(NotEstimatedBlockImpl.Fields.CHANNEL.fieldName())
//                    .onDelete(DeleteRule.CASCADE)
//                    .add();
//            table.foreignKey("ISC_HISTNOTESTBLOCK_FK_RT")
//                    .on(readingTypeRef)
//                    .references(ReadingType.class)
//                    .map(NotEstimatedBlockImpl.Fields.READINGTYPE.fieldName())
//                    .onDelete(DeleteRule.CASCADE)
//                    .add();
//        }
//    },

    ;

    abstract void addTo(DataModel dataModel);

}