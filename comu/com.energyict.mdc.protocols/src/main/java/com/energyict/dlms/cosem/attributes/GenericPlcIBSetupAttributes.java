/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Generic PIB setup attributes
 *
 * @author jme
 */
public enum GenericPlcIBSetupAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0),
    READ_RAW_IB(2, 8);

    /**
     * Attribute ID.
     */
    private final int attributeId;

    /**
     * The short name of the attribute (offset from base address).
     */
    private final int shortName;

    private GenericPlcIBSetupAttributes(final int attributeId, final int shortName) {
        this.attributeId = attributeId;
        this.shortName = shortName;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.GENERIC_PLC_IB_SETUP;
    }

    /**
     * {@inheritDoc}
     */
    public final int getShortName() {
        return this.shortName;
    }

    /**
     * {@inheritDoc}
     */
    public final int getAttributeNumber() {
        return this.attributeId;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}
