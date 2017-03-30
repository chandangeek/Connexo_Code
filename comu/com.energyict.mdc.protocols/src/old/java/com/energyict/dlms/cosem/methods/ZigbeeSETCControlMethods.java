/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

public enum ZigbeeSETCControlMethods implements DLMSClassMethods {

    REGISTER_DEVICE(1, 0x18),
    UNREGISTER_DEVICE(2, 0x20),
    UNREGISTER_ALL_DEVICES(3, 0x28);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    ZigbeeSETCControlMethods(final int methodNumber, final int shortName) {
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
        return DLMSClassId.ZIGBEE_SETC_CONTROL;
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
