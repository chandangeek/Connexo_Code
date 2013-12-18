package com.elster.jupiter.parties.impl;

import com.elster.jupiter.events.EventService;

public class InstallerImpl {	
	
	final private OrmClient ormClient;
	final private EventService eventService;
	
	InstallerImpl(OrmClient client,EventService eventService) {
		this.ormClient = client;
		this.eventService = eventService;
	}
	
	public void install(boolean executeDdl , boolean updateOrm , boolean createMasterData) {
        try {
            ormClient.install(executeDdl, updateOrm);
            if (createMasterData) {
                createMasterData();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
