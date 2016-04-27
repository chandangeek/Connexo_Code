package com.elster.jupiter.parties.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;

public class Installer implements FullInstaller {
	
	final private DataModel dataModel;
	final private EventService eventService;

    @Inject
	Installer(DataModel dataModel,EventService eventService) {
		this.dataModel = dataModel;
		this.eventService = eventService;
	}

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        try {
            dataModelUpgrader.upgrade(dataModel, Version.latest());
            createMasterData();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        createEventTypes();
    }

    public void install(boolean executeDdl , boolean updateOrm , boolean createMasterData) {
        try {
            dataModel.install(executeDdl, updateOrm);
            if (createMasterData) {
                createMasterData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        createEventTypes();
    }
	
	private void createMasterData() {
	}

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

}
