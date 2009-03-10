package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.protocolimpl.dlms.HDLCConnection;

public class KPHDLCConnection extends HDLCConnection{


	
	public KPHDLCConnection(InputStream inputStream, OutputStream outputStream,
			int timeout, long forceDelay, int maxRetries, int clientMacAddress,
			int serverLowerMacAddress, int serverUpperMacAddress,
			int addressingMode, int informationFieldSize) throws DLMSConnectionException,
			ConnectionException {
		super(inputStream, outputStream, timeout, forceDelay, maxRetries,
				clientMacAddress, serverLowerMacAddress, serverUpperMacAddress,
				addressingMode, informationFieldSize);
	}

	/**
     * Method that requests a MAC connection for the HDLC layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     * 
     * The KP meter required a specific signOn procedure where the baudrate had to start at 9600 instead of 300
     * 
     * @exception DLMSConnectionException
     */
    public void connectMAC() throws IOException,DLMSConnectionException {
    	
    	byte[] SNRMFrame;
       if (boolHDLCConnected==false) {
           try {
              if (hhuSignOn != null){
            	  hhuSignOn.signOn("",meterId,5);
              }
           }
           catch(ConnectionException e) {
               throw new DLMSConnectionException("connectMAC, HHU signOn failed, ConnectionException, "+e.getMessage());  
           }
           
           if ( SNRMType == 1 ){
        	   SNRMFrame = flexSNRMFrame;
           } else {
        	   SNRMFrame = macSNRMFrame;
           }

           byte[] macFrame = buildFrame(SNRMFrame);
           
           doConnectMAC(macFrame);
       }
    }

}
