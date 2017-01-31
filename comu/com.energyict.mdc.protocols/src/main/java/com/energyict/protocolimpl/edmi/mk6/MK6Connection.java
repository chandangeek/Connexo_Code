/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MK6Connection.java
 *
 * Created on 20 maart 2006, 10:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.Connection;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.edmi.mk6.core.ResponseData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 *
 * @author koen
 */
public class MK6Connection extends Connection  implements ProtocolConnection, Serializable {

    /** Generated SerialVersionUID */
	private static final long serialVersionUID = 4993375627564701564L;
	private static final int DEBUG=0;
    private static final long TIMEOUT=60000;

    private int timeout;
    private int maxRetries;
    private transient ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();

    private long sourceId;
    private long destinationId=-1;
    private int sequenceNr=0xFFFE; // initial sequencenumber
    private long forcedDelay;

    /** Creates a new instance of AlphaConnection */
    public MK6Connection(InputStream inputStream,
                         OutputStream outputStream,
                         int timeout,
                         int maxRetries,
                         long forcedDelay,
                         int echoCancelling,
                         HalfDuplexController halfDuplexController,
                         String serialNumber) throws ConnectionException {
          super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
          this.timeout = timeout;
          this.maxRetries=maxRetries;
          this.forcedDelay=forcedDelay;
          if ((serialNumber!=null) && ("".compareTo(serialNumber)!=0)) {
			destinationId=Long.parseLong(serialNumber);
		}
    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws java.io.IOException, ProtocolConnectionException {
        sourceId = Long.parseLong(nodeId);
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }

    private void sendByte(byte txbyte) throws ConnectionException {
        switch(txbyte) {
            case STX:
            case ETX:
            case DLE:
            case DC1: //XON:
            case DC3: //XOFF:
                assembleFrame(DLE);
                assembleFrame((byte)(txbyte|0x40));
                break;
            default:
                assembleFrame(txbyte);
        }
    } // void sendByte(byte txbyte) throws ConnectionException

    public ResponseData sendCommand(byte[] cmdData) throws IOException {
        int retry=0;
        doSendCommand(cmdData);
        while(true) {
            try {
                delayAndFlush(forcedDelay); // KV_DEBUG
                sendFrame();
                return receiveFrame();
            }
            catch(ConnectionException e) {
                if (retry++>=maxRetries) {
//                    throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage());
                	throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e);
                }
            }
        } // while(true)
    } // public void sendCommand(byte[] cmdData) throws ConnectionException

    private void genSequenceNr() {
        if ((sequenceNr==0) || (sequenceNr==0xFFFF)) {
			sequenceNr=1;
		} else {
			sequenceNr++;
		}
    }

    private byte[] getExtendedCommandHeader() {
        byte[] cmdData = new byte[11];
        cmdData[0]='E';
        cmdData[1]=(byte)(destinationId>>24);
        cmdData[2]=(byte)(destinationId>>16);
        cmdData[3]=(byte)(destinationId>>8);
        cmdData[4]=(byte)(destinationId);
        cmdData[5]=(byte)(sourceId>>24);
        cmdData[6]=(byte)(sourceId>>16);
        cmdData[7]=(byte)(sourceId>>8);
        cmdData[8]=(byte)(sourceId);
        genSequenceNr();
        cmdData[9]=(byte)(sequenceNr>>8);
        cmdData[10]=(byte)(sequenceNr);
        return cmdData;
    }

    // multidrop?
    private boolean isExtendedCommunication() {
        return (destinationId != -1);
    }

    private void assembleFrame(byte txbyte) {
        txOutputStream.write(txbyte);
    }

    private void sendFrame() throws ConnectionException {

        sendOut(txOutputStream.toByteArray());
    }


    private void doSendCommand(byte[] rawData) throws ConnectionException {
        txOutputStream.reset();
        assembleFrame(STX);
        byte[] cmdData=rawData;
        byte[] txFrame=null;
        if (isExtendedCommunication()) {  // multidrop...
            if (rawData==null) {
				cmdData = getExtendedCommandHeader();
			} else {
				cmdData = ProtocolUtils.concatByteArrays(getExtendedCommandHeader(),rawData);
			}
        }
        if ((cmdData!=null) && (cmdData.length>0)) {
            txFrame = new byte[cmdData.length+1+2]; // [STX][cmdData array bytes][CRC 16 bit]
            System.arraycopy(cmdData,0,txFrame,1,cmdData.length);
            txFrame[0]=STX;
            int crc = CRCGenerator.ccittCRC(txFrame, txFrame.length-2);
            txFrame[txFrame.length-2]=(byte)(crc>>8);
            txFrame[txFrame.length-1]=(byte)(crc);
            for (int i=1; i<(txFrame.length); i++) {
				sendByte(txFrame[i]);
			}
        }

        assembleFrame(ETX); // [ETX]
    } // void sendData(byte[] cmdData) throws ConnectionException


    private static final int STATE_WAIT_FOR_STX=0;
    private static final int STATE_WAIT_FOR_DATA=1;

    public ResponseData receiveFrame() throws NestedIOException, IOException {

        long protocolTimeout,interFrameTimeout;
        int kar;
        int state = STATE_WAIT_FOR_STX;
        boolean dleKar=false;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();

        interFrameTimeout = System.currentTimeMillis() + timeout;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;

        resultArrayOutputStream.reset();
        allDataArrayOutputStream.reset();

        copyEchoBuffer();
        while(true) {

            if ((kar = readIn()) != -1) {
                switch(state) {
                    case STATE_WAIT_FOR_STX:
                        interFrameTimeout = System.currentTimeMillis() + timeout;

                        if (kar==STX) {
                            allDataArrayOutputStream.write(kar);
                            state = STATE_WAIT_FOR_DATA;
                        }

                        break; // STATE_WAIT_FOR_STX

                    case STATE_WAIT_FOR_DATA:

                        if (kar==DLE) {
                            dleKar=true;
                        }
                        else if (kar==ETX) {
                            // Calc CRC
                            byte[] rxFrame = allDataArrayOutputStream.toByteArray();
                            if (CRCGenerator.ccittCRC(rxFrame)==0) {
                                // OK
                                if (isExtendedCommunication()) {
                                   int rxSequenceNr = (((int)rxFrame[10]&0xFF)<<8) | ((int)rxFrame[11]&0xFF);
                                   if (rxSequenceNr != sequenceNr) {
									throw new ProtocolConnectionException("receiveFrame() rxSequenceNr("+rxSequenceNr+") != sequenceNr("+sequenceNr+")",PROTOCOL_ERROR);
								} else {
									return new ResponseData(ProtocolUtils.getSubArray(rxFrame,12, rxFrame.length-3));
								}
                                } else {
									return new ResponseData(ProtocolUtils.getSubArray(rxFrame,1, rxFrame.length-3));
								}
                            }
                            else {
                                // ERROR, CRC error
                                throw new ProtocolConnectionException("receiveFrame() response crc error",CRC_ERROR);
                            }
                        }
                        else {
                            if (dleKar) {
                                allDataArrayOutputStream.write(kar&0xBF);
                            }
                            else {
                                allDataArrayOutputStream.write(kar);
                            }
                            dleKar=false;
                        }

                        break; // STATE_WAIT_FOR_DATA

                } // switch(state)

            } // if ((kar = readIn()) != -1)

            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() response timeout error",TIMEOUT_ERROR);
            }
            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error",TIMEOUT_ERROR);
            }

        }


    }

}