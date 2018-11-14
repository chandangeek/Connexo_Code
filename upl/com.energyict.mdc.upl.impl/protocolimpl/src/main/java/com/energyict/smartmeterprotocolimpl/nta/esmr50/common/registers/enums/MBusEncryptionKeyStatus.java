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
public enum MBusEncryptionKeyStatus {
    Status0(0, "no encryption_key"),
    Status1(1, "encryption_key set but not in use by E-meter"),
    Status2(2, "encryption_key transferred"),
    Status3(3, "encryption_key set and transferred to G-meter and in use by E-meter"),
    Status4(4, "encryption_key set and in use by E and G-meter");



    private String description;
    private int id;

    public static String getDescriptionForId(int id) {
        for (MBusEncryptionKeyStatus item : values()) {
            if (item.getId() == id) {
                return item.getDescription();
            }
        }
        return "Reserved ["+id+"]";
    }

    MBusEncryptionKeyStatus(int id, String description) {
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
