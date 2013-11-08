package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.attributes.DLMSAttributes;

/**
 * Interface to provide access to DLMS class <b>Method</b> functionality
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 25-nov-2010<br/>
 * Time: 15:45:46<br/>
 */
public interface DLMSClassMethods extends DLMSAttributes {

    /**
	 * Getter for the method number
	 *
	 * @return the method number as int
	 */
	int getMethodNumber();

}