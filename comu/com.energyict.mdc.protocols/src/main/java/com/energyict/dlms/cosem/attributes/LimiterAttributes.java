/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

public enum LimiterAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x00),
    MONITORED_VALUE(2, 0x08),
    THRESHOLD_ACTIVE(3, 0x10),
    THRESHOLD_NORMAL(4, 0x18),
    THRESHOLD_EMERGENCY(5, 0x20),
    MIN_OVER_THRESHOLD_DURATION(6, 0x28),
    MIN_UNDER_THRESHOLD_DURATION(7, 0x30),
    EMERGENCY_PROFILE(8, 0x38),
    EMERGENCY_PROFILE_GROUP_ID_LIST(9, 0x40),
    EMERGENCY_PROFILE_ACTIVE(10, 0x48),
    ACTIONS(11, 0x50);


    private final int attributeNumber;
    private final int shortName;

    private LimiterAttributes(int attrNr, int sn) {
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
        return DLMSClassId.LIMITER;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public static LimiterAttributes findByAttributeNumber(int attribute){
        for(LimiterAttributes limiterAttribute : LimiterAttributes.values()){
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
