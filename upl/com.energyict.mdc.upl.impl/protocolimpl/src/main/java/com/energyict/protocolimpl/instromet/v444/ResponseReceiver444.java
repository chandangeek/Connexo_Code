package com.energyict.protocolimpl.instromet.v444;

import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.instromet.connection.InstrometConnection;
import com.energyict.protocolimpl.instromet.connection.ResponseReceiver;

public class ResponseReceiver444 extends ResponseReceiver {
	
	public ResponseReceiver444(InstrometConnection instrometConnection, int timeout) {
    	super(instrometConnection, timeout);
    }
	
	protected int getStatusAddressValue(byte[] statusAddress) throws IOException {
		return ProtocolUtils.getIntLE(statusAddress, 0, 4);
	}
	
	protected int getStartAddress(byte[] startAddress) throws IOException {
		return ProtocolUtils.getIntLE(startAddress, 0, 4);
	}
	
	protected int getDataLengthValue(byte[] datalength) throws IOException  {
		return ProtocolUtils.getIntLE(datalength, 0, 2);
	}

}
