/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.customdlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.methods.DLMSClassMethods;

public enum DSMR4_MbusClientMethods implements DLMSClassMethods {

    SLAVE_INSTALL(1, 0x78),
    SLAVE_DEINSTALL(2, 0x80),
    CAPTURE(3, 0x88),
    RESET_ALARM(4, 0x90),
    SYNCHRONIZE_CLOCK(5, 0x98),
    DATA_SEND(6, 0xA0),
    SET_ENCRYPTION_KEY(7, 0xA8),
    TRANSFER_KEY(8, 0xB0);

    /**
     * The number of the method in chronological order
     */
    private final int methodNumber;
    /**
     * The shortName of this attribute according to BlueBook
     */
    private final int shortName;

    DSMR4_MbusClientMethods(final int methodNumber, final int shortName) {
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
        return DLMSClassId.MBUS_CLIENT;
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
