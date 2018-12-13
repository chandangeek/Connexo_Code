package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

/**
 * Created by iulian on 8/18/2016.
 */

/**
 * cs_attachment
 Indicates the current circuit switched status.
 enum:
 (0) inactive,
 (1) incoming call,
 (2) active,
 (3) ... (255) reserved
 */
public enum LTEDiagnosticCSAttachement {
    Inactive(0, "Inactive"),
    IncomingCall(1, "Incoming Call"),
    Active(2, "Active"),
    ;

    private String description;
    private int id;

    public static String getDescriptionForId(int id) {
        for (LTEDiagnosticCSAttachement item : values()) {
            if (item.getId() == id) {
                return item.getDescription();
            }
        }
        return "Reserved ["+id+"]";
    }

    LTEDiagnosticCSAttachement(int id, String description) {
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
