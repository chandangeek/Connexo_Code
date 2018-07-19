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
import com.elster.insight.issue.datavalidation.UsagePointHistoricalIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointHistoricalIssueUsagePointNotEstimatedBlock;
import com.elster.insight.issue.datavalidation.UsagePointOpenIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointOpenIssueUsagePointNotEstimatedBlock;
import com.elster.insight.issue.datavalidation.impl.entity.UsagePointHistoricalIssueDataValidationImpl;
import com.elster.insight.issue.datavalidation.impl.entity.UsagePointHistoricalIssueUsagePointUsagePointNotEstimatedBlockImpl;
import com.elster.insight.issue.datavalidation.impl.entity.UsagePointOpenIssueDataValidationImpl;
import com.elster.insight.issue.datavalidation.impl.entity.UsagePointIssueDataValidationImpl;
import com.elster.insight.issue.datavalidation.impl.entity.UsagePointUsagePointNotEstimatedBlockImpl;
import com.elster.insight.issue.datavalidation.impl.entity.UsagePointOpenIssueUsagePointUsagePointNotEstimatedBlockImpl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    IUV_ISSUE_OPEN() {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointOpenIssueDataValidation> table = dataModel.addTable(name(), UsagePointOpenIssueDataValidation.class);
            table.map(UsagePointOpenIssueDataValidationImpl.class);
            table.since(version(10, 5));
            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("IUV_ISSUE_OPEN_PK").on(issueColRef).add();
            table
                .foreignKey("IUV_ISSUE_OPEN_FK_TO_ISSUE")
                .on(issueColRef)
                .references(OpenIssue.class)
                .map(UsagePointIssueDataValidationImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    IUV_ISSUE_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointHistoricalIssueDataValidation> table = dataModel.addTable(name(), UsagePointHistoricalIssueDataValidation.class);
            table.map(UsagePointHistoricalIssueDataValidationImpl.class);
            table.since(version(10, 5));
            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("IUV_ISSUE_HIST_PK").on(issueColRef).add();
            table
                .foreignKey("IUV_ISSUE_HIST_FK_TO_ISSUE")
                .on(issueColRef)
                .references(HistoricalIssue.class)
                .map(UsagePointIssueDataValidationImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    IUV_ISSUE_ALL() {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointIssueDataValidation> table = dataModel.addTable(name(), UsagePointIssueDataValidation.class);
            table.map(UsagePointIssueDataValidationImpl.class);
            table.doNotAutoInstall();//because it is mapped to view
            table.since(version(10, 5));
            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("IUV_ISSUE_PK").on(issueColRef).add();
            table
                .foreignKey("IUV_ISSUE_FK_TO_ISSUE")
                .on(issueColRef)
                .references(IssueService.COMPONENT_NAME, "ISU_ISSUE_ALL")
                .map(UsagePointIssueDataValidationImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    IUV_NOTESTIMATEDBLOCK() {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointOpenIssueUsagePointNotEstimatedBlock> table = dataModel.addTable(name(), UsagePointOpenIssueUsagePointNotEstimatedBlock.class);
            table.map(UsagePointOpenIssueUsagePointUsagePointNotEstimatedBlockImpl.class);
            table.since(version(10, 5));
            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column channelRef = table.column("CHANNEL").number().conversion(NUMBER2LONG).notNull().add();
            Column readingTypeRef = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
            Column startTime = table.column("STARTTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.STARTTIME.fieldName()).notNull().add();
            table.column("ENDTIME").number().map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.ENDTIME.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();

            table.primaryKey("IUV_NOTESTBLOCK_PK").on(issueRef, channelRef, readingTypeRef, startTime).add();
            table.foreignKey("IUV_NOTESTBLOCK_FK_ISSUE")
                    .on(issueRef)
                    .references(IUV_ISSUE_OPEN.name())
                    .map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.ISSUE.fieldName())
                    .reverseMap(UsagePointIssueDataValidationImpl.Fields.NOTESTIMATEDBLOCKS.fieldName())
                    .composition()
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IUV_NOTESTBLOCK_FK_CHANNEL")
                    .on(channelRef)
                    .references(Channel.class)
                    .map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.CHANNEL.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IUV_NOTESTBLOCK_FK_RT")
                    .on(readingTypeRef)
                    .references(ReadingType.class)
                    .map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.READINGTYPE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    },

    IUV_NOTESTIMATEDBLOCK_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointHistoricalIssueUsagePointNotEstimatedBlock> table = dataModel.addTable(name(), UsagePointHistoricalIssueUsagePointNotEstimatedBlock.class);
            table.map(UsagePointHistoricalIssueUsagePointUsagePointNotEstimatedBlockImpl.class);
            table.since(version(10, 5));
            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column channelRef = table.column("CHANNEL").number().conversion(NUMBER2LONG).notNull().add();
            Column readingTypeRef = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
            Column startTime = table.column("STARTTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.STARTTIME.fieldName()).notNull().add();
            table.column("ENDTIME").number().map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.ENDTIME.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();

            table.primaryKey("IUV_HISTNOTESTBLOCK_PK").on(issueRef, channelRef, readingTypeRef, startTime).add();
            table.foreignKey("IUV_HISTNOTESTBLOCK_FK_ISSUE")
                    .on(issueRef)
                    .references(IUV_ISSUE_HISTORY.name())
                    .map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.ISSUE.fieldName())
                    .reverseMap(UsagePointIssueDataValidationImpl.Fields.NOTESTIMATEDBLOCKS.fieldName())
                    .composition()
                    .onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("IUV_HISTNOTESTBLOCK_FK_CHANNEL")
                    .on(channelRef)
                    .references(Channel.class)
                    .map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.CHANNEL.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IUV_HISTNOTESTBLOCK_FK_RT")
                    .on(readingTypeRef)
                    .references(ReadingType.class)
                    .map(UsagePointUsagePointNotEstimatedBlockImpl.Fields.READINGTYPE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    }
    ;

    abstract void addTo(DataModel dataModel);

}