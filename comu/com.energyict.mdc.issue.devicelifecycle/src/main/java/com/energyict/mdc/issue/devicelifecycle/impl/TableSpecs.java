/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl;

import com.elster.jupiter.fsm.StateTransition;
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
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.issue.devicelifecycle.FailedTransition;
import com.energyict.mdc.issue.devicelifecycle.HistoricalIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.HistoricalIssueFailedTransition;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueFailedTransition;
import com.energyict.mdc.issue.devicelifecycle.impl.entity.HistoricalIssueDeviceLifecycleImpl;
import com.energyict.mdc.issue.devicelifecycle.impl.entity.HistoricalIssueFailedTransitionImpl;
import com.energyict.mdc.issue.devicelifecycle.impl.entity.IssueDeviceLifecycleImpl;
import com.energyict.mdc.issue.devicelifecycle.impl.entity.FailedTransitionImpl;
import com.energyict.mdc.issue.devicelifecycle.impl.entity.OpenIssueDeviceLifecycleImpl;
import com.energyict.mdc.issue.devicelifecycle.impl.entity.OpenIssueFailedTransitionImpl;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    IDL_ISSUE_OPEN() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenIssueDeviceLifecycle> table = dataModel.addTable(name(), OpenIssueDeviceLifecycle.class);
            table.map(OpenIssueDeviceLifecycleImpl.class);
            table.setJournalTableName(name() + "JRNL").upTo(version(10, 2));

            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("IDL_ISSUE_OPEN_PK").on(issueColRef).add();
            table
                .foreignKey("IDL_ISSUE_OPEN_FK_TO_ISSUE")
                .on(issueColRef)
                .references(OpenIssue.class)
                .map(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    IDL_ISSUE_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<HistoricalIssueDeviceLifecycle> table = dataModel.addTable(name(), HistoricalIssueDeviceLifecycle.class);
            table.map(HistoricalIssueDeviceLifecycleImpl.class);
            table.setJournalTableName(name() + "JRNL").upTo(version(10, 2));

            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("IDL_ISSUE_HIST_PK").on(issueColRef).add();
            table
                .foreignKey("IDL_ISSUE_HIST_FK_TO_ISSUE")
                .on(issueColRef)
                .references(HistoricalIssue.class)
                .map(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    IDL_ISSUE_ALL() {
        @Override
        void addTo(DataModel dataModel) {
            Table<IssueDeviceLifecycle> table = dataModel.addTable(name(), IssueDeviceLifecycle.class);
            table.map(IssueDeviceLifecycleImpl.class);
            table.doNotAutoInstall();//because it is mapped to view

            Column issueColRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();

            table.addAuditColumns();
            table.primaryKey("IDL_ISSUE_PK").on(issueColRef).add();
            table
                .foreignKey("IDL_ISSUE_FK_TO_ISSUE")
                .on(issueColRef)
                .references(IssueService.COMPONENT_NAME, "ISU_ISSUE_ALL")
                .map(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName())
                .add();
        }
    },

    IDL_FAILEDTRANSITION() {
        @Override
        void addTo(DataModel dataModel) {
            Table<OpenIssueFailedTransition> table = dataModel.addTable(name(), OpenIssueFailedTransition.class);
            table.map(OpenIssueFailedTransitionImpl.class);

            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column lifecycleRef = table.column(FailedTransitionImpl.Fields.LIFECYCLE.fieldName()).number().conversion(NUMBER2LONG).notNull().add();
            Column transitionRef = table.column(FailedTransitionImpl.Fields.TRANSITION.fieldName()).number().conversion(NUMBER2LONG).notNull().add();
            Column fromRef = table.column(FailedTransitionImpl.Fields.FROM.fieldName()).number().conversion(NUMBER2LONG).notNull().add();
            Column toRef = table.column(FailedTransitionImpl.Fields.TO.fieldName()).number().conversion(NUMBER2LONG).notNull().add();
            Column cause = table.column(FailedTransitionImpl.Fields.CAUSE.fieldName()).varChar(DESCRIPTION_LENGTH).notNull().add();
            Column modTime = table.column("TIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(FailedTransitionImpl.Fields.MODTIME.fieldName()).notNull().add();

            table.primaryKey("IDL_FAILEDTRANSITION_PK").on(issueRef, lifecycleRef, transitionRef, fromRef, toRef, modTime, cause).add();
            table.foreignKey("IDL_FAILEDTRANSITION_FK_ISSUE")
                    .on(issueRef)
                    .references(IDL_ISSUE_OPEN.name())
                    .map(FailedTransitionImpl.Fields.ISSUE.fieldName())
                    .reverseMap(IssueDeviceLifecycleImpl.Fields.FAILED_TRANSITIONS.fieldName())
                    .composition()
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IDL_FAILEDTRANSITION_FK_LIFECYLE")
                    .on()
                    .references(DeviceLifeCycle.class)
                    .map(FailedTransitionImpl.Fields.LIFECYCLE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IDL_FAILEDTRANSITION_FK_TRANSITION")
                    .on(transitionRef)
                    .references(StateTransition.class)
                    .map(FailedTransitionImpl.Fields.TRANSITION.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IDL_FAILEDTRANSITION_FK_FROM")
                    .on(fromRef)
                    .references(StateTransition.class)
                    .map(FailedTransitionImpl.Fields.FROM.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IDL_FAILEDTRANSITION_FK_TO")
                    .on(toRef)
                    .references(StateTransition.class)
                    .map(FailedTransitionImpl.Fields.TO.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    },

    IDL_FAILEDTRANSITION_HISTORY() {
        @Override
        void addTo(DataModel dataModel) {
            Table<HistoricalIssueFailedTransition> table = dataModel.addTable(name(), HistoricalIssueFailedTransition.class);
            table.map(HistoricalIssueFailedTransitionImpl.class);

            Column issueRef = table.column("ISSUE").number().conversion(NUMBER2LONG).notNull().add();
            Column lifecycleRef = table.column(FailedTransitionImpl.Fields.LIFECYCLE.fieldName()).number().conversion(NUMBER2LONG).notNull().add();
            Column transitionRef = table.column(FailedTransitionImpl.Fields.TRANSITION.fieldName()).number().conversion(NUMBER2LONG).notNull().add();
            Column fromRef = table.column(FailedTransitionImpl.Fields.FROM.fieldName()).number().conversion(NUMBER2LONG).notNull().add();
            Column toRef = table.column(FailedTransitionImpl.Fields.TO.fieldName()).number().conversion(NUMBER2LONG).notNull().add();
            Column cause = table.column(FailedTransitionImpl.Fields.CAUSE.fieldName()).varChar(DESCRIPTION_LENGTH).notNull().add();
            Column modTime = table.column("TIME").number().conversion(ColumnConversion.NUMBER2INSTANT).map(FailedTransitionImpl.Fields.MODTIME.fieldName()).notNull().add();

            table.primaryKey("IDL_HISTFAILEDTRANSITION_PK").on(issueRef, lifecycleRef, transitionRef, fromRef, toRef, cause).add();
            table.foreignKey("IDL_HISTFAILEDTRANSITION_FK_ISSUE")
                    .on(issueRef)
                    .references(IDL_ISSUE_OPEN.name())
                    .map(FailedTransitionImpl.Fields.ISSUE.fieldName())
                    .reverseMap(IssueDeviceLifecycleImpl.Fields.FAILED_TRANSITIONS.fieldName())
                    .composition()
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IDL_HISTFAILEDTRANSITION_FK_LIFECYLE")
                    .on()
                    .references(DeviceLifeCycle.class)
                    .map(FailedTransitionImpl.Fields.LIFECYCLE.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IDL_HISTFAILEDTRANSITION_FK_TRANSITION")
                    .on(transitionRef)
                    .references(StateTransition.class)
                    .map(FailedTransitionImpl.Fields.TRANSITION.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IDL_HISTFAILEDTRANSITION_FK_FROM")
                    .on(fromRef)
                    .references(StateTransition.class)
                    .map(FailedTransitionImpl.Fields.FROM.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("IDL_HISTFAILEDTRANSITION_FK_TO")
                    .on(toRef)
                    .references(StateTransition.class)
                    .map(FailedTransitionImpl.Fields.TO.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    },
    ;
    abstract void addTo(DataModel dataModel);

}