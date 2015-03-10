package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.inject.Provider;

import javax.inject.Inject;

/**
 * Serves as an implementation for a ComTask that is defined by the User
 */
public class ComTaskDefinedByUserImpl extends ComTaskImpl {

    @Inject
    public ComTaskDefinedByUserImpl(Provider<LogBooksTaskImpl> logBooksTaskProvider,
                                    DataModel dataModel,
                                    Provider<StatusInformationTaskImpl> statusInformationTaskProvider,
                                    Provider<MessagesTaskImpl> messagesTaskProvider,
                                    Provider<BasicCheckTaskImpl> basicCheckTaskProvider,
                                    Provider<RegistersTaskImpl> registersTaskProvider,
                                    EventService eventService,
                                    Provider<ClockTaskImpl> clockTaskProvider,
                                    Provider<TopologyTaskImpl> topologyTaskProvider,
                                    Thesaurus thesaurus,
                                    Provider<LoadProfilesTaskImpl> loadProfilesTaskProvider) {
        super(logBooksTaskProvider, dataModel, statusInformationTaskProvider, messagesTaskProvider, basicCheckTaskProvider, registersTaskProvider, eventService, clockTaskProvider, topologyTaskProvider, thesaurus, loadProfilesTaskProvider);
    }

    @Override
    public boolean isUserComTask() {
        return true;
    }

    @Override
    public boolean isSystemComTask() {
        return false;
    }

}
