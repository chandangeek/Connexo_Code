package com.elster.jupiter.validation.impl;

public class InstallerImpl {

    private static final int SLOT_COUNT = 8;
    private static final int MONTHS_PER_YEAR = 12;

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
