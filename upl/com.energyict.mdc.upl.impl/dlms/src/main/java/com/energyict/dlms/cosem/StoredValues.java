/*
 * StoredValues.java
 *
 * Created on 13 oktober 2004, 15:29
 */

package com.energyict.dlms.cosem;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.NotInObjectListException;

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
