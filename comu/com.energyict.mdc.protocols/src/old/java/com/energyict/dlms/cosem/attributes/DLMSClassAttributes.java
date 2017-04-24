package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.obis.ObisCode;

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
