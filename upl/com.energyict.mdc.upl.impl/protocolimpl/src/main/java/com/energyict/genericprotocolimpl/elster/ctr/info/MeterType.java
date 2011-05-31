package com.energyict.genericprotocolimpl.elster.ctr.info;

/**
 * Copyrights EnergyICT
 * Date: 17/02/11
 * Time: 10:20
 */
public enum MeterType {

    MASS("Mass"),
    USON("Ultrasonic"),
    CORI("Coriolis"),
    VENT("Venturi"),
    MEMB("Membrane"),
    TURB("Turbine"),
    ROTO("Rotary"),
    Axxx("Other or generic"),
    INVALID("Invalid convertor type"),
    UNKNOWN("Unknown meter type");

    /**
     * A human readable description of the MeterType
     */
    private final String description;

    private MeterType(String description) {
        this.description = description;
    }

    /**
     * Get a MeterType from a given string.
     *
     * @param typeName the name of the MeterType
     * @return the MeterType or INVALID if not found
     */
    public static MeterType fromString(String typeName) {
        if ((typeName != null) && (typeName.length() > 0)) {
            for (MeterType type : values()) {
                if (type.name().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
        }
        return INVALID;
    }

    /**
     * This method checks if the MeterType is a valid value.
     * With a valid value we mean a value that could be read or written to a device.
     * Not valid: INVALID and UNKNOWN
     *
     * @return true if valid
     */
    public boolean isValid() {
        return !(equals(INVALID) || equals(UNKNOWN));
    }

}
