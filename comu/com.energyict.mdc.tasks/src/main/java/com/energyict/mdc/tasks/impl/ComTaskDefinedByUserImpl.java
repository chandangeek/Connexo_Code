/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ComTaskUserAction;

import com.google.inject.Provider;

import javax.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serves as an implementation for a ComTask that is defined by the User
 */
class ComTaskDefinedByUserImpl extends ComTaskImpl implements PersistenceAware {

    static final class ComTaskUserActionRecord {
        private ComTaskUserAction userAction;
        @SuppressWarnings("unused")
        private Reference<ComTask> comTask = ValueReference.absent();
        @SuppressWarnings("unused")
        private String userName;
        @SuppressWarnings("unused")
        private long version;
        @SuppressWarnings("unused")
        private Instant createTime;
        @SuppressWarnings("unused")
        private Instant modTime;

        ComTaskUserActionRecord() {

        }

        ComTaskUserActionRecord(ComTask comTask, ComTaskUserAction userAction) {
            this.comTask.set(comTask);
            this.userAction = userAction;
        }
    }

    private List<ComTaskUserActionRecord> comTaskUserActionRecords = new ArrayList<>();
    private Set<ComTaskUserAction> comTaskUserActions = new HashSet<>();

    @Inject
    ComTaskDefinedByUserImpl(Provider<LogBooksTaskImpl> logBooksTaskProvider, DataModel dataModel,
            Provider<StatusInformationTaskImpl> statusInformationTaskProvider,
            Provider<MessagesTaskImpl> messagesTaskProvider, Provider<BasicCheckTaskImpl> basicCheckTaskProvider,
            Provider<RegistersTaskImpl> registersTaskProvider, EventService eventService,
            Provider<ClockTaskImpl> clockTaskProvider, Provider<TopologyTaskImpl> topologyTaskProvider,
            Thesaurus thesaurus, Provider<LoadProfilesTaskImpl> loadProfilesTaskProvider,
            Provider<FirmwareManagementTaskImpl> firmwareManagementTaskProvider) {
        super(logBooksTaskProvider, dataModel, statusInformationTaskProvider, messagesTaskProvider,
                basicCheckTaskProvider, registersTaskProvider, eventService, clockTaskProvider, topologyTaskProvider,
                thesaurus, loadProfilesTaskProvider, firmwareManagementTaskProvider);
    }

    @Override
    public boolean isUserComTask() {
        return true;
    }

    @Override
    public boolean isSystemComTask() {
        return false;
    }

    @Override
    public void postLoad() {
        comTaskUserActions.addAll(comTaskUserActionRecords.stream().map(userActionRecord -> userActionRecord.userAction)
                .collect(Collectors.toList()));
    }

    @Override
    public Set<ComTaskUserAction> getUserActions() {
        return comTaskUserActions;
    }

    @Override
    public void setUserActions(Set<ComTaskUserAction> userActions) {
        if (userActions == null) {
            comTaskUserActions = Collections.emptySet();
            comTaskUserActionRecords = Collections.emptyList();
            return;
        }
        comTaskUserActions = userActions;
        comTaskUserActionRecords = comTaskUserActions.stream()
                .map(userAction -> new ComTaskUserActionRecord(this, userAction)).collect(Collectors.toList());
    }

}