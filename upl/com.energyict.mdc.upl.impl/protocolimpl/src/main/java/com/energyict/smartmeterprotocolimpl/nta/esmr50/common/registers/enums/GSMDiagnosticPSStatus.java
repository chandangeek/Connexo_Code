package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers.enums;

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
@Deprecated
public enum GSMDiagnosticPSStatus {
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
        for (GSMDiagnosticPSStatus item : values()) {
            if (item.getId() == id) {
                return item.getDescription();
            }
        }
        return "Reserved ["+id+"]";
    }

    GSMDiagnosticPSStatus(int id, String description) {
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
