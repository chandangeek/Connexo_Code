/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

public enum MulticastICMethods implements DLMSClassMethods {

    SEND_MULTICAST_PACKET(1, 0x10);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    MulticastICMethods(final int methodNumber, final int shortName) {
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

    /**
     * Getter for the ClassId for this object
     *
     * @return the DLMS ClassID
     */
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.MULTICAST_IC;
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