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

public class TCPIPConnection extends Connection implements DLMSConnection {
    private static final byte DEBUG=0;
    private final long TIMEOUT=300000;
    private final int WRAPPER_VERSION=0x0001;

    // TCPIP specific
    // Sequence numbering
    private boolean boolTCPIPConnected;
    
    private int maxRetries;
    private int clientAddress;
    private int serverAddress;
    int timeout;
    private long forceDelay;
    
    private int iskraWrapper = 0;

    private static final byte CLIENT_NO_SECURITY=0;
    private static final byte CLIENT_LOWLEVEL_SECURITY=1;
    
    private InvokeIdAndPriority invokeIdAndPriority;
    
    public TCPIPConnection(InputStream inputStream,
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
        boolTCPIPConnected=false;
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
       if (boolTCPIPConnected==false) {
          if (hhuSignOn != null) hhuSignOn.signOn("",meterId);
          boolTCPIPConnected=true;
       } // if (boolTCPIPConnected==false)
    } // public void connectMAC() throws IOException
    
/**
 * Method that requests a MAC disconnect for the TCPIP layer.
 * @exception TCPIPConnectionException
 */
    public void disconnectMAC() throws IOException {
       if (boolTCPIPConnected==true) {
           boolTCPIPConnected=false;
       } // if (boolTCPIPConnected==true)
       
    } // public void disconnectMAC() throws IOException
    
    private final int STATE_HEADER_VERSION=0;
    private final int STATE_HEADER_SOURCE=1;
    private final int STATE_HEADER_DESTINATION=2;
    private final int STATE_HEADER_LENGTH=3;
    private final int STATE_DATA=4;
    
    private WPDU receiveData() throws IOException {
        long protocolTimeout,interFrameTimeout;
        int kar;
        int length=0;
        int state=STATE_HEADER_VERSION;
        int count=0;
        WPDU wpdu=null;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        
        interFrameTimeout = System.currentTimeMillis() + timeout;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        
        resultArrayOutputStream.reset();
        copyEchoBuffer();
        
        while(true) {
                if ((kar = readIn()) != -1) {
                   if (DEBUG>=2) ProtocolUtils.outputHex(kar);
                   
                   switch (state) {
                       
                       case STATE_HEADER_VERSION: {
                          if (count==0) {
                              wpdu = new WPDU();
                              wpdu.setVersion(kar);
                              count++;
                          }
                          else {
                              wpdu.setVersion(wpdu.getVersion()*256+kar);
                              count=0;
                              if (wpdu.getVersion() == WRAPPER_VERSION)
                                  state = STATE_HEADER_SOURCE;
                          }
                       } break; // case STATE_HEADER_VERSION
                       
                       case STATE_HEADER_SOURCE: {
                          if (count==0) {
                              wpdu.setSource(kar);
                              count++;
                          }
                          else {
                              wpdu.setSource(wpdu.getSource()*256+kar);
                              count=0;
                              
                              int address = -1;
                              switch(iskraWrapper){
	                              case 0: {address = clientAddress;break;}
	                              case 1: {address = serverAddress;break;}
	                              default: {address = clientAddress;break;}
                              }
                              
                              if (wpdu.getSource() != address)
                                  state = STATE_HEADER_VERSION;
                              else
                                  state = STATE_HEADER_DESTINATION;
                          }
                       } break; // case STATE_HEADER_DESTINATION
                       
                       case STATE_HEADER_DESTINATION: {
                          if (count==0) {
                              wpdu.setDestination(kar);
                              count++;
                          }
                          else {
                              wpdu.setDestination(wpdu.getDestination()*256+kar);
                              count=0;
                              
                              int address = -1;
                              switch(iskraWrapper){
	                              case 0: {address = serverAddress;break;}
	                              case 1: {address = clientAddress;break;}
	                              default: {address = serverAddress;break;}
                              }
                              
                              if (wpdu.getDestination() != address)
                                  state = STATE_HEADER_VERSION;
                              else
                                  state = STATE_HEADER_LENGTH;
                          }
                       } break; // case STATE_HEADER_DESTINATION
                       
                       case STATE_HEADER_LENGTH: {
                          if (DEBUG>=2) System.out.println("KV_DEBUG> receive "+count+ "bytes of data");
                          if (count==0) {
                              wpdu.setLength(kar);
                              count++;
                          }
                          else {
                              wpdu.setLength(wpdu.getLength()*256+kar);
                              count=wpdu.getLength();
                              
                              if (DEBUG>=2) System.out.println("KV_DEBUG> count="+count);

                              // Add padding of 3 bytes to fake the HDLC LLC. Very tricky to reuse all code written in the early days when only HDLC existed...
                              resultArrayOutputStream.write(0);
                              resultArrayOutputStream.write(0);
                              resultArrayOutputStream.write(0);
                              
                              
                              state = STATE_DATA;
                          }
                       } break; // case STATE_HEADER_LENGTH
                       
                       case STATE_DATA: {
                           
                           interFrameTimeout = System.currentTimeMillis() + timeout;
                           
                           resultArrayOutputStream.write(kar);
                           if (--count<=0) {
                               wpdu.setData(resultArrayOutputStream.toByteArray());
                               if (DEBUG>=1) System.out.println("KV_DEBUG> RX-->"+wpdu);
                               return wpdu;
                           }

                       } break; // STATE_DATA STATE_IDLE
                       
                    } // switch (bCurrentState)

                } // if ((iNewKar = readIn()) != -1)

            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ConnectionException("receiveResponse() response timeout error",TIMEOUT_ERROR);
            }
            
            if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {
                throw new ConnectionException("receiveResponse() interframe timeout error",TIMEOUT_ERROR);
            }
                
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
//        try {
//           Thread.sleep(400);
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }
        
        while(true) {
            try {
                WPDU wpdu = new WPDU(clientAddress,serverAddress,byteRequestBuffer);
                if (DEBUG>=1) System.out.println("KV_DEBUG> TX-->"+wpdu);
                sendOut(wpdu.getFrameData());
                return receiveData().getData();
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
    
    class WPDU {
        
       private int version; 
       private int source;
       private int destination;
       private int length;
       private byte[] data;
        
       public WPDU(int source, int destination, byte[] data) {
           this.version = WRAPPER_VERSION;
           this.source=source;
           this.destination=destination;
           this.data=data;
           length = data.length;
       }
       
       
       public WPDU() {
           version=source=destination=length=-1;
       }
       
       public WPDU(byte[] byteBuffer) throws IOException {
           int offset = 0;
           setVersion(ProtocolUtils.getInt(byteBuffer,offset,2));
           offset+=2;
           setSource(ProtocolUtils.getInt(byteBuffer,offset,2));
           offset+=2;
           setDestination(ProtocolUtils.getInt(byteBuffer,offset,2));
           offset+=2;
           setLength(ProtocolUtils.getInt(byteBuffer,offset,2));
           offset+=2;
           setData(ProtocolUtils.getSubArray2(byteBuffer, offset, getLength() - 8));
       }

       public String toString() {
           return ProtocolUtils.outputHexString(data);
       }
       
        public byte[] getFrameData() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeShort(getVersion());
            daos.writeShort(getSource());
            daos.writeShort(getDestination());
            daos.writeShort(getLength());
            daos.write(getData());
//            baos.write(getVersion()%256);
//            baos.write(getVersion()/256);
//            baos.write(getSource()%256);
//            baos.write(getSource()/256);
//            baos.write(getDestination()%256);
//            baos.write(getDestination()/256);
//            baos.write(getLength()%256);
//            baos.write(getLength()/256);
//            baos.write(getData());
            return baos.toByteArray();
        }
        
        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getSource() {
            return source;
        }

        public void setSource(int source) {
            this.source = source;
        }

        public int getDestination() {
            return destination;
        }

        public void setDestination(int destination) {
            this.destination = destination;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    } // class WPDU
    
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