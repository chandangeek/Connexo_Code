/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSAttribute;

/**
 * Interface to provide access to DLMS class <b>Attributes</b> functionality
 *
 * @author jme
 *
 */
public interface DLMSClassAttributes extends DLMSAttributes {

	/**
	 * Getter for the attribute number
	 *
	 * @return the attribute number as int
	 */
	int getAttributeNumber();

    /**
     * Getter for the DLMSAttribute
     *
     * @return the short name as int
     */
    DLMSAttribute getDLMSAttribute(ObisCode obisCode);

}
