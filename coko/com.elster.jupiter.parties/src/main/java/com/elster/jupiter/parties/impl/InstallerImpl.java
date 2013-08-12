package com.elster.jupiter.parties.impl;

public class InstallerImpl {	
	
	public void install(boolean executeDdl , boolean updateOrm , boolean createMasterData) {
        try {
            Bus.getOrmClient().install(executeDdl, updateOrm);
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
            eventType.install();
        }
    }

}
