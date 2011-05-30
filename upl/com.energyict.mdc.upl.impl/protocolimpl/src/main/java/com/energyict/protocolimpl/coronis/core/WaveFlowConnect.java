package com.energyict.protocolimpl.coronis.core;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.*;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.*;
import java.util.logging.Logger;

public class WaveFlowConnect extends Connection implements ProtocolConnection, ProtocolStackLink {

	
	private final int timeout;
	private final Logger logger;
	private final long forceDelay;
	private final int retries;

	
	
	/**
	 * Reference to the escape command factory. this factory allows calling
	 * wavenis protocolstack specific commands if implemented...  
	 */
	private EscapeCommandFactory escapeCommandFactory;
	
	final public EscapeCommandFactory getEscapeCommandFactory() {
		return escapeCommandFactory;
	}

	public WaveFlowConnect(final InputStream inputStream, final OutputStream outputStream, final int timeout, final Logger logger, final long forceDelay, final int retries) throws ConnectionException {
		
		super(inputStream,outputStream,forceDelay,0);
		
		this.timeout = timeout;
		this.logger = logger;
		this.forceDelay = forceDelay;
		this.retries=retries;
		escapeCommandFactory = new EscapeCommandFactory(this);		
	}

	public MeterType connectMAC(String strID, String strPassword,int securityLevel, String nodeId) throws IOException,ProtocolConnectionException {
		
		// Set the RF response timeout
		escapeCommandFactory.setWavenisStackConfigRFResponseTimeout(timeout);
		
		return null;
	}

	public byte[] dataReadout(String strID, String nodeId)
			throws NestedIOException, ProtocolConnectionException {
		return null;
	}

	public void disconnectMAC() throws NestedIOException,
			ProtocolConnectionException {
	}

	public HHUSignOn getHhuSignOn() {
		return null;
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn) {
	}

    public byte[] readRadioAddress() throws IOException {
        return escapeCommandFactory.getRadioAddress();
    }

	public byte[] sendEscapeData(byte[] bs) throws NestedIOException, ConnectionException {
		int retry=0;
		delay(forceDelay);
		
		while(true) {
			try {
				sendOut(bs);
				break;
			}
			catch(ConnectionException e) {

				if (e.getMessage().indexOf("WavenisStackInterruptedException") >=0 ) {
					throw new NestedIOException(new InterruptedException());
				}
				
				if (e.getMessage().indexOf("error Timeout ")>=0) {
					if (retry++ >= retries) {
						throw new ConnectionException("After 3 retries, "+e.getMessage());
					}
					
					logger.warning("Waveflow connect, sendData() retry ["+retry+"] with timeout ["+(timeout*(retry+1))+"] ms");
				}
				else {
					throw e;
				}
			}
		}
		
		long timeoutInterFrame = System.currentTimeMillis()+timeout;
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
	
	public byte[] sendData(byte[] bs) throws NestedIOException, ConnectionException  {

		int communicationAttemptNr=0;
		int retry=0;
		delay(forceDelay);
		
		
		//System.out.println("TX : "+ProtocolUtils.outputHexString(bs));
		
		while(true) {
			try {
				escapeCommandFactory.setWavenisStackCommunicationAttemptNr(communicationAttemptNr);
			} catch (IOException e1) {
				logger.severe("Error setting the communicationAttemptNr in the wavenis stack!");
			}
			
			try {
				sendOut(bs);
				break;
			}
			catch(ConnectionException e) {
				
				if (e.getMessage().indexOf("WavenisStackInterruptedException") >=0 ) {
					throw new NestedIOException(new InterruptedException());
				}
				// Only increment communicationAttemptNr after a timeout exception
				if ((e.getMessage().indexOf("WavenisProtocolTimeoutException") >=0 ) || (e.getMessage().indexOf("WavenisProtocolControlTimeoutException") >=0 )) {
					communicationAttemptNr++;
				}
				
				if (retry++ >= retries) {
					throw new ConnectionException("After ["+retries+"] retries, "+e.getMessage());
				}
				else {
					logger.warning("Waveflow connect, sendData() retry ["+communicationAttemptNr+"] timeout ["+(timeout*(communicationAttemptNr+1))+"] ms");
				}
				
			} // catch(ConnectionException e)
			
		} // while(true)
		
		
		long timeoutInterFrame = System.currentTimeMillis()+timeout;
		copyEchoBuffer();
		byte[] data=null;
		do {
			delay(100);
			data = readInArray();
			
            if (((long) (System.currentTimeMillis() - timeoutInterFrame)) > 0) {
                throw new ProtocolConnectionException("WaveFlowConnect, interframe timeout error",TIMEOUT_ERROR);
            }			
			
		} while(data==null);
		
		//System.out.println("RX : "+ProtocolUtils.outputHexString(data));

		return data;
	}

	public Logger getLogger() {
		return logger;
	}
}