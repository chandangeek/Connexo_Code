package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

/**
 * Copyrights EnergyICT
 * Date: 1-okt-2010
 * Time: 13:52:26
 */
public enum StructureType {

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
