package com.elster.jupiter.validation.impl;

public class InstallerImpl {

    public void install(boolean executeDdl, boolean updateOrm) {
        Bus.getOrmClient().install(executeDdl, updateOrm);
        createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install();
        }
    }

}
