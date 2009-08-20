/*
 * StoredValues.java
 *
 * Created on 13 oktober 2004, 15:29
 */

package com.energyict.dlms.cosem;

import java.io.IOException;
import java.util.Date;

import com.energyict.obis.ObisCode;

/**
 * 
 * @author Koen
 */
public interface StoredValues {

	HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException;

	Date getBillingPointTimeDate(int billingPoint) throws IOException;

	int getBillingPointCounter() throws IOException;

	void retrieve() throws IOException;

	ProfileGeneric getProfileGeneric();

}
