/*
 * TrimeranConnection.java
 *
 * Created on 19 juni 2006, 16:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.core;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.ConnectionV25;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.edf.trimaran.core.AbstractSPDU;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Koen
 *
 * GN |19-09-2008| Deleted the session timeOuts - When meter fails he retried for about 5 minutes and hardly never recovered from that
 * GN |24-07-2009| Rollbacked to previous working version
 */
public class TrimeranConnection extends ConnectionV25  implements ProtocolConnection {

    private static final int DEBUG=0;
    //private static final long SESSIONTIMEOUT_TSE=22000;

    final int TSE=0;
    final int TL=1;

    int timeoutTSE;
    int localTimeout;
    int timeoutType;
    int maxRetries;
    long forcedDelay;
    ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();
    String nodeId;
    int securityLevel;
    private int nSEQ=0;
    private int nSEQRx=0;
    ResponseFrame lastResponseFrame;
    int halfDuplex;
    private int type;
    int interKarTimeoutValue;
    int ackTimeoutTL;
    int commandTimeout;
    int flushTimeout;
    boolean firstCommand=true;
    private final int TYPE_DATA= 0x0;
    private final int TYPE_ACK = 0x6;
    private final int TYPE_NACK = 0xB;
    private final int WAIT_FOR_LENGTH=0;
    private final int WAIT_FOR_CONTROL=1;
    private final int WAIT_FOR_DATA=2;
    private final int WAIT_FOR_CRC=3;
    private final short ERROR_NAK=-30;
    private final short ERROR_LENGTH=-31;

    /** Creates a new instance of TrimeranConnection */
    public TrimeranConnection(InputStream inputStream,
            OutputStream outputStream,
            int timeoutTSE,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController,
            String serialNumber,int securityLevel,int halfDuplex,int interKarTimeoutValue,int ackTimeoutTL,int commandTimeout,int flushTimeout) {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeoutTSE = timeoutTSE;
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
        this.securityLevel=securityLevel;
        this.halfDuplex=halfDuplex;
        this.interKarTimeoutValue=interKarTimeoutValue;
        this.ackTimeoutTL=ackTimeoutTL;
        this.commandTimeout=commandTimeout;
        this.flushTimeout=flushTimeout;

    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException {
        this.nodeId=nodeId;
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


    private void sendFrame(byte[] data) throws ConnectionException {
        //if (getS
        sendOut(data);
    }

    public byte[] sendCommand(byte[] cmdData) throws IOException {
        return sendCommand(cmdData,0);
    }

    public byte[] sendCommand(byte[] cmdData, int len) throws IOException {
        int retry=0;

        try {
            if (DEBUG >= 1) {
                System.out.println("before sendCommand, wait for retry from meter...");
            }
            if (!firstCommand) {
                if (flushTimeout != 0) {
                    receiveData(flushTimeout);
                }
            }
            else {
                firstCommand=false;
            }
        }
        catch(IOException e) {
            // absorb
            if (DEBUG >= 1) {
                System.out.println("before sendCommand, Exception..." + e.toString());
            }
        }

        assembleCommand(cmdData);
        while(true) {
            try {
                flushInputStream();

                sendFrame(txOutputStream.toByteArray());
                return getSessionData(len);
            } catch(ConnectionException e) {
                if (retry++>=(maxRetries-1)) { // maxretries voldoet in een bepaalde voorwaarde aan de sessiontimeout...
                    throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage());
                }

                // KV new timeout behaviour
                if (e.getReason() == TIMEOUT_ERROR) {
                    if (timeoutType==TSE) {
                        throw new ProtocolConnectionException("sendCommand() error maxRetries (" + maxRetries + "), " + e.getMessage());
                    }
                }

                if ((e.getReason() != ERROR_NAK) && (e.getReason() != TIMEOUT_ERROR)) {
                    assembleCommand(cmdData); // YES, we must adjust the sequence number!
                }
            }
        }
    }

    private void assembleCommand(byte[] rawData) throws IOException {
        txOutputStream.reset();
        int len = rawData.length+2+2; // LEN (1 byte) ,NSEQ (1 byte) ,CRC (2 bytes)
        txOutputStream.write(len);
        txOutputStream.write(getNextNSEQ());
        txOutputStream.write(rawData);
        int crc = CRCGenerator.calcCRCFull(txOutputStream.toByteArray());
        txOutputStream.write(crc&0xFF);
        txOutputStream.write((crc>>8)&0xFF);
        setType(rawData[0]);
    }


    private void sendAck() throws IOException {
        sendAck(-1);
    }

    private void sendAck(int seq) throws IOException {
        byte[] ack = new byte[4];
        ack[0]=4;
        if (seq == -1) {
            ack[1] = (byte) (0x60 | getNSEQ());
        }
        else {
            ack[1] = (byte) (0x60 | seq);
        }

        int crc = CRCGenerator.calcCRC(ack,2);
        ack[2] = (byte)(crc&0xFF);
        ack[3] = (byte)((crc>>8)&0xFF);
        sendFrame(ack);
    }

    private void sendNAck() throws IOException {
        sendAck(-1);
    }

    private void sendNAck(int seq) throws IOException {
        byte[] ack = new byte[4];
        ack[0]=4;
        if (seq == -1) {
            ack[1] = (byte) (0xB0 | getNSEQ());
        }
        else {
            ack[1] = (byte) (0xB0 | seq);
        }

        int crc = CRCGenerator.calcCRC(ack,2);
        ack[2] = (byte)(crc&0xFF);
        ack[3] = (byte)((crc>>8)&0xFF);
        sendFrame(ack);
    }

    public byte[] getSessionData(int len) throws IOException {
        ResponseData responseData;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        boolean firstTry=true;
        while(true) {
            if (firstTry) {
                responseData = receiveData(commandTimeout);
                firstTry=false;
            }
            else {
                responseData = receiveData();
            }
            if (responseData==null) {
                return null;
            }
            if (responseData.getSPDUType() == AbstractSPDU.SPDU_DAT) {
                if (DEBUG >= 2) {
                    System.out.println("GetSession received AbstractSPDU.SPDU_DAT frame...");
                }
                if (responseData.getData().length > 0) {
                    if ((len>0) && (resultArrayOutputStream.toByteArray().length >= len)) {
                        if (DEBUG >= 2) {
                            System.out.println("Error, GetSession received AbstractSPDU.SPDU_DAT frame...");
                        }
                        throw new ProtocolConnectionException("getSessionData() Length error received="+resultArrayOutputStream.toByteArray().length+", allowed receive="+len,ERROR_LENGTH);
                    }
                }
                else if (DEBUG >= 2) {
                    System.out.println("Error, DO NOT THROW EXCEPTION !! GetSession received AbstractSPDU.SPDU_DAT frame...");
                }
                resultArrayOutputStream.write(responseData.getData());
            } else if (responseData.getSPDUType() == AbstractSPDU.SPDU_EOD) {
                if (DEBUG >= 2) {
                    System.out.println("GetSession received AbstractSPDU.SPDU_EOD frame...");
                }
                if ((len>0) && (resultArrayOutputStream.toByteArray().length != len)) {
                    if (DEBUG >= 2) {
                        System.out.println("Error, GetSession received AbstractSPDU.SPDU_EOD frame...");
                    }
                    throw new ProtocolConnectionException("getSessionData() Length error received="+resultArrayOutputStream.toByteArray().length+", allowed receive="+len,ERROR_LENGTH);
                }
                return resultArrayOutputStream.toByteArray();
            } else if (responseData.getSPDUType() == AbstractSPDU.SPDU_XID) {
                if (DEBUG >= 2) {
                    System.out.println("GetSession received AbstractSPDU.SPDU_XID frame...");
                }
                return responseData.getData();
            }

        }
    }

    private ResponseData receiveData() throws IOException {
        return receiveData(ackTimeoutTL);
    }

    private ResponseData receiveData(int useTimeout) throws IOException {
        int retry = 0;
        ResponseFrame responseFrame;
        localTimeout=useTimeout; //ackTimeoutTL;
        if (DEBUG >= 1) {
            System.out.println("set localTimeout to " + localTimeout + " ms");
        }
        timeoutType=TL;
        while(true) {

            try {
                responseFrame = receiveFrame(localTimeout);

                retry=0;
            } catch(ConnectionException e) {
                if (e.getReason() == CRC_ERROR) {
                    if (retry++>=maxRetries) {
                        throw e;
                    }
                    else {
                        sendNAck(getNSEQRx());
                        continue;
                    }
                }
                else {
                    throw e;
                }
            }

            if (responseFrame.getType() == TYPE_DATA) {

                if ((lastResponseFrame != null) && (lastResponseFrame.getNSEQRx() == getNSEQRx())) {
                    // absorb
                    sendAck(getNSEQRx());
                    if (DEBUG >= 1) {
                        System.out.println("Error in sequence nr... send same ACK...");
                    }
                    lastResponseFrame=responseFrame;
                    return new ResponseData(ProtocolUtils.getSubArray2(responseFrame.getData(), 0, 1));
                } else {
                    setNSEQ(getNSEQRx());
                    sendAck();
                    if (DEBUG >= 2) {
                        System.out.println("Data received, send ACK...");
                    }
                    lastResponseFrame=responseFrame;
                    return new ResponseData(responseFrame.getData());
                }
            } else if (responseFrame.getType() == TYPE_ACK) {
                //absorb...
                if (DEBUG >= 2) {
                    System.out.println("ACK received...");
                }
                if (getType() == AbstractSPDU.SPDU_EOS) {
                    return null;
                }

                long temp = System.currentTimeMillis();
                // to avoid rubish received... wait for an extra CD toggle and flush the input buffer before continue!

                if (getHalfDuplexController() != null) {
                    getHalfDuplexController().request2SendV25(0);
                    flushInputStream();
                    getHalfDuplexController().request2ReceiveV25(0);
                }

                // KV 09082006 control timeout
                localTimeout=timeoutTSE-(int)(System.currentTimeMillis()-temp); //-(int)forcedDelay; // 1 sec to wait before retry! because we received already an ACK!
                timeoutType=TSE;
                if (DEBUG >= 1) {
                    System.out.println("set localTimeout to " + localTimeout + " ms");
                }

            } else if (responseFrame.getType() == TYPE_NACK) {
                if (DEBUG >= 1) {
                    System.out.println("NACK received...");
                }
                throw new ProtocolConnectionException("receiveData() NACK received ",ERROR_NAK);
            }
            else {
                delayAndFlush(forcedDelay);
                throw new ProtocolConnectionException("receiveData() invalid frametype responseFrame.getType()="+responseFrame.getType(),PROTOCOL_ERROR);
            }
        }
    }

    private ResponseFrame receiveFrame(int localTimeout) throws IOException {

        long interFrameTimeout,interKarTimeout;
        int kar;
        int len=0;
        ByteArrayOutputStream frameArrayOutputStream = new ByteArrayOutputStream();
        int state = WAIT_FOR_LENGTH;

        int type=0;
        interFrameTimeout = System.currentTimeMillis() + localTimeout;
        interKarTimeout = System.currentTimeMillis() + interKarTimeoutValue;

        frameArrayOutputStream.reset();




        copyEchoBuffer();
        while(true) {
            if ((kar = readIn()) != -1) {
                // KV 09082006 inter karacter timeout
                if (state!=WAIT_FOR_LENGTH) {
                    if (System.currentTimeMillis() - interKarTimeout > 0) {
                        if (DEBUG>=1) {
                            System.out.println("KV_DEBUG> TrimaranConnection, receiveFrame(), interkar timeout at len=" + len + ", kar = " + Integer.toHexString(kar));
                        }
                        delayAndFlush(1000);
                        throw new ProtocolConnectionException("receiveFrame() interkar timeout error",TIMEOUT_ERROR);
                    }
                }
                interKarTimeout = System.currentTimeMillis() + interKarTimeoutValue;
                frameArrayOutputStream.write(kar);
                switch(state) {
                    case WAIT_FOR_LENGTH:
                        len=kar;
                        if ((len >= 4) && (len <= 0x7E)) {
                            if (DEBUG >= 1) {
                                System.out.println("KV_DEBUG> collect frame with len=" + len);
                            }
                            state = WAIT_FOR_CONTROL;
                            len--;
                        } else {
                            frameArrayOutputStream.reset();
                        }

                        break; // WAIT_FOR_LENGTH

                    case WAIT_FOR_CONTROL:
                        type = (kar >> 4) & 0x0F;
                        setNSEQRx(kar & 0x0F);
                        state = WAIT_FOR_DATA;
                        len--;

                        break; // WAIT_FOR_CONTROL

                    case WAIT_FOR_DATA:
                        if (--len<=2) {
                            state = WAIT_FOR_CRC;
                        }
                        break; // WAIT_FOR_DATA

                    case WAIT_FOR_CRC:
                        if (--len<=0) {
                            byte[] frame = frameArrayOutputStream.toByteArray();

                            if (DEBUG >= 1) {
                                System.out.println("KV_DEBUG> frame=" + ProtocolUtils.outputHexString(frame));
                            }
                            if (CRCGenerator.calcCRC(frame)==0) {
                                return new ResponseFrame(ProtocolUtils.getSubArray2(frame, 2, frame.length-4), type, getNSEQRx());
                            } else {
                                if (DEBUG >= 1) {
                                    System.out.println("KV_DEBUG> ******************************************* CRC NOK *************************************************");
                                }
                                throw new ProtocolConnectionException("receiveFrame() CRC error",CRC_ERROR);
                            }

                        }
                        break; // WAIT_FOR_CRC

                }
            }
            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error",TIMEOUT_ERROR);
            }
        }
    }


    public void setNSEQ(int nSEQ) {
        this.nSEQ=nSEQ;
    }


    public int getNSEQ() {
        return nSEQ;
    }

    public int getNextNSEQ() {
        ++nSEQ;
        if (nSEQ >= 0x10) {
            nSEQ = 0;
        }
        return nSEQ;
    }

    public int getType() {
        return type;
    }

    private void setType(int type) {
        this.type = type;
    }

    public int getNSEQRx() {
        return nSEQRx;
    }

    public void setNSEQRx(int nSEQRx) {
        this.nSEQRx = nSEQRx;
    }

}