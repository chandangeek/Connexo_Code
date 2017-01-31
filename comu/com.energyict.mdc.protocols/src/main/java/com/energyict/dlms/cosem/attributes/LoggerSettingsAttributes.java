/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;


public enum LoggerSettingsAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    SERVER_LOG_LEVEL(2, 0x08),
    WEB_PORTAL_LOG_LEVEL(3, 0x10);

    private final int attributeNumber;
    private final int shortName;

    private LoggerSettingsAttributes(int attrNr, int sn) {
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
        return DLMSClassId.LOGGER_SETTINGS;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }
    
    public static LoggerSettingsAttributes findByAttributeNumber(int attribute){
        for(LoggerSettingsAttributes limiterAttribute : LoggerSettingsAttributes.values()){
            if(limiterAttribute.getAttributeNumber() == attribute){
                return limiterAttribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attribute);
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}
