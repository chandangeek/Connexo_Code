package com.energyict.dlms.mocks;

import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.cosem.StoredValues;

public class MockProtocolLink implements ProtocolLink{
	
	private DLMSConnection connection;
	
	public MockProtocolLink(DLMSConnection dlmsConnection){
		this.connection = dlmsConnection;
	}

	public DLMSConnection getDLMSConnection() {
		return this.connection;
	}

	public Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	public DLMSMeterConfig getMeterConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getReference() {
		return ProtocolLink.LN_REFERENCE;
	}

	public int getRoundTripCorrection() {
		// TODO Auto-generated method stub
		return 0;
	}

	public StoredValues getStoredValues() {
		// TODO Auto-generated method stub
		return null;
	}

	public TimeZone getTimeZone() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRequestTimeZone() {
		// TODO Auto-generated method stub
		return false;
	}

}
