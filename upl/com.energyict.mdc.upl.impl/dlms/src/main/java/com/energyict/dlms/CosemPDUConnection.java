package com.energyict.dlms;

import com.energyict.dialer.connection.*;
import java.io.*;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.protocol.ProtocolUtils;

/**
 * @version  1.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the TCPIP transport layer wrapper protocol.
 */

public class CosemPDUConnection extends Connection implements DLMSConnection {
    private static final byte DEBUG=0;
    private final long TIMEOUT=300000;
    private final int WRAPPER_VERSION=0x0001;

    // TCPIP specific
    // Sequence numbering
    private boolean connected;
    
    private int maxRetries;
    private int clientAddress;
    private int serverAddress;
    int timeout;
    private long forceDelay;
    
    private int iskraWrapper = 0;

    private static final byte CLIENT_NO_SECURITY=0;
    private static final byte CLIENT_LOWLEVEL_SECURITY=1;
    
    private InvokeIdAndPriority invokeIdAndPriority;
    
    public CosemPDUConnection(InputStream inputStream,
                          OutputStream outputStream,
                          int timeout,
                          int forceDelay,
                          int maxRetries,
                          int clientAddress, 
                          int serverAddress) throws IOException {
        super(inputStream, outputStream);
        this.maxRetries = maxRetries;
        this.timeout=timeout;
        this.clientAddress=clientAddress;
        this.serverAddress=serverAddress;
        this.forceDelay = forceDelay;
        connected=false;
        this.invokeIdAndPriority = new InvokeIdAndPriority();
        
    } // public TCPIPConnection(...)

    public int getType() {
        return DLMSConnection.DLMS_CONNECTION_TCPIP;
    }
    
    public void setSNRMType(int type) {
        // absorb...
    }
    
/**
 * Method that requests a MAC connection for the TCPIP layer. this request negotiates some parameters
 * for the buffersizes and windowsizes.
 * @exception TCPIPConnectionException
 */
    public void connectMAC() throws IOException {
       if (connected==false) {
          if (hhuSignOn != null) hhuSignOn.signOn("",meterId);
          connected=true;
       } // if (connected==false)
    } // public void connectMAC() throws IOException
    
/**
 * Method that requests a MAC disconnect for the TCPIP layer.
 * @exception TCPIPConnectionException
 */
    public void disconnectMAC() throws IOException {
       if (connected==true) {
           connected=false;
       } // if (connected==true)
       
    } // public void disconnectMAC() throws IOException
    
    private byte[] receiveData() throws IOException {
        int kar;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        resultArrayOutputStream.reset();
        copyEchoBuffer();
        while(true) {
            if ((kar = readIn()) != -1) {
               if (DEBUG>=2) ProtocolUtils.outputHex(kar);
               resultArrayOutputStream.write(kar);
            } // if ((iNewKar = readIn()) != -1)
            return resultArrayOutputStream.toByteArray();    
        } // while(true)
    } // private byte waitForTCPIPFrameStateMachine()
    
    
	public void setIskraWrapper(int type) {
		
		/**
		 * GN|260208|: Creation of this method to switch the client an server addresses
		 * Should have been correct but Actaris probably switched this and Iskra came last ... 
		 */
		iskraWrapper = type;
	}

/**
 * Method that sends an information data field and receives an information field.
 * @param Data with the information field.
 * @return Response data with the information field.
 * @exception TCPIPConnectionException
 */
    public byte[] sendRequest(byte[] data) throws IOException {
        int retry=0;
        
        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[data.length-3];
        System.arraycopy(data, 3, byteRequestBuffer, 0, byteRequestBuffer.length);
        
        while(true) {
            try {
                sendOut(byteRequestBuffer);
                return receiveData();
            }
            catch (ConnectionException e) {
                if (retry++ >= maxRetries)
                    throw new IOException("sendRequest, IOException, "+com.energyict.cbo.Utils.stack2string(e));
            }
        }

    } // public byte[] sendRequest(byte[] byteBuffer) throws TCPIPConnectionException

    // KV 18092003
    HHUSignOn hhuSignOn=null;
    String meterId="";
    public void setHHUSignOn(HHUSignOn hhuSignOn,String meterId) {
        this.hhuSignOn=hhuSignOn;
        this.meterId=meterId;
    }
    public HHUSignOn getHhuSignOn() {
        return hhuSignOn;   
    }
    
    /********************************************************************************************************
     * Invoke-Id-And-Priority byte setting
     ********************************************************************************************************/
    
    public void setInvokeIdAndPriority(InvokeIdAndPriority iiap){
    	this.invokeIdAndPriority = iiap;
    }
    
    public InvokeIdAndPriority getInvokeIdAndPriority(){
    	return this.invokeIdAndPriority;
    }
} // public class TCPIPConnection