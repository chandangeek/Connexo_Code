/*
 * MiniDLMSConnection.java
 *
 * Created on 30 november 2006, 9:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.Connection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Koen
 */
public class MiniDLMSConnection extends Connection  implements ProtocolConnection {

    private static final int DEBUG=0;
    private static final long TIMEOUT=60000;

    InputStream inputStream;
    OutputStream outputStream;
    private int echoCancelling;
    private HalfDuplexController halfDuplexController;
    private int timeout;
    private int maxRetries;
    private long forcedDelay;
    private int securityLevel;
    private int clientAddress;

    private String nodeId;

    // packet flags
    private static final int EVENT_START_FLAG=0x7F;
    private static final int EVENT_END_FLAG=0x7E;
    private static final int EVENT_ESCAPE_FLAG=0x7D;

    // receive packet receiver state machine
    private static final int STATE_RECEIVE_NOPACKET=0;
    private static final int STATE_RECEIVE_ESCAPE=1;
    private static final int STATE_RECEIVE_BUILDING_PACKET=2;

    // packet types
    private static final int PACKET_TYPE_SABM = 0xF4;
    private static final int PACKET_TYPE_UA = 0xCE;
    private static final int PACKET_TYPE_RR = 0x88;
    private static final int PACKET_TYPE_REJ = 0x90; // 0x98; Discrepancy in MiniDLMS protocol page 7 doc compared to the communication trace...
    private static final int PACKET_TYPE_UI = 0xC8;
    private static final int PACKET_TYPE_I = 0x00; // 0 N(s2) N(s1) N(s0) P N(r2) N(r1) N(r0)

    // send state machine
    private TransmitterStateMachine transmitterStateMachine=null;
    private ReceiverStateMachine receiverStateMachine=null;

    private int sendSequence;
    private int receiveSequence;

    ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();

    /** Creates a new instance of MiniDLMSConnection */
    public MiniDLMSConnection(InputStream inputStream,
                              OutputStream outputStream,
                              int maxRetries,
                              long forcedDelay,
                              int echoCancelling,
                              HalfDuplexController halfDuplexController,
                              int timeout,
                              int securityLevel,
                              int clientAddress) {

        super(inputStream,outputStream,forcedDelay,echoCancelling,halfDuplexController);
        this.setTimeout(timeout);
        this.setMaxRetries(maxRetries);
        this.setForcedDelay(forcedDelay);
        this.setSecurityLevel(securityLevel);
        this.setClientAddress(clientAddress);
        init();
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }

    private void init() {
        txOutputStream.reset();
        setSendSequence(0);
        setReceiveSequence(7);
        setTransmitterStateMachine(new TransmitterStateMachine(this));
        setReceiverStateMachine(new ReceiverStateMachine(this));
    }

    public byte[] sendCommand(byte[] data) throws IOException {
        return getTransmitterStateMachine().sendRequestI(data);
        //return getTransmitterStateMachine().sendRequestUI(data);
    }

    protected void sendUA() throws IOException {
        send(getPACKET_TYPE_UA());
    }
    protected void sendRR() throws IOException {
        send(getPACKET_TYPE_RR());
    }
    protected void sendREJ() throws IOException {
        send(getPACKET_TYPE_REJ());
    }

    protected void sendUI(byte[] data) throws IOException {
        send(getPACKET_TYPE_UI(), data);
    }

    protected void sendSABM() throws IOException {
        send(getPACKET_TYPE_SABM());
    }

    private void send(int packetType) throws IOException {
        send(packetType, null);
    }

    protected void send(int packetType, byte[] data) throws IOException {
        delayAndFlush(forcedDelay);

        sendOut(assembleFrame(packetType, data));
    }

    private byte[] assembleFrame(int packetType, byte[] data) throws IOException {
        ByteArrayOutputStream crcFrame = new ByteArrayOutputStream();
        txOutputStream.reset();
        txOutputStream.write(getEVENT_START_FLAG());
        crcFrame.write(getClientAddress());
        crcFrame.write(Integer.parseInt(getNodeId()));

//System.out.println("KV_DEBUG> "+(data!=null?ProtocolUtils.outputHexString(data):"")+", S="+getSendSequence()+", R="+getReceiveSequence());



        if ((packetType&0xF7) == getPACKET_TYPE_I()) {  // also send N(s) and N(r)
            packetType = packetType | (getSendSequence()<<4) | getReceiveSequence();
        } else if (packetType == getPACKET_TYPE_REJ()) { // also send N(r)
            packetType = packetType | getReceiveSequence();
        } else if (packetType == getPACKET_TYPE_RR()) { // also send N(r)
            packetType = packetType | getReceiveSequence();
        }

        crcFrame.write(packetType);

        if (data!=null) {
            crcFrame.write(data);
        }
        byte[] crcFrameData = crcFrame.toByteArray();
        int crc = CRCGenerator.calcCRC16(crcFrameData);
        crcFrame.write(crc>>8);
        crcFrame.write(crc);

        txOutputStream.write(escapeData(crcFrame.toByteArray()));
        txOutputStream.write(getEVENT_END_FLAG());

        return txOutputStream.toByteArray();

    } // protected byte[] assembleFrame(int packetType, byte[] data) throws IOException


    public void identify() throws IOException {
        sendOut((byte)'I');
    }

    public void incSendSequence() {
        if (sendSequence++ >=7) sendSequence=0;
    }

    public int getReceiveSequencePlus1() {
        int temp = receiveSequence;
        if (temp++ >=7) temp=0;
        return temp;
    }
    public int getSendSequencePlus1() {
        int temp = sendSequence;
        if (temp++ >=7) temp=0;
        return temp;
    }

    public void incReceiveSequence() {
        if (receiveSequence++ >=7) receiveSequence=0;
    }

    protected byte[] escapeData(byte[] data) {
        ByteArrayOutputStream escapedData = new ByteArrayOutputStream();
        escapedData.reset();
        for (int i=0;i<data.length;i++) {
            if ((data[i] == EVENT_START_FLAG) || (data[i] == EVENT_END_FLAG) || (data[i] == EVENT_ESCAPE_FLAG))
                escapedData.write(EVENT_ESCAPE_FLAG);
            escapedData.write(data[i]);
        }
        return escapedData.toByteArray();
    }

    public com.energyict.protocol.meteridentification.MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws java.io.IOException {
        this.setNodeId(nodeId);
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
    }

    public static int getEVENT_START_FLAG() {
        return EVENT_START_FLAG;
    }

    public static int getEVENT_END_FLAG() {
        return EVENT_END_FLAG;
    }

    public static int getEVENT_ESCAPE_FLAG() {
        return EVENT_ESCAPE_FLAG;
    }

    public int getEchoCancelling() {
        return echoCancelling;
    }

    public void setEchoCancelling(int echoCancelling) {
        this.echoCancelling = echoCancelling;
    }

    public HalfDuplexController getHalfDuplexController() {
        return halfDuplexController;
    }

    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getForcedDelay() {
        return forcedDelay;
    }

    public void setForcedDelay(long forcedDelay) {
        this.forcedDelay = forcedDelay;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String receiveIdentifyResponse() throws IOException {
        long protocolTimeout,interFrameTimeout;
        int kar;
        int index=0;
        int state = STATE_RECEIVE_NOPACKET;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();

        interFrameTimeout = System.currentTimeMillis() + timeout;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;


        if (DEBUG>=1) {
            System.out.println("KV_DEBUG> timeout=" + timeout);
        }

        resultArrayOutputStream.reset();

        copyEchoBuffer();
        while(true) {
            if ((kar = readIn()) != -1) {
                if (DEBUG >= 2) {
                    System.out.print(",0x");
                    ProtocolUtils.outputHex(kar);
                }
                resultArrayOutputStream.write(kar);
                if (kar==0) {
                    return new String(resultArrayOutputStream.toByteArray());
                }
            } // if ((kar = readIn()) != -1)

            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("receivePacketStateMachine() response timeout error",TIMEOUT_ERROR);
            }

            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("receivePacketStateMachine() interframe timeout error",TIMEOUT_ERROR);
            }

        } // while(true)

    } // private String receiveIdentifyResponse() throws IOException

    protected Frame receivePacketStateMachine() throws IOException {
        long protocolTimeout,interFrameTimeout;
        int kar;
        int index=0;
        int state = STATE_RECEIVE_NOPACKET;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();

        interFrameTimeout = System.currentTimeMillis() + timeout;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;


        if (DEBUG>=1) {
            System.out.println("KV_DEBUG> timeout=" + timeout);
        }

        resultArrayOutputStream.reset();

        copyEchoBuffer();
        while(true) {
            if ((kar = readIn()) != -1) {

                if (DEBUG >= 2) {
                    System.out.print(",0x");
                    ProtocolUtils.outputHex(kar);
                }
                switch(state) {

                    case STATE_RECEIVE_NOPACKET: {

                       if (kar==getEVENT_START_FLAG()) {
                           state = STATE_RECEIVE_BUILDING_PACKET;
                           index=0;
                           resultArrayOutputStream.reset();
                       }

                    } break; // STATE_RECEIVE_NOPACKET

                    case STATE_RECEIVE_ESCAPE: {
                        if ((kar==getEVENT_END_FLAG()) || (kar==getEVENT_ESCAPE_FLAG()) || (kar==getEVENT_START_FLAG())) {
                            resultArrayOutputStream.write(kar);
                            index++;
                            state = STATE_RECEIVE_BUILDING_PACKET;
                        }
                        else {
                            // error! should not receive something else then a flag character here...
                            index=0;
                            resultArrayOutputStream.reset();
                            state = STATE_RECEIVE_NOPACKET;
                        }
                    } break; // STATE_RECEIVE_ESCAPE

                    case STATE_RECEIVE_BUILDING_PACKET: {
                        if (kar==getEVENT_END_FLAG()) {
                            state = STATE_RECEIVE_NOPACKET;

                            // calc CRC
                            byte[] data = resultArrayOutputStream.toByteArray();
                            if (CRCGenerator.calcCRC16(data)==0) {
                                return new Frame(data);
                            } else {
                                throw new ProtocolConnectionException("receivePacketStateMachine() crc error", CRC_ERROR);
                            }

                        }
                        else if (kar==getEVENT_ESCAPE_FLAG()) {
                            state = STATE_RECEIVE_ESCAPE;
                        }
                        else if (kar==getEVENT_START_FLAG()) {
                            index=0;
                            resultArrayOutputStream.reset();
                        }
                        else {
                            resultArrayOutputStream.write(kar);
                            index++;
                        }

                    } break; // STATE_RECEIVE_BUILDING_PACKET

                } // switch(state)

            } // if ((kar = readIn()) != -1)

            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("receivePacketStateMachine() response timeout error",TIMEOUT_ERROR);
            }

            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("receivePacketStateMachine() interframe timeout error",TIMEOUT_ERROR);
            }

        } // while(true)

    } // private byte[] receivePacketStateMachine() throws IOException {

    public byte getTIMEOUT_ERROR() {
        return super.TIMEOUT_ERROR;
    }
    public byte getPROTOCOL_ERROR() {
        return super.PROTOCOL_ERROR;
    }

    public TransmitterStateMachine getTransmitterStateMachine() {
        return transmitterStateMachine;
    }

    private void setTransmitterStateMachine(TransmitterStateMachine transmitterStateMachine) {
        this.transmitterStateMachine = transmitterStateMachine;
    }

    public int getClientAddress() {
        return clientAddress;
    }

    private void setClientAddress(int clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getNodeId() {
        return nodeId;
    }

    private void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getSendSequence() {
        return sendSequence;
    }

    public void setSendSequence(int sendSequence) {
        this.sendSequence = sendSequence;
    }

    public int getReceiveSequence() {
        return receiveSequence;
    }

    public void setReceiveSequence(int receiveSequence) {
        this.receiveSequence = receiveSequence;
    }

    public static int getPACKET_TYPE_SABM() {
        return PACKET_TYPE_SABM;
    }

    public static int getPACKET_TYPE_UA() {
        return PACKET_TYPE_UA;
    }

    public static int getPACKET_TYPE_RR() {
        return PACKET_TYPE_RR;
    }

    public static int getPACKET_TYPE_REJ() {
        return PACKET_TYPE_REJ;
    }

    public static int getPACKET_TYPE_UI() {
        return PACKET_TYPE_UI;
    }

    public static int getPACKET_TYPE_I() {
        return PACKET_TYPE_I;
    }
    public static int getPACKET_TYPE_IP() {
        return PACKET_TYPE_I|0x08;
    }

    public ReceiverStateMachine getReceiverStateMachine() {
        return receiverStateMachine;
    }

    private void setReceiverStateMachine(ReceiverStateMachine receiverStateMachine) {
        this.receiverStateMachine = receiverStateMachine;
    }

}