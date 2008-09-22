package com.energyict.dlms.client;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.dlms.*;
import com.energyict.dlms.cosem.StoredValues;

public class CompoundDataBuilderConnection implements ProtocolLink {

	AdaptorConnection connection = null;
	
	public CompoundDataBuilderConnection() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public DLMSConnection getDLMSConnection() {
		try {
			// TODO Auto-generated method stub
			if (connection == null)
			     connection = new AdaptorConnection();
			return connection;
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public AdaptorConnection getAdaptorConnection() {
		return (AdaptorConnection)connection;
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
		// TODO Auto-generated method stub
		return 0;
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
