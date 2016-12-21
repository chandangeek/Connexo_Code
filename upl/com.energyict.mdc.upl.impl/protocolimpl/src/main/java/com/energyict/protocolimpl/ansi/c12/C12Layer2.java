/*
 * C12Layer2.java
 *
 * Created on 15 oktober 2005, 15:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * @author Koen
 */
public class C12Layer2 extends Connection implements ProtocolConnection {

    protected static final int DEBUG = 0;
    protected static final long TIMEOUT = 600000;

    protected static final int MULTIPLE_PACKET_TRANSMISSION = 0x80;
    protected static final int MULTIPLE_PACKET_FIRST_PACKET = 0x40;
    protected static final int TOGGLE_BIT = 0x20;


    protected int identity;

    protected int timeout;
    protected int maxRetries;

    protected byte[] previousPacket, packet;
    // layer2 specific locals
    protected int previousControl, control;
    protected int previousSequence, sequence;
    protected boolean multiplePacket;

    int receivedIdentity;
    Integer receivedControl, previousReceivedControl;
    int receivedSequence, previousReceivedSequence;
    int receivedLength;

    // KV_TO_DO gebruik nog implementeren...
    protected NegotiateResponse negotiateResponse = null;
    private Logger logger;
    private int controlToggleBitMode;

    /**
     * Creates a new instance of C12Connection
     */
    public C12Layer2(InputStream inputStream,
                     OutputStream outputStream,
                     int timeout,
                     int maxRetries,
                     long forcedDelay,
                     int echoCancelling,
                     HalfDuplexController halfDuplexController,
                     Logger logger,
                     int controlToggleBitMode) throws ConnectionException {
        super(inputStream, outputStream, forcedDelay, echoCancelling, halfDuplexController);
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.logger = logger;
        this.controlToggleBitMode = controlToggleBitMode;
    }

    public void initStates() {
        previousControl = 0;
        control = 0x20;
        previousSequence = sequence = 0;
        multiplePacket = false;
    }

    /*******************************************************************************************
     * Public methods
     ******************************************************************************************/


    public ResponseData sendRequest(RequestData requestData) throws IOException {

        int retry = 0;

        buildPacket(requestData);

        while (true) {
            try {
                sendOut(getPacket());
                ResponseData responseData = receiveResponseData();
                return responseData;
            } catch (ConnectionException e) {
                int mr = getMaxRetries();
                if (retry++ >= mr) {
                    throw new ProtocolConnectionException("sendCommand() error maxRetries (" + mr + "), " + e.getMessage(), MAX_RETRIES_ERROR);
                }
            }
        }
    }

    public ResponseData sendBytes(byte[] requestData) throws IOException {
        int retry = 0;

        while (true) {
            try {
                sendOut(requestData);
                return receiveResponseData();
            } catch (ConnectionException e) {
                int mr = getMaxRetries();
                if (retry++ >= mr) {
                    throw new ProtocolConnectionException("sendCommand() error maxRetries (" + mr + "), " + e.getMessage(), MAX_RETRIES_ERROR);
                }
            }
        }
    }

    static private final int STATE_WAIT_FOR_START_OF_PACKET = 0;
    static private final int STATE_WAIT_FOR_IDENTITY = 1;
    static private final int STATE_WAIT_FOR_CONTROL = 2;
    static private final int STATE_WAIT_FOR_SEQUENCE_NUMBER = 3;
    static private final int STATE_WAIT_FOR_LENGTH = 4;
    static private final int STATE_WAIT_FOR_DATA = 5;
    static private final int STATE_WAIT_FOR_CRC = 6;

    protected ResponseData receiveResponseData() throws IOException {
        long protocolTimeout, interFrameTimeout;
        int kar;
        int state = STATE_WAIT_FOR_START_OF_PACKET;
        int count = 0;
        int calculatedCrc = 0;
        int receivedCrc = 0;

        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();
        ResponseData responseData = new ResponseData();

        receivedIdentity = 0;
        receivedSequence = 0;
        receivedLength = 0;
        previousReceivedSequence = 0;

        interFrameTimeout = System.currentTimeMillis() + timeout;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;

        resultArrayOutputStream.reset();
        allDataArrayOutputStream.reset();

        if (DEBUG == 1) System.out.println("receiveResponseData(...):");
        copyEchoBuffer();
        while (true) {

            if ((kar = readIn()) != -1) {
                if (DEBUG == 1) {
                    System.out.print(",0x");
                    ProtocolUtils.outputHex(((int) kar));
                }
                allDataArrayOutputStream.write(kar);

                switch (state) {
                    case STATE_WAIT_FOR_START_OF_PACKET: {
                        if (kar == 0xEE) {
                            state = STATE_WAIT_FOR_IDENTITY;
                            interFrameTimeout = System.currentTimeMillis() + timeout;
                        } else if (kar == ACK) {
                            //absorb
                            allDataArrayOutputStream.reset();
                        } else if (kar == NAK) {
                            // KV_TO_DO 
                            allDataArrayOutputStream.reset();
                        } else {
                            // KV_TO_DO 
                            allDataArrayOutputStream.reset();
                        }

                    }
                    break; // STATE_WAIT_FOR_START_OF_PACKET

                    case STATE_WAIT_FOR_IDENTITY: {
                        receivedIdentity = (int) kar;
                        state = STATE_WAIT_FOR_CONTROL;
                    }
                    break; // STATE_WAIT_FOR_IDENTITY

                    case STATE_WAIT_FOR_CONTROL: {
                        previousReceivedControl = receivedControl;
                        receivedControl = (int) kar;

                        // KV_TO_DO check for duplicates...

                        state = STATE_WAIT_FOR_SEQUENCE_NUMBER;
                    }
                    break; // STATE_WAIT_FOR_CONTROL

                    case STATE_WAIT_FOR_SEQUENCE_NUMBER: {
                        previousReceivedSequence = receivedSequence;
                        receivedSequence = (int) kar;

                        // KV_TO_DO check for duplicates...

                        state = STATE_WAIT_FOR_LENGTH;
                        count = 2;
                    }
                    break; // STATE_WAIT_FOR_SEQUENCE_NUMBER

                    case STATE_WAIT_FOR_LENGTH: {
                        if (count == 2) {
                            receivedLength = (int) (kar << 8);
                            count--;
                        } else {
                            receivedLength |= (int) (kar);
                            state = STATE_WAIT_FOR_DATA;
                        }
                    }
                    break; // STATE_WAIT_FOR_LENGTH

                    case STATE_WAIT_FOR_DATA: {
                        resultArrayOutputStream.write(kar);
                        if (receivedLength-- <= 1) {
                            calculatedCrc = CRCGenerator.calcHDLCCRC(allDataArrayOutputStream.toByteArray());
                            state = STATE_WAIT_FOR_CRC;
                            count = 2;
                        }

                    }
                    break; // STATE_WAIT_FOR_DATA

                    case STATE_WAIT_FOR_CRC: {
                        // validate CRC
                        byte[] data = allDataArrayOutputStream.toByteArray();

                        if (count == 2) {
                            receivedCrc = (int) (kar << 8);
                            count--;
                        } else {
                            receivedCrc |= (int) (kar);
                            if (receivedCrc == calculatedCrc) {
                                // Bugfix CRM TKT-30314-S6563
                                // In some cases, the device sends an invalid response missing the last (2th CRC) byte!
                                // When in this case, the protocol will stay in the loop (readIn() == -1), cause it expects still 1 byte to be read.
                                // -> No ACK is send -> the device will do a retransmit
                                // First byte of retransmit = the missing byte (2th CRC byte).
                                // All other bytes of retransmit (= duplicate copy of the response) can be discarded.
                                flushInputStream();

                                sendOut(ACK);
                                if (((receivedControl & MULTIPLE_PACKET_TRANSMISSION) == MULTIPLE_PACKET_TRANSMISSION) &&
                                        (receivedSequence > 0)) {
                                    // continue cause we have an ongoing multiple packet transmission here...
                                    if (checkForMatchingControlToggleBit(getControl(), previousReceivedControl, receivedControl)) {
                                        allDataArrayOutputStream.reset();
                                        state = STATE_WAIT_FOR_START_OF_PACKET;
                                        toggleControlBitIfNeeded();    //Ensure the control toggle bit is toggled
                                    } else {
                                        // Mismatch in control toggle bit
                                        // Consider the response as a retry response, which can be ignored
                                        // Instead, re-listen for a valid packet
                                        state = STATE_WAIT_FOR_START_OF_PACKET;
                                        resultArrayOutputStream.reset();
                                        allDataArrayOutputStream.reset();
                                        getLogger().severe("C12 connection layer - Received response during multiple packet transmission with incorrect toggle control bit, the response will be ignored (as it was probably a retry).");
                                    }
                                } else {
                                    if (checkForMatchingControlToggleBit(getControl(), previousReceivedControl, receivedControl)) {
                                        responseData.setData(resultArrayOutputStream.toByteArray());
                                        return responseData;
                                    } else {
                                        // Mismatch in control toggle bit
                                        // Consider the response as a retry response, which can be ignored
                                        // Instead, re-listen for a valid packet
                                        state = STATE_WAIT_FOR_START_OF_PACKET;
                                        resultArrayOutputStream.reset();
                                        allDataArrayOutputStream.reset();
                                        getLogger().severe("C12 connection layer - Received response with incorrect toggle control bit, the response will be ignored (as it was probably a retry).");
                                    }
                                }
                            } else {
                                throw new ProtocolConnectionException("receiveFrame() response crc error", CRC_ERROR);
                            }
                        }
                    }
                    break; // STATE_WAIT_FOR_CRC
                } // switch(iState)
            } // if ((iNewKar = readIn()) != -1)
            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveFrame() response timeout error",TIMEOUT_ERROR);
            }
            if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error",TIMEOUT_ERROR);
            }
        } // while(true)
    }

    private boolean checkForMatchingControlToggleBit(int sendControl, Integer previousReceivedControl, Integer receivedControl) {
        if (controlToggleBitMode == 1) {
            return (receivedControl & 0x20) == (sendControl & 0x20);
        } else if (controlToggleBitMode == 2) {
            return (previousReceivedControl == null) // If previousReceivedControl is 'null'(which should be only the case for the first response we receive), then return true
                                                     // as there is off course no previous received control to validate against
                    || (receivedControl & 0x20) != (previousReceivedControl & 0x20); // Received control should be different from the previous one
        } else {
            return true; // Don't validate control toggle bit
        }
    }

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

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException, ProtocolConnectionException {
        setIdentity(Integer.parseInt(nodeId));
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }


    /*******************************************************************************************
     * Private methods
     ******************************************************************************************/
    static protected final int HEADER_LENGTH = 6;
    static protected final int CRC_LENGTH = 2;
    static protected final int LENGTH_OFFSET = 4;

    protected void buildPacket(RequestData requestData) {
        byte[] data = requestData.getAssembledData();
        packet = new byte[data.length + HEADER_LENGTH + CRC_LENGTH];
        System.arraycopy(data, 0, packet, HEADER_LENGTH, data.length);
        packet[0] = (byte) 0xEE;
        packet[1] = (byte) getIdentity(); //0x40; // KV_DEBUG was 0 // changed to use nodeaddress 14/02/2006
        buildControl(false);
        packet[2] = (byte) getControl();
        buildSequence();
        packet[3] = (byte) getSequence();
        packet[LENGTH_OFFSET] = (byte) ((data.length >> 8) & 0xFF);
        packet[LENGTH_OFFSET + 1] = (byte) (data.length & 0xFF);
        int crc = CRCGenerator.calcHDLCCRC(packet, packet.length - 2);
        packet[packet.length - 2] = (byte) ((crc >> 8) & 0xFF);
        packet[packet.length - 1] = (byte) (crc & 0xFF);
        //  save everything...
        previousControl = getControl();
        previousSequence = getSequence();
        previousPacket = packet;
    }

    /*
     * Layer 2's control byte
     * bit 7: if true, then this packet is part of a multipacket transmission
     * bit 6: if true, then this is the first packet of a multipacket transmission
     * bit 5: should toggle for each new packet send... retransmitted packets keep the same state as
     *        the original packet sent
     */
    protected int getControl() {
        return control;
    }

    protected void buildControl(boolean firstMultipleTransmissionPacket) {
        if (isMultiplePacket()) {
            control |= MULTIPLE_PACKET_TRANSMISSION;
        } else {
            control &= (MULTIPLE_PACKET_TRANSMISSION ^ 0xFF);
        }

        if (firstMultipleTransmissionPacket) {
            control |= MULTIPLE_PACKET_FIRST_PACKET;
        } else {
            control &= (MULTIPLE_PACKET_FIRST_PACKET ^ 0xFF);
        }

        boolean toggleBit = ((control & TOGGLE_BIT) == TOGGLE_BIT);
        if (toggleBit) {
            control &= (TOGGLE_BIT ^ 0xFF);
        } else {
            control |= TOGGLE_BIT;
        }
    }

    protected void toggleControlBitIfNeeded() {
        if (controlToggleBitMode == 1) {
            boolean toggleBit = ((control & TOGGLE_BIT) == TOGGLE_BIT);
            if (toggleBit) {
                control &= (TOGGLE_BIT ^ 0xFF);
            } else {
                control |= TOGGLE_BIT;
            }
        }
        // Else, if 0 (= control toggle bit validation disabled) or
        // if 2 (= separate Tx/Rx control toggle bit), toggle should not be done
    }

    /*
     * Decremented for each new packet sent. The first packet of a multiple packet transmission have
     * total number of packets - 1. 0 means the last packet or a single  packet to send.
     */
    protected int getSequence() {
        return sequence;
    }

    protected void buildSequence() {
        if (sequence-- < 1) {
            sequence = 0;
        }
    }

    protected byte[] getPreviousPacket() {
        return previousPacket;
    }

    protected int getPreviousControl() {
        return previousControl;
    }

    public int getPreviousSequence() {
        return previousSequence;
    }

    public boolean isMultiplePacket() {
        return multiplePacket;
    }

    public void setMultiplePacket(boolean multiplePacket) {
        this.multiplePacket = multiplePacket;
    }

    public byte[] getPacket() {
        return packet;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setNegotiateResponse(NegotiateResponse negotiateResponse) {
        this.negotiateResponse = negotiateResponse;

    }

    public NegotiateResponse getNegotiateResponse() {
        return negotiateResponse;
    }

    public int getIdentity() {
        return identity;
    }

    public void setIdentity(int identity) {
        this.identity = identity;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }
}
