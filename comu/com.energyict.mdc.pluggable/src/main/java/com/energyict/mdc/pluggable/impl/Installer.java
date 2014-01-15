package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;

/**
 * Installs the pluggable bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (15:08)
 */
public class Installer {

    private final DataModel dataModel;
    private final EventService eventService;
    private final Clock clock;

    public Installer(DataModel dataModel, EventService eventService, Clock clock) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.clock = clock;
    }

    public void install(boolean executeDdl, boolean updateOrm, boolean createMasterData) {
        try {
            this.dataModel.install(executeDdl, updateOrm);
            if (createMasterData) {
                this.createMasterData();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        createEventTypes();
    }

    private void createMasterData() {
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
    }

}