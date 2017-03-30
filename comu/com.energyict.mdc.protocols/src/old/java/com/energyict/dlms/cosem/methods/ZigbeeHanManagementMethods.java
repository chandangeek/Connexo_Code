/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

public enum ZigbeeHanManagementMethods implements DLMSClassMethods {

    BACKUP(1, 0x20),
    RESTORE(2, 0x28),
    CREATE_HAN(3, 0x30),
    IDENTIFY_DEVICE(4, 0x38),
    REMOVE_MIRROR(5, 0x40),
    UPDATE_NETWORK_KEYS(6, 0x48),
    UPDATE_LINK_KEYS(7, 0x50),
    REMOVE_HAN(8, 0x58);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    ZigbeeHanManagementMethods(final int methodNumber, final int shortName) {
        this.methodNumber = methodNumber;
        this.shortName = shortName;
    }

    /**
     * Getter for the method number
     *
     * @return the method number as int
     */
    public int getMethodNumber() {
        return this.methodNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.ZIGBEE_HAN_MANAGEMENT;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }
}
