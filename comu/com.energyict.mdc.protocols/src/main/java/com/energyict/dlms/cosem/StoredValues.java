/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * StoredValues.java
 *
 * Created on 13 oktober 2004, 15:29
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NotInObjectListException;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public interface StoredValues {

	HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException;

	Date getBillingPointTimeDate(int billingPoint) throws IOException;

	int getBillingPointCounter() throws IOException;

	void retrieve() throws IOException;

	ProfileGeneric getProfileGeneric() throws NotInObjectListException;

}
