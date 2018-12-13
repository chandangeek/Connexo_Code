package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

/**
 * Created by iulian on 8/18/2016.
 */

/**
 * status
 (0) Image transfer not initiated,
 (1) Image transfer initiated,
 (2) Image verification initiated,
 (3) Image verification successful,
 (4) Image verification failed,
 (5) Image activation initiated,
 (6) Image activation successful,
 (7) Image activation failed
 */
public enum MBusFWTransferStatus {
    Status0(0, "Image transfer not initiated"),
    Status1(1, "Image transfer initiated"),
    Status2(2, "Image verification initiated"),
    Status3(3, "Image verification successful"),
    Status4(4, "Image verification failed"),
    Status5(5, "Image activation initiated"),
    Status6(6, "Image activation successful"),
    Status7(7, "Image activation failed") ;

    private String description;
    private int id;

    public static String getDescriptionForId(int id) {
        for (MBusFWTransferStatus item : values()) {
            if (item.getId() == id) {
                return item.getDescription();
            }
        }
        return "Reserved ["+id+"]";
    }

    MBusFWTransferStatus(int id, String description) {
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
