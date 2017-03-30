/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

public enum StructureType {

    IDENTIFICATION(0x30, "Identification structure"),
    IDENTIFICATION2(0x31, "Identification structure 2"),
    DECF(0x35, "Table DECF"),
    INVALID_STRUCTURECODE(0xFF, "Invalid structure code");

    private final int structureCode;
    private final String description;

    private StructureType(int structureCode, String description) {
        this.structureCode = structureCode;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getStructureCode() {
        return structureCode;
    }

    public static StructureType fromStructureCode(int code) {
        for (StructureType type : StructureType.values()) {
            if (type.getStructureCode() == (code & 0x7F)) {
                return type;
            }
        }
        return INVALID_STRUCTURECODE;
    }

}
