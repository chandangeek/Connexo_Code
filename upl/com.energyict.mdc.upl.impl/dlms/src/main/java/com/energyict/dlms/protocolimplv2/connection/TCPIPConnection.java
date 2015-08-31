package com.energyict.dlms.protocolimplv2.connection;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ServerComChannel;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Class that implements the TCPIP transport layer wrapper protocol.
 * Use this for all V2 protocols.
 */
public class TCPIPConnection implements DlmsV2Connection {

    private static final long TIMEOUT = 300000;
    private static final int WRAPPER_VERSION = 0x0001;
    private final ComChannel comChannel;

    private boolean boolTCPIPConnected;

    private int maxRetries;
    private int clientAddress;
    private int serverAddress;
    private long timeout;
    private long forceDelay;
    private boolean switchAddresses = false;
    private boolean useGeneralBlockTransfer;
    private int generalBlockTransferWindowSize;
    private boolean usePolling;

    /**
     * The current retry count - 0 = first try / 1 = first retry / ...
     */
    private int currentRetryCount = 0;

    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler;
    private HHUSignOnV2 hhuSignOn = null;
    private String meterId = "";

    public TCPIPConnection(ComChannel comChannel, CommunicationSessionProperties properties) {
        this.comChannel = comChannel;
        this.maxRetries = properties.getRetries();
        this.timeout = properties.getTimeout();
        this.clientAddress = properties.getClientMacAddress();
        this.serverAddress = properties.getServerUpperMacAddress();
        this.forceDelay = properties.getForcedDelay();
        setSwitchAddresses(properties.isSwitchAddresses());
        this.boolTCPIPConnected = false;
        this.invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();
        this.useGeneralBlockTransfer = properties.useGeneralBlockTransfer();
        this.generalBlockTransferWindowSize = properties.getGeneralBlockTransferWindowSize();
        this.usePolling = properties.isUsePolling();
    }

    public long getForceDelay() {
        return this.forceDelay;
    }

    /**
     * Method that requests a MAC connection for the TCPIP layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     */
    public void connectMAC() {
        if (!this.boolTCPIPConnected) {
            if (this.hhuSignOn != null) {
                this.hhuSignOn.signOn("", this.meterId);
            }
            this.boolTCPIPConnected = true;
        }
    }

    /**
     * Method that requests a MAC disconnect for the TCPIP layer.
     */
    public void disconnectMAC() {
        this.boolTCPIPConnected = false;
    }

    /**
     * Listen for a while, to receive a response from the meter
     *
     * @throws IOException       in case of a problem with the communication (e.g. timeout)
     * @throws ProtocolException in case of a response that contains unexpected data
     */
    private WPDU receiveData() throws IOException {
        long protocolTimeout, interFrameTimeout;
        int kar;
        State state = State.STATE_HEADER_VERSION;
        int count = 0;
        WPDU wpdu = null;

        interFrameTimeout = System.currentTimeMillis() + this.timeout;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;

        comChannel.startReading();

        if (!usePolling) {
            wpdu = new WPDU();

            ByteBuffer header = readHeader();
            readVersion(wpdu, header);
            readSourceField(wpdu, header);
            readDestinationField(wpdu, header);
            int length = (header.getShort()  & 0x0FFFF);
            wpdu.setLength(length);

            byte[] frame = new byte[length];
            int readBytes = readFixedNumberOfBytes(frame);
            if (readBytes != length) {
                throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Attempted to read out full frame (" + length + " bytes), but received " + readBytes + " bytes instead..."));
            }

            byte[] hdlcLegacyBytes = new byte[3];
            wpdu.setData(ProtocolTools.concatByteArrays(hdlcLegacyBytes, frame));

            return wpdu;
        } else {
            ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
            resultArrayOutputStream.reset();

            while (true) {
                if ((kar = readIn()) != -1) {
                    switch (state) {

                    /*
                     * Read the header version of the WPDU packet (2 bytes, and should be 0x0001)
                     */
                        case STATE_HEADER_VERSION: {
                            if (count == 0) {
                                wpdu = new WPDU();
                                wpdu.setVersion(kar);
                                count++;
                            } else {
                                wpdu.setVersion(wpdu.getVersion() * 256 + kar);
                                count = 0;
                                if (wpdu.getVersion() == WRAPPER_VERSION) {
                                    state = State.STATE_HEADER_SOURCE;
                                } else {
                                    throw new ProtocolException("Received WPDU with wrong WPDU version! " +
                                            "Expected [" + WRAPPER_VERSION + "] but received [" + wpdu.getVersion() + "].");
                                }
                            }
                        }
                        break;

                    /*
                     * Read the header source address of the WPDU packet (2 bytes)
                     */
                        case STATE_HEADER_SOURCE: {
                            if (count == 0) {
                                wpdu.setSource(kar);
                                count++;
                            } else {
                                wpdu.setSource(wpdu.getSource() * 256 + kar);
                                count = 0;

                                int address = this.switchAddresses ? this.serverAddress : this.clientAddress;
                                if (wpdu.getSource() != address) {
                                    state = State.STATE_HEADER_VERSION;
                                    throw new ProtocolException("Received WPDU with wrong source address! Expected [" + address + "] but received [" + wpdu.getSource() + "].");
                                } else {
                                    state = State.STATE_HEADER_DESTINATION;
                                }
                            }
                        }
                        break;

                    /*
                     * Read the header destination address of the WPDU packet (2 bytes)
                     */
                        case STATE_HEADER_DESTINATION: {
                            if (count == 0) {
                                wpdu.setDestination(kar);
                                count++;
                            } else {
                                wpdu.setDestination(wpdu.getDestination() * 256 + kar);
                                count = 0;

                                int address = switchAddresses ? this.clientAddress : this.serverAddress;
                                if (wpdu.getDestination() != address) {
                                    state = State.STATE_HEADER_VERSION;
                                    throw new ProtocolException("Received WPDU with wrong destination address! " +
                                            "Expected [" + address + "] but received [" + wpdu.getDestination() + "].");
                                } else {
                                    state = State.STATE_HEADER_LENGTH;
                                }
                            }
                        }
                        break;

                    /*
                     * Read the length header source address of the WPDU packet (2 bytes)
                     */
                        case STATE_HEADER_LENGTH: {
                            if (count == 0) {
                                wpdu.setLength(kar);
                                count++;
                            } else {
                                wpdu.setLength(wpdu.getLength() * 256 + kar);
                                count = wpdu.getLength();

                                // Add padding of 3 bytes to fake the HDLC LLC. Very tricky to reuse all code written in the early days when only HDLC existed...
                                resultArrayOutputStream.write(0);
                                resultArrayOutputStream.write(0);
                                resultArrayOutputStream.write(0);


                                state = State.STATE_DATA;
                            }
                        }
                        break; // case STATE_HEADER_LENGTH

                        case STATE_DATA: {

                            interFrameTimeout = System.currentTimeMillis() + this.timeout;

                            resultArrayOutputStream.write(kar);
                            if (--count <= 0) {
                                wpdu.setData(resultArrayOutputStream.toByteArray());
                                return wpdu;
                            }

                        }
                        break; // STATE_DATA STATE_IDLE

                    }

                }

                if (((System.currentTimeMillis() - protocolTimeout)) > 0) {
                    throw new IOException("receiveResponse() response timeout error");
                }

                if (((System.currentTimeMillis() - interFrameTimeout)) > 0) {
                    throw new IOException("receiveResponse() interframe timeout error");
                }

            } // while(true)
        }
    } // private byte waitForTCPIPFrameStateMachine()

    /**
     * Read in a fixed number of bytes, or throw an IOException in case of a timeout
     */
    private int readFixedNumberOfBytes(byte[] frame) throws IOException {
        final long timeoutMoment = System.currentTimeMillis() + timeout;

        while (comChannel.available() == 0) {
            if (System.currentTimeMillis() > timeoutMoment) {
                throw new IOException("receiveResponse() response timeout error");
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
                }
            }
        }

        return comChannel.read(frame);
    }

    private void readDestinationField(WPDU wpdu, ByteBuffer header) throws ProtocolException {
        wpdu.setDestination(header.getShort() & 0x0FFFF);
        int address = switchAddresses ? this.clientAddress : this.serverAddress;
        if (wpdu.getDestination() != address) {
            throw new ProtocolException("Received WPDU with wrong destination address! Expected [" + address + "] but received [" + wpdu.getDestination() + "].");
        }
    }

    private void readSourceField(WPDU wpdu, ByteBuffer header) throws ProtocolException {
        wpdu.setSource(header.getShort() & 0x0FFFF);
        int address = this.switchAddresses ? this.serverAddress : this.clientAddress;
        if (wpdu.getSource() != address) {
            throw new ProtocolException("Received WPDU with wrong source address! Expected [" + address + "] but received [" + wpdu.getSource() + "].");
        }
    }

    private void readVersion(WPDU wpdu, ByteBuffer header) throws ProtocolException {
        wpdu.setVersion(header.getShort() & 0x0FFFF);
        if (wpdu.getVersion() != WRAPPER_VERSION) {
            throw new ProtocolException("Received WPDU with wrong WPDU version! Expected [" + WRAPPER_VERSION + "] but received [" + wpdu.getVersion() + "].");
        }
    }

    private ByteBuffer readHeader() throws IOException {
        byte[] header = new byte[8];
        int readBytes = readFixedNumberOfBytes(header);
        if (readBytes != 8) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Attempted to read out 8 header bytes but received " + readBytes + " bytes instead..."));
        }
        return ByteBuffer.wrap(header);
    }

    private int readIn() {
        if (comChannel.available() != 0) {
            return comChannel.read();
        } else {
            delay(100);
            return -1;
        }
    }

    /**
     * delay
     *
     * @param lDelay long delay in ms
     */
    protected void delay(long lDelay) {
        try {
            Thread.sleep(lDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
        }
    }

    public void setSwitchAddresses(boolean type) {

        /**
         * GN|260208|: Creation of this method to switch the client an server addresses
         * Should have been correct but Actaris probably switched this and Iskra came last ...
         */
        this.switchAddresses = type;
    }

    public byte[] readResponseWithRetries(byte[] retryRequest) {
        boolean firstRead = true;
        // this.currentTryCount contains the current try number - we should not start again from 0, but continue from current try number

        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[retryRequest.length - 3];
        System.arraycopy(retryRequest, 3, byteRequestBuffer, 0, byteRequestBuffer.length);

        while (true) {
            try {
                WPDU wpdu = new WPDU(this.clientAddress, this.serverAddress, byteRequestBuffer);
                if (firstRead) {
                    firstRead = false;              // In the first iteration, do not send a retry, but start directly reading
                } else {
                    sendOut(wpdu.getFrameData());   // Do send out retry request
                }
                return receiveData().getData();
            } catch (ProtocolException e) {    //Received invalid data, cannot continue...
                throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
            } catch (IOException e) {
                if (this.currentRetryCount++ >= this.maxRetries) {
                    throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, maxRetries + 1);
                }
            }
        }
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) {
        return this.readResponseWithRetries(retryRequest);
    }

    public byte[] sendRawBytes(byte[] data) {
        resetCurrentRetryCount();
        while (true) {
            try {
                sendOut(data);
                return receiveData().getRawData();
            } catch (ProtocolException e) {    //Received invalid data, cannot continue...
                throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
            } catch (IOException e) {
                if (this.currentRetryCount++ >= this.maxRetries) {
                    throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, maxRetries + 1);
                }
            }
        }
    }

    /**
     * Method that sends an information data field and receives an information field.
     *
     * @param data with the information field.
     * @return Response data with the information field.
     */
    public byte[] sendRequest(byte[] data) {
        resetCurrentRetryCount();

        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[data.length - 3];
        System.arraycopy(data, 3, byteRequestBuffer, 0, byteRequestBuffer.length);

        while (true) {
            try {
                WPDU wpdu = new WPDU(this.clientAddress, this.serverAddress, byteRequestBuffer);
                sendOut(wpdu.getFrameData());
                return receiveData().getData();
            } catch (ProtocolException e) {    //Received invalid data, cannot continue...
                throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
            } catch (IOException e) {
                if (this.currentRetryCount++ >= this.maxRetries) {
                    throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, maxRetries + 1);
                }
            }
        }
    }

    private void sendOut(byte[] frameData) {
        DLMSUtils.delay(forceDelay);
        comChannel.startWriting();
        comChannel.write(frameData);
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setRetries(int retries) {
        this.maxRetries = retries;
    }

    public byte[] sendRequest(final byte[] encryptedRequest, boolean isAlreadyEncrypted) {
        return sendRequest(encryptedRequest);
    }

    public void sendUnconfirmedRequest(final byte[] request) {
        resetCurrentRetryCount();

        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[request.length - 3];
        System.arraycopy(request, 3, byteRequestBuffer, 0, byteRequestBuffer.length);

        WPDU wpdu = new WPDU(this.clientAddress, this.serverAddress, byteRequestBuffer);
        sendOut(wpdu.getFrameData());
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
        this.hhuSignOn = (HHUSignOnV2) hhuSignOn;
        this.meterId = meterId;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId, int hhuSignonBaudRateCode) {
        setHHUSignOn(hhuSignOn, meterId);
    }

    public HHUSignOn getHhuSignOn() {
        return this.hhuSignOn;
    }

    private void resetCurrentRetryCount() {
        this.currentRetryCount = 0;
    }

    /**
     * *****************************************************************************************************
     * Invoke-Id-And-Priority byte setting
     * ******************************************************************************************************
     */

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        return this.invokeIdAndPriorityHandler;
    }

    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
        this.invokeIdAndPriorityHandler = iiapHandler;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public int getMaxTries() {
        return getMaxRetries() + 1;
    }

    @Override
    public boolean useGeneralBlockTransfer() {
        return useGeneralBlockTransfer;
    }

    @Override
    public int getGeneralBlockTransferWindowSize() {
        return generalBlockTransferWindowSize;
    }

    @Override
    public void prepareComChannelForReceiveOfNextPacket() {
        comChannel.startWriting();
        if (comChannel instanceof ServerComChannel) {
            ((ServerComChannel) comChannel).sessionCountersStartWriting();
        }
    }

    private enum State {
        STATE_HEADER_VERSION,
        STATE_HEADER_SOURCE,
        STATE_HEADER_DESTINATION,
        STATE_HEADER_LENGTH,
        STATE_DATA
    }

    class WPDU {

        private int version;
        private int source;
        private int destination;
        private int length;
        private byte[] data;

        public WPDU(int source, int destination, byte[] data) {
            this.version = WRAPPER_VERSION;
            this.source = source;
            this.destination = destination;
            this.data = data;
            this.length = data.length;
        }


        public WPDU() {
            this.version = this.source = this.destination = this.length = -1;
        }

        @Override
        public String toString() {
            return ProtocolUtils.outputHexString(this.data);
        }

        public byte[] getFrameData() {
            byte[] result = new byte[0];
            result = ProtocolTools.concatByteArrays(result, ProtocolTools.getBytesFromInt(getVersion(), 2));
            result = ProtocolTools.concatByteArrays(result, ProtocolTools.getBytesFromInt(getSource(), 2));
            result = ProtocolTools.concatByteArrays(result, ProtocolTools.getBytesFromInt(getDestination(), 2));
            result = ProtocolTools.concatByteArrays(result, ProtocolTools.getBytesFromInt(getLength(), 2));
            result = ProtocolTools.concatByteArrays(result, getData());
            return result;
        }

        public int getVersion() {
            return this.version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getSource() {
            return this.source;
        }

        public void setSource(int source) {
            this.source = source;
        }

        public int getDestination() {
            return this.destination;
        }

        public void setDestination(int destination) {
            this.destination = destination;
        }

        public int getLength() {
            return this.length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public byte[] getData() {
            return this.data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public byte[] getRawData() {
            byte[] result = new byte[0];
            result = ProtocolTools.concatByteArrays(result, ProtocolTools.getBytesFromInt(getVersion(), 2));
            result = ProtocolTools.concatByteArrays(result, ProtocolTools.getBytesFromInt(getSource(), 2));
            result = ProtocolTools.concatByteArrays(result, ProtocolTools.getBytesFromInt(getDestination(), 2));
            result = ProtocolTools.concatByteArrays(result, ProtocolTools.getBytesFromInt(getLength(), 2));
            result = ProtocolTools.concatByteArrays(result, ProtocolUtils.getSubArray(getData(), 3));   //Strip HDLC 3 leading bytes again
            return result;
        }
    }
}