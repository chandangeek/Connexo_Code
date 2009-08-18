/*
 * ProtocolLink.java
 *
 * Created on 18 augustus 2004, 12:01
 */

package com.energyict.dlms;

import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.dlms.cosem.StoredValues;
/**
 *
 * @author  Koen
 */
public interface ProtocolLink {

	int SN_REFERENCE=1;
	int LN_REFERENCE=0;

	DLMSConnection getDLMSConnection();
	DLMSMeterConfig getMeterConfig();
	TimeZone getTimeZone();
	boolean isRequestTimeZone();
	int getRoundTripCorrection();
	Logger getLogger();
	int getReference();
	StoredValues getStoredValues();

}
