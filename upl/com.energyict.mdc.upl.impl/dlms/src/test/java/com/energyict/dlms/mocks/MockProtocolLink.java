package com.energyict.dlms.mocks;

import com.energyict.dlms.*;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.StoredValues;

import java.util.TimeZone;
import java.util.logging.Logger;

public class MockProtocolLink implements ProtocolLink{
	
	private DLMSConnection connection;
	private DLMSMeterConfig meterConfig;
    private int reference;
	
	public MockProtocolLink(DLMSConnection dlmsConnection){
		this(dlmsConnection, null);
	}
	
	public MockProtocolLink(DLMSConnection dlmsConnection, DLMSMeterConfig meterConfig){
		this.connection = dlmsConnection;
		this.meterConfig = meterConfig;
        this.reference = ProtocolLink.LN_REFERENCE;
	}

    @Override
    public ApplicationServiceObject getAso() {
        return null;
    }

	public DLMSConnection getDLMSConnection() {
		return this.connection;
	}

	public Logger getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	public DLMSMeterConfig getMeterConfig() {
		return meterConfig;
	}

	public int getReference() {
		return reference;
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

    public void setReference(int reference){
        this.reference = reference;
    }
}
