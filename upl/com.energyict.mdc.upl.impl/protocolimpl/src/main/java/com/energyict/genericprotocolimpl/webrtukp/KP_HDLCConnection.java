package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.protocolimpl.dlms.HDLCConnection;

public class KP_HDLCConnection extends HDLCConnection{

	public KP_HDLCConnection(InputStream inputStream,
			OutputStream outputStream, int timeout, long forceDelay,
			int maxRetries, int clientMacAddress, int serverLowerMacAddress,
			int serverUpperMacAddress, int addressingMode)
			throws DLMSConnectionException, ConnectionException {
		super(inputStream, outputStream, timeout, forceDelay, maxRetries,
				clientMacAddress, serverLowerMacAddress, serverUpperMacAddress,
				addressingMode);
	}
	
	/**
     * Method that requests a MAC connection for the HDLC layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     * 
     * The KP Module requires a special signon procedure. The initial baudrate must be 9600 instead of 300
     * The rest is the same
     * 
     * @exception DLMSConnectionException
     */
    public void connectMAC() throws IOException,DLMSConnectionException {
    	
    	byte[] SNRMFrame;
       if (boolHDLCConnected==false) {
           try {
        	   if (hhuSignOn != null) hhuSignOn.signOn("",meterId,5);
           }
           catch(ConnectionException e) {
               throw new DLMSConnectionException("connectMAC, HHU signOn failed, ConnectionException, "+e.getMessage());  
           }
           
           if ( SNRMType == 1 )
        	   SNRMFrame = flexSNRMFrame;
           else
        	   SNRMFrame = macSNRMFrame;

           byte[] macFrame = buildFrame(SNRMFrame);
           
           doConnectMAC(macFrame);
       }
    }

}
