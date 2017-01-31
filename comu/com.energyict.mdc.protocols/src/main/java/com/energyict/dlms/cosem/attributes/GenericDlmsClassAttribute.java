/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 19/12/11
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class GenericDlmsClassAttribute implements DLMSClassAttributes {

    private final int attributeNumber;
    private final DLMSClassId dlmsClassId;
    private final int shortName;

    public GenericDlmsClassAttribute(final int shortName, final DLMSClassId dlmsClassId, final int attributeNumber) {
        this.shortName = shortName;
        this.dlmsClassId = dlmsClassId;
        this.attributeNumber = attributeNumber;
    }

    public GenericDlmsClassAttribute(final int shortName, final DLMSAttribute dlmsAttribute) {
        this.shortName = shortName;
        this.dlmsClassId = dlmsAttribute.getDLMSClassId();
        this.attributeNumber = dlmsAttribute.getAttribute();
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    /**
     * Getter for the DLMSAttribute
     *
     * @return the short name as int
     */
    public DLMSAttribute getDLMSAttribute(final ObisCode obisCode) {
        return new DLMSAttribute(obisCode, getAttributeNumber(), getDlmsClassId());
    }

    /**
     * Getter for the ClassId for this object
     *
     * @return the DLMS ClassID
     */
    public DLMSClassId getDlmsClassId() {
        return this.dlmsClassId;
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
