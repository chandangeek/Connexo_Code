package com.energyict.mdc.issue.datavalidation.impl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueNotEstimatedBlock;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.OpenIssueNotEstimatedBlock;
import com.energyict.mdc.issue.datavalidation.impl.entity.HistoricalIssueDataValidationImpl;
import com.energyict.mdc.issue.datavalidation.impl.entity.HistoricalIssueNotEstimatedBlockImpl;
import com.energyict.mdc.issue.datavalidation.impl.entity.IssueDataValidationImpl;
import com.energyict.mdc.issue.datavalidation.impl.entity.NotEstimatedBlockImpl;
import com.energyict.mdc.issue.datavalidation.impl.entity.OpenIssueDataValidationImpl;
import com.energyict.mdc.issue.datavalidation.impl.entity.OpenIssueNotEstimatedBlockImpl;

public enum TableSpecs {

    IDV_ISSUE_OPEN() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenIssueDataValidation> table = dataModel.addTable(name(), OpenIssueDataValidation.class);
            table.map(OpenIssueDataValidationImpl.class);
            table.setJournalTableName(name() + "JRNL");
            
            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            
            table.primaryKey("IDV_ISSUE_OPEN_PK").on(issueColRef).add();
            table.foreignKey("IDV_ISSUE_OPEN_FK_TO_ISSUE").map(IssueDataValidationImpl.Fields.BASEISSUE.fieldName()).on(issueColRef).references(IssueService.COMPONENT_NAME, "ISU_ISSUE_OPEN").add();
            table.addAuditColumns();
        }
    },
    
    IDV_ISSUE_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<HistoricalIssueDataValidation> table = dataModel.addTable(name(), HistoricalIssueDataValidation.class);
            table.map(HistoricalIssueDataValidationImpl.class);
            table.setJournalTableName(name() + "JRNL");
            
            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            
            table.primaryKey("IDV_ISSUE_HIST_PK").on(issueColRef).add();
            table.foreignKey("IDV_ISSUE_HIST_FK_TO_ISSUE").map(IssueDataValidationImpl.Fields.BASEISSUE.fieldName()).on(issueColRef).references(IssueService.COMPONENT_NAME, "ISU_ISSUE_HISTORY").add();
            table.addAuditColumns();
        }
    },
    
    IDV_ISSUE_ALL() {
        @Override
        void addTo(DataModel dataModel) {
            Table<IssueDataValidation> table = dataModel.addTable(name(), IssueDataValidation.class);
            table.map(IssueDataValidationImpl.class);
            table.doNotAutoInstall();//because it is mapped to view
            
            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            
            table.primaryKey("IDV_ISSUE_PK").on(issueColRef).add();
            table.foreignKey("IDV_ISSUE_FK_TO_ISSUE").map(IssueDataValidationImpl.Fields.BASEISSUE.fieldName()).on(issueColRef).references(IssueService.COMPONENT_NAME, "ISU_ISSUE_ALL").add();
            table.addAuditColumns();
        }
    },
    
    IDV_NOTESTIMATEDBLOCK() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenIssueNotEstimatedBlock> table = dataModel.addTable(name(), OpenIssueNotEstimatedBlock.class);
            table.map(OpenIssueNotEstimatedBlockImpl.class);
            
            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column channelRef = table.column("CHANNEL").number().conversion(NUMBER2LONG).notNull().add();
            Column readingTypeRef = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
            Column startTime = table.column("STARTTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(NotEstimatedBlockImpl.Fields.STARTTIME.fieldName()).notNull().add();
            table.column("ENDTIME").number().map(NotEstimatedBlockImpl.Fields.ENDTIME.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();
            
            table.primaryKey("IDV_NOTESTBLOCK_PK").on(issueRef, channelRef, readingTypeRef, startTime).add();
            table.foreignKey("IDV_NOTESTBLOCK_FK_ISSUE")
                    .on(issueRef).references(IDV_ISSUE_OPEN.name())
                    .map(NotEstimatedBlockImpl.Fields.ISSUE.fieldName())
                    .reverseMap(IssueDataValidationImpl.Fields.NOTESTIMATEDBLOCKS.fieldName()).composition()
                    .onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("IDV_NOTESTBLOCK_FK_CHANNEL")
                    .on(channelRef).map(NotEstimatedBlockImpl.Fields.CHANNEL.fieldName())
                    .references(MeteringService.COMPONENTNAME, "MTR_CHANNEL")
                    .onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("IDV_NOTESTBLOCK_FK_RT")
                    .on(readingTypeRef).map(NotEstimatedBlockImpl.Fields.READINGTYPE.fieldName())
                    .references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE")
                    .onDelete(DeleteRule.CASCADE).add();
        }
    },
    
    IDV_NOTESTIMATEDBLOCK_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<HistoricalIssueNotEstimatedBlock> table = dataModel.addTable(name(), HistoricalIssueNotEstimatedBlock.class);
            table.map(HistoricalIssueNotEstimatedBlockImpl.class);
            
            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column channelRef = table.column("CHANNEL").number().conversion(NUMBER2LONG).notNull().add();
            Column readingTypeRef = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
            Column startTime = table.column("STARTTIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(NotEstimatedBlockImpl.Fields.STARTTIME.fieldName()).notNull().add();
            table.column("ENDTIME").number().map(NotEstimatedBlockImpl.Fields.ENDTIME.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).notNull().add();
            
            table.primaryKey("IDV_HISTNOTESTBLOCK_PK").on(issueRef, channelRef, readingTypeRef, startTime).add();
            table.foreignKey("IDV_HISTNOTESTBLOCK_FK_ISSUE")
                    .on(issueRef).references(IDV_ISSUE_HISTORY.name())
                    .map(NotEstimatedBlockImpl.Fields.ISSUE.fieldName())
                    .reverseMap(IssueDataValidationImpl.Fields.NOTESTIMATEDBLOCKS.fieldName()).composition()
                    .onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("IDV_HISTNOTESTBLOCK_FK_CHANNEL")
                    .on(channelRef).map(NotEstimatedBlockImpl.Fields.CHANNEL.fieldName())
                    .references(MeteringService.COMPONENTNAME, "MTR_CHANNEL")
                    .onDelete(DeleteRule.CASCADE).add();
            table.foreignKey("IDV_HISTNOTESTBLOCK_FK_RT")
                    .on(readingTypeRef).map(NotEstimatedBlockImpl.Fields.READINGTYPE.fieldName())
                    .references(MeteringService.COMPONENTNAME, "MTR_READINGTYPE")
                    .onDelete(DeleteRule.CASCADE).add();
        }
    },
    
    ;

    abstract void addTo(DataModel dataModel);

}