package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

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
public enum MBusFUAKStatus {
    Status0(0, "FUAK status unknown"),
    Status1(1, "FUAK received by E-meter"),
    Status2(2, "FUAK accepted by G-meter"),
    Status3(3, "FUAK revoked by G-meter");

    private String description;
    private int id;

    public static String getDescriptionForId(int id) {
        for (MBusFUAKStatus item : values()) {
            if (item.getId() == id) {
                return item.getDescription();
            }
        }
        return "Reserved ["+id+"]";
    }

    MBusFUAKStatus(int id, String description) {
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
