/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import com.energyict.mdc.common.tasks.ComTaskUserAction;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.common.tasks.TaskServiceKeys;

import com.google.inject.Provider;

import javax.inject.Inject;

import java.util.Collections;
import java.util.Set;

/**
 * An implementation of a System defined ComTask
 */
@UniqueComTaskForFirmwareUpgrade(groups = {Save.Create.class, Save.Update.class}, message = "{" + TaskServiceKeys.ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED + "}")
@OnlyOneProtocolTaskIfFirmwareUpgrade(groups = {Save.Create.class, Save.Update.class}, message = "{" + TaskServiceKeys.ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED + "}")
class ComTaskDefinedBySystemImpl extends ComTaskImpl implements SystemComTask {

    @Inject
    ComTaskDefinedBySystemImpl(DataModel dataModel, Thesaurus thesaurus, EventService eventService,
            Provider<BasicCheckTaskImpl> basicCheckTaskProvider, Provider<ClockTaskImpl> clockTaskProvider,
            Provider<LoadProfilesTaskImpl> loadProfilesTaskProvider, Provider<LogBooksTaskImpl> logBooksTaskProvider,
            Provider<MessagesTaskImpl> messagesTaskProvider, Provider<RegistersTaskImpl> registersTaskProvider,
            Provider<StatusInformationTaskImpl> statusInformationTaskProvider,
            Provider<TopologyTaskImpl> topologyTaskProvider,
            Provider<FirmwareManagementTaskImpl> firmwareManagementTaskProvider) {
        super(logBooksTaskProvider, dataModel, statusInformationTaskProvider, messagesTaskProvider,
                basicCheckTaskProvider, registersTaskProvider, eventService, clockTaskProvider, topologyTaskProvider,
                thesaurus, loadProfilesTaskProvider, firmwareManagementTaskProvider);
    }

    @Override
    public FirmwareManagementTask createFirmwareUpgradeTask() {
        FirmwareManagementTaskImpl firmwareUpgradeTask = getFirmwareManagementTaskProvider().get();
        firmwareUpgradeTask.ownedBy(this);
        addProtocolTask(firmwareUpgradeTask);
        return firmwareUpgradeTask;
    }

    @Override
    public boolean isUserComTask() {
        return false;
    }

    @Override
    public boolean isSystemComTask() {
        return true;
    }

    @Override
    public Set<ComTaskUserAction> getUserActions() {
        // cannot be executed on demand from UI
        return Collections.emptySet();
    }

    @Override
    public void setUserActions(Set<ComTaskUserAction> userActions) {
        // do nothing
    }

}
