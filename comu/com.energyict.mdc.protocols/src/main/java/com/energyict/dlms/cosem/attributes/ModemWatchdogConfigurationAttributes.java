/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum ModemWatchdogConfigurationAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    CONFIG_PARAMETERS(2, 0x08),
    IS_ENABLED(3, 0x10);

    private final int attributeNumber;
    private final int shortName;

    private ModemWatchdogConfigurationAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.MODEM_WATCHDOG_SETUP;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public static ModemWatchdogConfigurationAttributes findByAttributeNumber(int attribute) {
        for (ModemWatchdogConfigurationAttributes limiterAttribute : ModemWatchdogConfigurationAttributes.values()) {
            if (limiterAttribute.getAttributeNumber() == attribute) {
                return limiterAttribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attribute);
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}