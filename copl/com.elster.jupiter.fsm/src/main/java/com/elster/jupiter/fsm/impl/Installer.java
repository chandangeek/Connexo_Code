package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Takes the necessary steps to install the technical components of the finite state machine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:57)
 */
public class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final EventService eventService;
    private final FiniteStateMachineService finiteStateMachineService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService, FiniteStateMachineService finiteStateMachineService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for FSM",
                this::createEventTypes,
                logger
        );
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
    }

}