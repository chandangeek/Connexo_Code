package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.net.ConnectException;
import java.util.logging.Logger;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.*;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870ConnectionException;

public class WaveFlowConnect extends Connection implements ProtocolConnection {

	
	private final int timeout;
	private final Logger logger;
	private final long forceDelay;
	
	public WaveFlowConnect(final InputStream inputStream, final OutputStream outputStream, final int timeout, final Logger logger, final long forceDelay) throws ConnectionException {
		
		super(inputStream,outputStream,forceDelay,0);
		
		this.timeout = timeout;
		this.logger = logger;
		this.forceDelay = forceDelay;
		
				
	}

	public MeterType connectMAC(String strID, String strPassword,
			int securityLevel, String nodeId) throws IOException,
			ProtocolConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] dataReadout(String strID, String nodeId)
			throws NestedIOException, ProtocolConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	public void disconnectMAC() throws NestedIOException,
			ProtocolConnectionException {
		// TODO Auto-generated method stub
		
	}

	public HHUSignOn getHhuSignOn() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn) {
		// TODO Auto-generated method stub
		
	}

	public byte[] sendData(byte[] bs) throws NestedIOException, ConnectionException {
		
		
		delay(forceDelay);
		
		long timeoutInterFrame = System.currentTimeMillis()+timeout;
		
		
		sendOut(bs);
		copyEchoBuffer();
		byte[] data=null;
		do {
			delay(100);
			data = readInArray();
			
            if (((long) (System.currentTimeMillis() - timeoutInterFrame)) > 0) {
                throw new ProtocolConnectionException("WaveFlowConnect, interframe timeout error",TIMEOUT_ERROR);
            }			
			
		} while(data==null);
		
		return data;
	}
	
	

	
	
	
	
}
