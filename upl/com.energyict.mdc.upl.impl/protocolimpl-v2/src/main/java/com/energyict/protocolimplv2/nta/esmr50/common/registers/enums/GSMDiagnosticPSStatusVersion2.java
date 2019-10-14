package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

/**
 * Created by Paul van Minderhout on 10/07/2019
 */

import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.GSMDiagnosticPSStatusVersion1;

/**

 GSM diagnostics (class_id: 47, version: 2)

 ps_status	The ps_status value field indicates the packet switched status of the modem.
 enum:
 (0)			inactive,
 (1)			GPRS,
 (2)			EDGE,
 (3)			UMTS,
 (4)			HSDPA,
 (5)			LTE,
 (6)			CDMA,
 (7)			LTE Cat M1
 (8)			LTE NB-IoT
 (9)	...(255) reserved

 */
public enum GSMDiagnosticPSStatusVersion2 {
    LTE_CAT_M1(7, "LTE Cat M1"),
    LTE_NB_IOT(8, "LTE NB-IoT");

    private String description;
    private int id;

    public static String getDescriptionForId(int id) {
        for (GSMDiagnosticPSStatusVersion1 item : GSMDiagnosticPSStatusVersion1.values()) {
            if (item.getId() == id) {
                return item.getDescription();
            }
        }
        for (GSMDiagnosticPSStatusVersion2 item : values()) {
            if (item.getId() == id) {
                return item.getDescription();
            }
        }
        return "Reserved ["+id+"]";
    }

    GSMDiagnosticPSStatusVersion2(int id, String description) {
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
