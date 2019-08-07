package com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model;

public enum InitiatorElectricalPhaseType {

    NOT_DEFINED(0, "Not defined"),
    PHASE_1(1, "Phase 1"),
    PHASE_2(2, "Phase 2"),
    PHASE_3(3, "Phase 3");

    private int id;
    private String description;

    InitiatorElectricalPhaseType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static String getDescription(int id) {
        for (InitiatorElectricalPhaseType item : values()) {
            if (id == item.id) {
                return item.description;
            }
        }
        throw new IllegalArgumentException("Invalid Initiator Electrical Phase ID: " + id);
    }

}