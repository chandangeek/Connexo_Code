package com.elster.insight.usagepoint.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;

public class Installer {	
	
	final private DataModel dataModel;
	final private EventService eventService;
	
	Installer(DataModel dataModel,EventService eventService) {
		this.dataModel = dataModel;
		this.eventService = eventService;
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
