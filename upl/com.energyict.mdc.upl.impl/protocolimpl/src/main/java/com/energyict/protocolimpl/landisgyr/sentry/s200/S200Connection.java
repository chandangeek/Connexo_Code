/*
 * S200Connection.java
 *
 * Created on 18 juli 2006, 13:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.io.NestedIOException;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.landisgyr.sentry.s200.core.ResponseData;
import com.energyict.protocolimpl.landisgyr.sentry.s200.core.ResponseFrame;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Koen
 */
public class S200Connection extends Connection  implements ProtocolConnection {

    private static final int DEBUG=0;
    private static final long TIMEOUT=60000;

    int timeout;
    int maxRetries;
    long forcedDelay;
    int securityLevel;
    private int crn;
    String nodeId;
    int crnInitialValue;

    ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();

    /** Creates a new instance of S200Connection */
    public S200Connection(InputStream inputStream,
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController,
            String serialNumber,int securityLevel, int crnInitialValue) throws ConnectionException {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeout = timeout;
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
        this.securityLevel=securityLevel;
        this.crnInitialValue=crnInitialValue;
        if (crnInitialValue < 0)
           setCrn(-1);
        else
           setCrn(crnInitialValue-1);
    } // EZ7Connection(...)


    public byte[] dataReadout(String strID, String nodeId) {
        return null;
    }

    public com.energyict.protocol.meteridentification.MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) {
        this.nodeId=nodeId;
        return null;
    }

    public void disconnectMAC() {
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }

    public void delayAndFlush(long delay)  throws ConnectionException, NestedIOException {
        super.delayAndFlush(delay);
    }

    public int getCrn() {
        return crn;
    }

    public void setCrn(int crn) {
        this.crn = crn;
    }

    public int getNextCrn() {
        return ++crn;
    }

    // STX CRN DATA[6] PASSWORD[4] CRC[2]
    private void assembleFrame(int command, byte[] data) throws IOException {
        txOutputStream.reset();
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        temp.write(command);
        temp.write(getNextCrn());
        temp.write(data);
        int crc = CRCGenerator.calcCRCSentry(temp.toByteArray());
        temp.write(crc>>8);
        temp.write(crc&0xFF);
        txOutputStream.write(STX);
        txOutputStream.write(temp.toByteArray());
    }

    private void sendFrame() throws ConnectionException {
        sendOut(txOutputStream.toByteArray());
    }

    public ResponseData sendCommand(int command, byte[] data) throws IOException {
        int retry=0;
        //assembleFrame(command,data);
        while(true) {
            try {
                assembleFrame(command,data);
                delayAndFlush(forcedDelay);
                sendFrame();
                ResponseFrame rf = receiveFrame();
                if (rf.getFrameNr() != getCrn()) {
                    if (crnInitialValue < 0) {
                        //System.out.println("crnInitialValue < 0 !!!!! (received "+rf.getFrameNr()+" != "+getCrn()+")");
                        setCrn(rf.getFrameNr()-1);
                    }
                    throw new ProtocolConnectionException("receiveFrame() crn difference (received "+rf.getFrameNr()+" != "+getCrn()+" ",PROTOCOL_ERROR);
                }
                return new ResponseData(rf);
            } catch(ConnectionException e) {
                if (retry++>=maxRetries) {
                    throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage(), MAX_RETRIES_ERROR);
                }
            }
        } // while(true)
    } // public ResponseData sendCommand(int command, byte[] data) throws IOException


    private final int WAIT_FOR_STX=0;
    private final int WAIT_FOR_FRAME_CONTROL=1;
    private final int WAIT_FOR_UNIT_ID=2;
    private final int WAIT_FOR_STATUS=3;
    private final int WAIT_FOR_FRAME_NR=4;
    private final int WAIT_FOR_DATA=5;
    private final int WAIT_FOR_CRC=6;
    private final int WAIT_FOR_END_CONTROL=7;

    private ResponseFrame receiveFrame() throws IOException {

        long protocolTimeout,interFrameTimeout;
        int kar;
        int count=0;
        int retry=0;
        int len=0;
        int crcCalculated=0;
        int crcReceived=0;
        int dataLenght=0;
        int state = WAIT_FOR_STX;
        ByteArrayOutputStream frameArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();


        interFrameTimeout = System.currentTimeMillis() + timeout;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;

        frameArrayOutputStream.reset();
        resultArrayOutputStream.reset();

        copyEchoBuffer();
        while(true) {

            if ((kar = readIn()) != -1) {
                if (DEBUG >= 2) {
                    System.out.print(",0x");
                    ProtocolUtils.outputHex(kar);
                }

                frameArrayOutputStream.write(kar);

                switch(state) {

                    case WAIT_FOR_STX:
                        interFrameTimeout = System.currentTimeMillis() + timeout;
                        if (kar == STX) {
                            state = WAIT_FOR_FRAME_CONTROL;
                            frameArrayOutputStream.reset();
                        }


                    break; // WAIT_FOR_STX

                    case WAIT_FOR_FRAME_CONTROL:

                        if (kar == ACK) {
                            state = WAIT_FOR_UNIT_ID;
                            dataLenght=6;
                            count = 0;
                        }
                        else if (kar == NAK) {
                            throw new ProtocolConnectionException("receiveFrame() NAK received",PROTOCOL_ERROR);
                        }
                        else if (kar == 0x0C) {
                            // Datadump
                            state = WAIT_FOR_UNIT_ID;
                            dataLenght=256;
                            count = 0;
                        }

                    break; // WAIT_FOR_FRAME_CONTROL

                    case WAIT_FOR_UNIT_ID:
                        if (count++ >= 3) {
                            state = WAIT_FOR_STATUS;
                        }
                    break; // WAIT_FOR_UNIT_ID

                    case WAIT_FOR_STATUS:
                        state = WAIT_FOR_FRAME_NR;
                    break; // WAIT_FOR_STATUS

                    case WAIT_FOR_FRAME_NR:
                        state = WAIT_FOR_DATA;
                        count = 0;
                    break; // WAIT_FOR_FRAME_NR

                    case WAIT_FOR_DATA:
                        if (count++ >= (dataLenght-1)) {
                            state = WAIT_FOR_CRC;
                            count = 0;
                            crcCalculated = CRCGenerator.calcCRCSentry(frameArrayOutputStream.toByteArray());
                        }
                    break; // WAIT_FOR_DATA

                    case WAIT_FOR_CRC:
                        if (count++ >= 1) {
                            byte[] frame = frameArrayOutputStream.toByteArray();
                            crcReceived = (((int)frame[frame.length-2]&0xff)<<8) | ((int)frame[frame.length-1]&0xff);
                            if (crcReceived == crcCalculated) {
                                if (dataLenght==6)
                                   return new ResponseFrame(frame);
                                else
                                   state = WAIT_FOR_END_CONTROL;
                            }
                            else {
                                throw new ProtocolConnectionException("receiveFrame() CRC_ERROR",CRC_ERROR);
                            }
                            // verify CRC;
                        }
                    break; // WAIT_FOR_CRC

                    case WAIT_FOR_END_CONTROL:
                        return new ResponseFrame(frameArrayOutputStream.toByteArray());
                    // DB_WAIT_FOR_END_CONTROL

                } // switch(state)

            } // if ((kar = readIn()) != -1)

            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() response timeout error",TIMEOUT_ERROR);
            }
            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error",TIMEOUT_ERROR);
            }

        } // while(true)

    } // public void receiveFrame() throws ConnectionException

    // STX CSA CBN CRC[2]
    private void assembleDataBlockAcknowledge(int cbn,int csa) throws IOException {
        txOutputStream.reset();
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        temp.write(csa);
        temp.write(cbn);
        int crc = CRCGenerator.calcCRCSentry(temp.toByteArray());
        temp.write(crc>>8);
        temp.write(crc&0xFF);
        txOutputStream.write(STX);
        txOutputStream.write(temp.toByteArray());
    }

    public ResponseFrame sendDataBlockAcknowledge(int blockNr) throws IOException {
        int retry=0;
        int csa = ACK;
        while(true) {
            try {

                delayAndFlush(forcedDelay);
                assembleDataBlockAcknowledge(blockNr,csa);
                sendFrame();

                ResponseFrame rdb = receiveFrame();

                return rdb;
            } catch(ConnectionException e) {

                if (retry++>=maxRetries) {
                    throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage(), MAX_RETRIES_ERROR);
                }
                else {
                    if (e.getReason() == CRC_ERROR)
                        csa = NAK;
                    else if ((blockNr == 0) && (e.getReason() == TIMEOUT_ERROR)) // timeout on last block ack
                        return null;
                }

            }
        } // while(true)
    } // public ResponseData sendCommand(int command, byte[] data) throws IOException


}
