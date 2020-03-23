package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

/**
 * Created by iulian on 8/18/2016.
 */

/**
 * ps_status
 The ps_status value field indicates the packet switched status of the modem.
 enum:
 (0) inactive,
 (1) GPRS,
 (2) EDGE,
 (3) UMTS,
 (4) HSDPA,
 (5) LTE,
 (6) CDMA,
 (7) ... (255) reserved
 */
public enum GSMDiagnosticPSStatusVersion1 {
    Inactive(0, "Inactive"),
    GPRS(1, "GPRS"),
    EDGE(2, "EDGE"),
    UMTS(3, "UMTS"),
    HSDPA(4, "HSDPA"),
    LTE(5, "LTE"),
    CDMA(6, "CDMA");

    private String description;
    private int id;

    public static String getDescriptionForId(int id) {
        for (GSMDiagnosticPSStatusVersion1 item : values()) {
            if (item.getId() == id) {
                return item.getDescription();
            }
        }
        return "Reserved ["+id+"]";
    }

    GSMDiagnosticPSStatusVersion1(int id, String description) {
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
