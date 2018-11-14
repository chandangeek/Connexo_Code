package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers.enums;

/**
 * Created by iulian on 8/18/2016.
 */

/**
 * status
 Indicates the registration status of the modem.
 enum:
 (0) not registered,
 (1) registered, home network,
 (2) not registered, but MT is currently searching a new operator to register to,
 (3) registration denied,
 (4) unknown,
 (5) registered, roaming
 (6) ... (255) reserved
 */
@Deprecated
public enum LTEDiagnosticStatus {
    NotRegistered(0, "Not registered"),
    RegisteredHome(1, "Registered, home network"),
    NotRegisteredButSearching(2, "Not registered, but MT is currently searching a new operator to register to"),
    RegistrationDenied(3, "Registration denied"),
    Unknown(4, "Unknown"),
    RegisteredRoaming(5,"Registered, roaming");



    private String description;
    private int id;

    public static String getDescriptionForId(int id) {
        for (LTEDiagnosticStatus item : values()) {
            if (item.getId() == id) {
                return item.getDescription();
            }
        }
        return "Reserved ["+id+"]";
    }

    LTEDiagnosticStatus(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return getId()+"="+getDescription();
    }

    public String getDescription() {
        return description;
    }
}
