/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.attributes.DLMSAttributes;

public interface DLMSClassMethods extends DLMSAttributes {

    /**
	 * Getter for the method number
	 *
	 * @return the method number as int
	 */
	int getMethodNumber();

}