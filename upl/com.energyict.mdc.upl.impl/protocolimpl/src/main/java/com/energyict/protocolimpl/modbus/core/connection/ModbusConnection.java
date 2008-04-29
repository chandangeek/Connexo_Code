/*
 * ModbusConnection.java
 *
 * Created on 19 september 2005, 16:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.connection;

import com.energyict.protocolimpl.modbus.core.*;
import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCodeFactory;
import java.io.*;
import java.util.*;


import com.energyict.cbo.NestedIOException;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.ConnectionRS485;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.protocol.meteridentification.MeterType;

/**
 *
 * @author Koen
 */
public class ModbusConnection extends ConnectionRS485 implements ProtocolConnection { 
    
    private static final int DEBUG=0;
    private static final long TIMEOUT=60000;
    
    int timeout;
    int maxRetries;
    boolean loggedOn=false;
    
    int address;
    int interframeTimeout;
    int responseTimeout;
    int physicalLayer;
    
    /** Creates a new instance of MarkVConnection */
    public ModbusConnection(InputStream inputStream,
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController,
            int interframeTimeout,
            int responseTimeout,
            int physicalLayer) throws ConnectionException {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeout = timeout;
        this.maxRetries=maxRetries;
        this.interframeTimeout=interframeTimeout;
        this.responseTimeout=responseTimeout;
        this.physicalLayer=physicalLayer;
        
    } // ModbusConnection(...)
    
    public void setAddress(int address) {
        this.address=address;
    }
    public int getAddress() {
        return address;
    }
    
    private void assembleAndSend(RequestData requestData) throws NestedIOException, ConnectionException {
        byte[] data = ProtocolUtils.concatByteArrays(new byte[]{(byte)getAddress()},requestData.getFrameData());
        int crc = CRCGenerator.calcCRCModbus(data);
        sendRawData(ProtocolUtils.concatByteArrays(data,new byte[]{(byte)(crc%256),(byte)(crc/256)}));
    }
    
    public ResponseData sendRequest(RequestData requestData) throws IOException {
        int retry=0;
        while(true) {
        	flushInputStream();
            ResponseData responseData;
            try {
                assembleAndSend(requestData);
                if (requestData.getFunctionCode() == 0x2B) // in case of read device identification, always use modbus phy because there is no length in the frame
                    responseData = receiveDataModbus(requestData);
                else if (physicalLayer==0)// use datalength
                   responseData = receiveDataLength(requestData);
                else if (physicalLayer==1) // following modbus specs
                   responseData = receiveDataModbus(requestData);
                else throw new ProtocolConnectionException("ModbusConnection, sendRequest(), unknow physicalLayer="+physicalLayer+" property! Correct first");
                
                if (responseData.isException()) {
                    throw new ProtocolConnectionException("ModbusConnection, sendRequest(), exception "+responseData.getExceptionString()+" received!");
                }
                return responseData;
            }
            catch(ConnectionException e) {
                if (DEBUG>=1) System.out.println("KV_DEBUG> CRC_ERROR retry="+retry+" of getMaxRetries()="+getMaxRetries());
                if (retry++>=getMaxRetries()) {
                    throw new ProtocolConnectionException("ModbusConnection, sendRequest(), error maxRetries ("+getMaxRetries()+"), "+e.getMessage());
                }
            }
            catch(ModbusException e) {
                if (DEBUG>=1) System.out.println("KV_DEBUG> ModbusException retry="+retry+" of getMaxRetries()="+getMaxRetries());
                if (retry++>=getMaxRetries()) {
                    throw new ProtocolConnectionException("ModbusConnection, sendRequest(), error maxRetries ("+getMaxRetries()+"), "+e.getMessage());
                }
            }
        }
    }
    
    static private final int STATE_WAIT_FOR_ADDRESS=0;
    static private final int STATE_WAIT_FOR_FUNCTIONCODE=1;
    static private final int STATE_WAIT_FOR_DATA=2;
    static private final int STATE_WAIT_FOR_LENGTH=3;
    static private final int STATE_WAIT_FOR_EXCEPTIONCODE=4;
    
    private ResponseData receiveDataLength(RequestData requestData) throws NestedIOException, IOException {
        long protocolTimeout;
        int kar;
        int state=STATE_WAIT_FOR_ADDRESS;
        boolean lastPacket=true;
        ResponseData responseData = new ResponseData();
        ByteArrayOutputStream resultDataArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();
        
        protocolTimeout = System.currentTimeMillis() + timeout;
        int len=0;
        int functionErrorCode=0;
        resultDataArrayOutputStream.reset();
        allDataArrayOutputStream.reset();
        if (DEBUG >= 2) System.out.println("receiveDataLength(...):");
        copyEchoBuffer();
        while(true) {
            
            if ((kar = readIn()) != -1) {
                
                if (DEBUG >= 2) {
                    System.out.print(",0x");
                    ProtocolUtils.outputHex( ((int)kar));
                }
                
                allDataArrayOutputStream.write(kar); // accumulate frame
                
                switch(state) {
                    case STATE_WAIT_FOR_ADDRESS: {
                        if (kar == getAddress()) {
                            allDataArrayOutputStream.reset();
                            allDataArrayOutputStream.write(kar);
                            responseData.setAddress(kar);
                            state = STATE_WAIT_FOR_FUNCTIONCODE;
                            len=0;
                            protocolTimeout = System.currentTimeMillis() + responseTimeout;
                        }
                        else allDataArrayOutputStream.reset();
                   } break; // STATE_WAIT_FOR_ADDRESS
                    
                    case STATE_WAIT_FOR_FUNCTIONCODE: {
                        if (kar == requestData.getFunctionCode()) {
                            responseData.setFunctionCode(kar);
                            state = STATE_WAIT_FOR_LENGTH;
                        }
                        else if (kar == (requestData.getFunctionCode()+0x80)) {
                            functionErrorCode=kar;
                            state = STATE_WAIT_FOR_EXCEPTIONCODE;
                        }
                        else {
                            throw new ProtocolConnectionException("receiveDataLength() should receive the functioncode!",PROTOCOL_ERROR);
                        }
                    } break; // STATE_WAIT_FOR_FUNCTIONCODE
                    
                    case STATE_WAIT_FOR_EXCEPTIONCODE: {

                        throw new ModbusException("receiveDataLength() functionErrorCode 0x"+Integer.toHexString(functionErrorCode)+", exception code 0x"+Integer.toHexString(kar)+", received!",PROTOCOL_ERROR, functionErrorCode, kar);
                    } // STATE_WAIT_FOR_EXCEPTIONCODE
                    
                    case STATE_WAIT_FOR_LENGTH: {
                        resultDataArrayOutputStream.write(kar);
                        len=(kar+2); // add 2 bytes for the CRC
                        state=STATE_WAIT_FOR_DATA;
                    } break; // STATE_WAIT_FOR_LENGTH
                    
                    // we should not use the length to check if the vcomplete frame is received. However, the problem lays within java not behaving 
                    // realtime enough to implement the correct Modbus Phy layer timing T = 3.5 kar
                    // Gaps between receiving data from the underlaying serial logic can take up to 30 ms...
                    case STATE_WAIT_FOR_DATA: {
                        resultDataArrayOutputStream.write(kar);
                        if (--len <= 0) {
                            try {
                                Thread.sleep(interframeTimeout);
                            }
                            catch(InterruptedException e) {
                                // absorb
                            }
                            byte[] data = allDataArrayOutputStream.toByteArray();
                            if (data.length <= 2)
                                throw new ProtocolConnectionException("receiveDataLength() PROTOCOL Error",PROTOCOL_ERROR);
                            int crc = ((int)data[data.length-1]&0xff)<<8 | ((int)data[data.length-2]&0xff);
                            data = ProtocolUtils.getSubArray2(data, 0, data.length-2);
                            int crc2 = CRCGenerator.calcCRCModbus(data);
                            if (crc2==crc) {
                                data = resultDataArrayOutputStream.toByteArray();
                                responseData.setData(ProtocolUtils.getSubArray2(data, 0, data.length-2));
                                if (DEBUG>=2) System.out.println("KV_DEBUG> "+responseData);
                                return responseData;
                            }
                            else {
                                if (DEBUG>=2) System.out.println("KV_DEBUG> CRC_ERROR ");
                                throw new ProtocolConnectionException("receiveDataLength() CRC Error",CRC_ERROR);
                            }
                        }
                        
                    } break; // STATE_WAIT_FOR_DATA
                    
                    default:
                        throw new ProtocolConnectionException("receiveDataLength() invalid state!",PROTOCOL_ERROR);
                    
                } // switch(iState)
                
                //allDataArrayOutputStream.write(kar); // accumulate frame
                
            } // if ((iNewKar = readIn()) != -1)
            
            // in case of a response timeout
            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveDataLength() response timeout error",TIMEOUT_ERROR);
            }
            
        } // while(true)
        
    } // private ResponseData receiveData(RequestData requestData) throws NestedIOException, IOException
    
    // KV_TO_DO should inplement intercharacter frame timeout of max 1.5 char and interframe timeout of min 3.5 char
    private ResponseData receiveDataModbus(RequestData requestData) throws NestedIOException, IOException {
        long protocolTimeout,interframe;
        int kar;
        int state=STATE_WAIT_FOR_ADDRESS;
        boolean lastPacket=true;
        int functionErrorCode=0;
        ResponseData responseData = new ResponseData();
        ByteArrayOutputStream resultDataArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();
        
        protocolTimeout = System.currentTimeMillis() + timeout;
        // We should implement an phy abstraction layer with manageable parameters. Because we fix the interframe timeout at the
        // 2400 (lowest?) baudrate, performance when reading lots of modbusmeters real-time can degrade!
        interframe = System.currentTimeMillis() + timeout;
        
        resultDataArrayOutputStream.reset();
        allDataArrayOutputStream.reset();
        if (DEBUG >=2) System.out.println("receiveData(...):");
        copyEchoBuffer();
        while(true) {
            
            if ((kar = readIn()) != -1) {
                
                if (state != STATE_WAIT_FOR_ADDRESS)
                     interframe = System.currentTimeMillis() + interframeTimeout; // // 3.5 cher T @ 2400 (supposed as lowest baudrate)
                
                if (DEBUG >= 2) {
                    System.out.print(",0x");
                    ProtocolUtils.outputHex( ((int)kar));
                }
                
                allDataArrayOutputStream.write(kar); // accumulate frame
                
                switch(state) {
                    case STATE_WAIT_FOR_ADDRESS: {
                        if (kar == getAddress()) {
                            allDataArrayOutputStream.reset();
                            allDataArrayOutputStream.write(kar);
                            responseData.setAddress(kar);
                            state = STATE_WAIT_FOR_FUNCTIONCODE;
                            if (DEBUG>=2) System.out.println("KV_DEBUG> address received");
                        }
                        else allDataArrayOutputStream.reset();
                   } break; // STATE_WAIT_FOR_ADDRESS
                    
                    case STATE_WAIT_FOR_FUNCTIONCODE: {
                        if (kar == requestData.getFunctionCode()) {
                            responseData.setFunctionCode(kar);
                            state = STATE_WAIT_FOR_DATA;
                        }
                        else if (kar == (requestData.getFunctionCode()+0x80)) {
                            functionErrorCode=kar;
                            state = STATE_WAIT_FOR_EXCEPTIONCODE;
                        }
                        else {
                            throw new ProtocolConnectionException("receiveDataModbus() should receive the functioncode!",PROTOCOL_ERROR);
                        }
                        
                    } break; // STATE_WAIT_FOR_FUNCTIONCODE
                    
                    case STATE_WAIT_FOR_EXCEPTIONCODE: {
                        throw new ModbusException("receiveDataModbus() functionErrorCode 0x"+Integer.toHexString(functionErrorCode)+", exception code 0x"+Integer.toHexString(kar)+", received!",PROTOCOL_ERROR, functionErrorCode, kar);
                    } // STATE_WAIT_FOR_EXCEPTIONCODE
                    
                    case STATE_WAIT_FOR_DATA: {
                        resultDataArrayOutputStream.write(kar);
                    } break; // STATE_WAIT_FOR_DATA
                    
                    default:
                        throw new ProtocolConnectionException("receiveDataModbus() invalid state!",PROTOCOL_ERROR);
                    
                } // switch(iState)
                
                //allDataArrayOutputStream.write(kar); // accumulate frame
                
            } // if ((iNewKar = readIn()) != -1)
            
            // frame received, check validity
            if (state != STATE_WAIT_FOR_ADDRESS) {
                if (((long) (System.currentTimeMillis() - interframe)) > 0) {
                    byte[] data = allDataArrayOutputStream.toByteArray();
                    if (data.length <= 2)
                        throw new ProtocolConnectionException("receiveDataModbus() PROTOCOL Error",PROTOCOL_ERROR);
                    int crc = ((int)data[data.length-1]&0xff)<<8 | ((int)data[data.length-2]&0xff);
                    data = ProtocolUtils.getSubArray2(data, 0, data.length-2);
                    int crc2 = CRCGenerator.calcCRCModbus(data);
                    if (crc2==crc) {
                        data = resultDataArrayOutputStream.toByteArray();
                        responseData.setData(ProtocolUtils.getSubArray2(data, 0, data.length-2));
                        if (DEBUG>=2) System.out.println("KV_DEBUG> "+responseData);
                        return responseData;
                    }
                    else {
                        if (DEBUG>=2) System.out.println("KV_DEBUG> CRC_ERROR ");
                        throw new ProtocolConnectionException("receiveDataModbus() CRC Error",CRC_ERROR);
                    }
                } // if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0)
            } // if (state != STATE_WAIT_FOR_ADDRESS)
            
            // in case of a response timeout
            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveDataModbus() response timeout error",TIMEOUT_ERROR);
            }
            
        } // while(true)
        
    } // private ResponseData receiveData(RequestData requestData) throws NestedIOException, IOException
    
    /*******************************************************************************************
     * Implementation of the abstract Connection class
     ******************************************************************************************/
    public void setHHUSignOn(HHUSignOn hhuSignOn) {
        
    }
    public HHUSignOn getHhuSignOn() {
        return null;
    }
    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        
    }
    public MeterType connectMAC(String strID,String strPassword,int securityLevel,String nodeId) throws IOException, ProtocolConnectionException {
        if (strID==null)
            throw new IOException("DeviceID invalid! Must have a value! Correct first!");
        setAddress(Integer.parseInt(strID));
        return null;
    }
    public byte[] dataReadout(String strID,String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }
    
}
