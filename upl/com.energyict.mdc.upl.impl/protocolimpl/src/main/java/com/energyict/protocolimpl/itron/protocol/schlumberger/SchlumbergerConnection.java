/*
 * schlumbergerConnection.java
 *
 * Created on 7 september 2006, 11:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol.schlumberger;

import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import java.io.*;

/**
 *
 * @author Koen
 */
public class SchlumbergerConnection extends Connection  implements ProtocolConnection {
    
    private static final int DEBUG=0;
    private static final long TIMEOUT=60000;
    
    int timeout;
    int maxRetries;
    long forcedDelay;
    ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();
    private String nodeId;
    int securityLevel;
    
    
    
    /** Creates a new instance of DGCOMConnection */
    public SchlumbergerConnection(InputStream inputStream,
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController,
            int securityLevel) throws ConnectionException {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeout = timeout;
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
        this.securityLevel=securityLevel;
        
    } // EZ7Connection(...)
    
    public com.energyict.protocol.meteridentification.MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws java.io.IOException, ProtocolConnectionException {
        this.setNodeId(nodeId);
        return null;
    }
    
    public byte[] dataReadout(String strID, String nodeId) throws com.energyict.cbo.NestedIOException, ProtocolConnectionException {
        return null;
    }
    
    public void disconnectMAC() throws com.energyict.cbo.NestedIOException, ProtocolConnectionException {
    }
    
    public HHUSignOn getHhuSignOn() {
        return null;
    }
    
    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }
    
    public void delayAndFlush(int delay) throws IOException {
        super.delayAndFlush(delay);
    }
    
    private void doSendCommand(Command command) throws ConnectionException,IOException {
        txOutputStream.reset();
        int checksum=0;
        if (command.getCommand() != 0)
            txOutputStream.write(command.getCommand());
        if (command.getData() != null)
            txOutputStream.write(command.getData());
        if (command.getCommand() != (char)ENQ) {
            int crc = CRCGenerator.calcCRCCCITTSchlumberger(txOutputStream.toByteArray());
            txOutputStream.write((crc>>8)&0xFF);
            txOutputStream.write(crc&0xFF);
        }
    } // void sendData(byte[] cmdData) throws ConnectionException
        
    private void sendFrame() throws ConnectionException {
        sendOut(txOutputStream.toByteArray());
    }
    
    public void sendEnqMultidrop(int nrOfEnqs) throws IOException {
        sendEnqMultidrop(nrOfEnqs, -1);
    }
    
    public void sendEnqMultidrop(int nrOfEnqs,int slaveId) throws IOException {
        int retry=0;
        if (slaveId != -1) { 
            delayAndFlush(1600);
            // SlaveId 0,1,2 
            int slave = slaveId | 0x08; 
            sendOut(new byte[]{(byte)0x58,(byte)slave,(byte)0x59,(byte)(slave^0xFF)});
            receiveUntilTimeout();
        }
        while(true) {
            try {
                delayAndFlush(1000);
                for (int i=0;i<nrOfEnqs;i++) {
                    sendOut((byte)0x05);
                    receiveResponse(new Command((char)0x05),800);
                }
                return;
            } catch(ConnectionException e) {
                if (DEBUG>=1) e.printStackTrace();
                if (e.getReason() == PROTOCOL_ERROR)
                    throw new ProtocolConnectionException("sendCommand() error, "+e.getMessage());
                else {
                    
                    //System.out.println("KV_DEBUG> timeout "+retry);
                    
                    if (retry++>=5) {
                        //return;
                        
                        throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage());
                    }
                }
            }
        } // while(true)
    }
    
    // continue receiving until 1500 ms timeout receiving garbage
    public void receiveUntilTimeout() throws IOException {
        long receiveTimeout;
        int kar;
        
        receiveTimeout = System.currentTimeMillis() + 1500;
        while(true) {
            
            if ((kar = readIn()) != -1) {        
                receiveTimeout = System.currentTimeMillis() + 1500;
            }
            
            if (((long) (System.currentTimeMillis() - receiveTimeout)) > 0) {
                return;
            }            
        }
    } // public void receiveUntilTimeout() throws IOException
    
    public Response sendCommand(Command command) throws IOException {
        int retry=0;
        doSendCommand(command);
        while(true) {
            try {
                delayAndFlush(forcedDelay); // KV_DEBUG
                sendFrame();
                Response response = receiveResponse(command);
                return response;
            } catch(ConnectionException e) {
                if (DEBUG>=1) e.printStackTrace();
                if (e.getReason() == PROTOCOL_ERROR)
                    throw new ProtocolConnectionException("sendCommand() error, "+e.getMessage());
                else {
                    if (retry++>=maxRetries) {
                        throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage());
                    }
                }
            }
        } // while(true)
    }
    
    

    
    private final int WAIT_FOR_CONTROL=0;
    private final int WAIT_FOR_DATA=1;
    private final int WAIT_FOR_CRC=2;
    
            
    public Response receiveResponse(Command command) throws IOException {
        return receiveResponse(command,-1);
    }
    public Response receiveResponse(Command command,long timeoutEnq) throws IOException {
        long protocolTimeout,interFrameTimeout;
        int kar;
        int count=0;
        int state = WAIT_FOR_CONTROL;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        
        
        interFrameTimeout = System.currentTimeMillis() + (timeoutEnq==-1?timeout:timeoutEnq);
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        
        
        if (DEBUG>=1) System.out.println("KV_DEBUG> timeout="+timeout);
        
        resultArrayOutputStream.reset();
        
        copyEchoBuffer();
        while(true) {
            
            if ((kar = readIn()) != -1) {
                
                if (DEBUG >= 2) {
                    System.out.print(",0x");
                    ProtocolUtils.outputHex( ((int)kar));
                }
                resultArrayOutputStream.write(kar);
                switch(state) {

                    case WAIT_FOR_CONTROL: {
                        if (kar == ACK) {
                            if ((command.isENQCommand()) || (command.isSCommand()))
                                return null;
                            else if (command.isICommand()) {
                                state = WAIT_FOR_DATA;
                                count = 17; // UNIT_TYPE (3) UNIT_ID (8) MEM_START (3) MEM_STOP (3)
                            }
                            else if (command.isUCommand()) {
                                state = WAIT_FOR_DATA;
                                count = command.getExpectedDataLength(); // data length to expect
                            }
                            else if (command.isDCommand())
                                return null;
                            else
                                return null;
                        }
                        else if (kar == NAK) {
                            throw new ProtocolConnectionException("receiveResponse() NAK received, either CRC error or command invalid.",NAK_RECEIVED);
                        }
                        else if (kar == CAN) {
                            
                            if (command.isICommand())
                                throw new ProtocolConnectionException("receiveResponse() unitId or unitType missing or incorrect...",PROTOCOL_ERROR);
                            else
                                throw new ProtocolConnectionException("receiveResponse() error, command cannot be carried out. Probably missing S (security) command before...",PROTOCOL_ERROR);
                        }
                        else
                            resultArrayOutputStream.reset();
                        
                    } break; // WAIT_FOR_CONTROL

                    case WAIT_FOR_DATA: {
                        if ((command.isICommand()) || (command.isUCommand())) {
                            if (--count <= 0) {
                                state = WAIT_FOR_CRC;
                                count = 2;
                            }
                        }
                        else if (command.isDCommand()) {
                            if (kar == ACK) {
                                return null;
                            }
                            else
                                throw new ProtocolConnectionException("receiveResponse() 0x"+Integer.toHexString(kar)+" received instead of second ACK for the D command...",PROTOCOL_ERROR);
                        }
                    } break; // WAIT_FOR_DATA
                    
                    case WAIT_FOR_CRC: {
                        if (--count <= 0) {
                            byte[] data = resultArrayOutputStream.toByteArray();
                            int crc = CRCGenerator.calcCRCCCITTSchlumberger(data);
                            if (crc == 0) {
                                Response response = new Response(ProtocolUtils.getSubArray2(data, 1, data.length-1));
                                if (DEBUG>=1) System.out.println("KV_DEBUG> CRC OK, response="+response);
                                return response;
                            }
                            else {
                                throw new ProtocolConnectionException("receiveResponse() crc error",CRC_ERROR);
                            }
                        } // if (--count <= 0)
                    } // WAIT_FOR_CRC
                    
                } // switch(state)
                
            } // if ((kar = readIn()) != -1)
            
            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveResponse() response timeout error",TIMEOUT_ERROR);
            }
            
            if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveResponse() interframe timeout error",TIMEOUT_ERROR);
            }
            
        } // while(true)
        
    } // public Response receiveResponse()

    public String getNodeId() {
        return nodeId;  
    }

    private void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
}
