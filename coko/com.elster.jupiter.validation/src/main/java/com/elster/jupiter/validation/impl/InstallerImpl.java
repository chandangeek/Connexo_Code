package com.elster.jupiter.validation.impl;

public class InstallerImpl {

    public void install(boolean executeDdl, boolean updateOrm) {
        try {
            Bus.getOrmClient().install(executeDdl, updateOrm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install();
        }
    }

}
