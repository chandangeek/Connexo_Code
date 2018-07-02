/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.insight.issue.datavalidation.HistoricalIssueDataValidation;
import com.elster.insight.issue.datavalidation.HistoricalIssueNotEstimatedBlock;
import com.elster.insight.issue.datavalidation.IssueDataValidation;
import com.elster.insight.issue.datavalidation.OpenIssueDataValidation;
import com.elster.insight.issue.datavalidation.OpenIssueNotEstimatedBlock;
import com.elster.insight.issue.datavalidation.impl.entity.HistoricalIssueDataValidationImpl;
import com.elster.insight.issue.datavalidation.impl.entity.HistoricalIssueNotEstimatedBlockImpl;
import com.elster.insight.issue.datavalidation.impl.entity.IssueDataValidationImpl;
import com.elster.insight.issue.datavalidation.impl.entity.NotEstimatedBlockImpl;
import com.elster.insight.issue.datavalidation.impl.entity.OpenIssueDataValidationImpl;
import com.elster.insight.issue.datavalidation.impl.entity.OpenIssueNotEstimatedBlockImpl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    IUV_ISSUE_OPEN() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenIssueDataValidation> table = dataModel.addTable(name(), OpenIssueDataValidation.class);
            table.map(OpenIssueDataValidationImpl.class);
            table.since(version(10, 5));
            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("IUV_ISSUE_OPEN_PK").on(issueColRef).add();
            table
                .foreignKey("IUV_ISSUE_OPEN_FK_TO_ISSUE")
                .on(issueColRef)
                .references(OpenIssue.class)
                .map(IssueDataValidationImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    IUV_ISSUE_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<HistoricalIssueDataValidation> table = dataModel.addTable(name(), HistoricalIssueDataValidation.class);
            table.map(HistoricalIssueDataValidationImpl.class);
            table.since(version(10, 5));
            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("IUV_ISSUE_HIST_PK").on(issueColRef).add();
            table
                .foreignKey("IUV_ISSUE_HIST_FK_TO_ISSUE")
                .on(issueColRef)
                .references(HistoricalIssue.class)
                .map(IssueDataValidationImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    IUV_ISSUE_ALL() {
        @Override
        void addTo(DataModel dataModel) {
            Table<IssueDataValidation> table = dataModel.addTable(name(), IssueDataValidation.class);
            table.map(IssueDataValidationImpl.class);
            table.doNotAutoInstall();//because it is mapped to view
            table.since(version(10, 5));
            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("IUV_ISSUE_PK").on(issueColRef).add();
            table
                .foreignKey("IUV_ISSUE_FK_TO_ISSUE")
                .on(issueColRef)
                .references(IssueService.COMPONENT_NAME, "ISU_ISSUE_ALL")
                .map(IssueDataValidationImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    IUV_NOTESTIMATEDBLOCK() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenIssueNotEstimatedBlock> table = dataModel.addTable(name(), OpenIssueNotEstimatedBlock.class);
            table.map(OpenIssueNotEstimatedBlockImpl.class);
            table.since(version(10, 5));
            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column channelRef = table.column("CHANNEL").number().conversion(NUMBER2LONG).notNull().add();
            Column readingTypeRef = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
            Column startTime = table.column("STARTTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(NotEstimatedBlockImpl.Fields.STARTTIME.fieldName()).notNull().add();
            table.column("ENDTIME").number().map(NotEstimatedBlockImpl.Fields.ENDTIME.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();

            table.primaryKey("IUV_NOTESTBLOCK_PK").on(issueRef, channelRef, readingTypeRef, startTime).add();
            table.foreignKey("IUV_NOTESTBLOCK_FK_ISSUE")
                    .on(issueRef)
                    .references(IUV_ISSUE_OPEN.name())
                    .map(NotEstimatedBlockImpl.Fields.ISSUE.fieldName())
                    .reverseMap(IssueDataValidationImpl.Fields.NOTESTIMATEDBLOCKS.fieldName())
                    .composition()
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IUV_NOTESTBLOCK_FK_CHANNEL")
                    .on(channelRef)
                    .references(Channel.class)
                    .map(NotEstimatedBlockImpl.Fields.CHANNEL.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IUV_NOTESTBLOCK_FK_RT")
                    .on(readingTypeRef)
                    .references(ReadingType.class)
                    .map(NotEstimatedBlockImpl.Fields.READINGTYPE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    },

    IUV_NOTESTIMATEDBLOCK_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<HistoricalIssueNotEstimatedBlock> table = dataModel.addTable(name(), HistoricalIssueNotEstimatedBlock.class);
            table.map(HistoricalIssueNotEstimatedBlockImpl.class);
            table.since(version(10, 5));
            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column channelRef = table.column("CHANNEL").number().conversion(NUMBER2LONG).notNull().add();
            Column readingTypeRef = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
            Column startTime = table.column("STARTTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(NotEstimatedBlockImpl.Fields.STARTTIME.fieldName()).notNull().add();
            table.column("ENDTIME").number().map(NotEstimatedBlockImpl.Fields.ENDTIME.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();

            table.primaryKey("IUV_HISTNOTESTBLOCK_PK").on(issueRef, channelRef, readingTypeRef, startTime).add();
            table.foreignKey("IUV_HISTNOTESTBLOCK_FK_ISSUE")
                    .on(issueRef)
                    .references(IUV_ISSUE_HISTORY.name())
                    .map(NotEstimatedBlockImpl.Fields.ISSUE.fieldName())
                    .reverseMap(IssueDataValidationImpl.Fields.NOTESTIMATEDBLOCKS.fieldName())
                    .composition()
                    .onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("IUV_HISTNOTESTBLOCK_FK_CHANNEL")
                    .on(channelRef)
                    .references(Channel.class)
                    .map(NotEstimatedBlockImpl.Fields.CHANNEL.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IUV_HISTNOTESTBLOCK_FK_RT")
                    .on(readingTypeRef)
                    .references(ReadingType.class)
                    .map(NotEstimatedBlockImpl.Fields.READINGTYPE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    }
    ;

    abstract void addTo(DataModel dataModel);

}