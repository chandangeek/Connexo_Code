package com.energyict.dlms;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.protocolimplv2.connection.RetryRequestPreparation.RetryRequestPreparationConsumer;
import com.energyict.dlms.protocolimplv2.connection.RetryRequestPreparation.RetryRequestPreparationHandler;
import com.energyict.protocol.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version  1.0
 * @author Koenraad Vanderschaeve
 * <P>
 *         <B>Description :</B><BR>
 *         Class that implements the TCPIP transport layer wrapper protocol.
 */

public class TCPIPConnection extends Connection implements DLMSConnection, RetryRequestPreparationConsumer {

    private final Logger logger;

    private static final byte DEBUG = 0;
    private static final long TIMEOUT = 300000;
    private static final int WRAPPER_VERSION = 0x0001;

    // TCPIP specific
    // Sequence numbering
    private boolean boolTCPIPConnected;

    private int maxRetries;
    private int clientAddress;
    private int serverAddress;
    long timeout;
    private long forceDelay;

    private int iskraWrapper = 0;
    private boolean incrementFrameCounterForRetries;

    /**
     * The current retry count - 0 = first try / 1 = first retry / ...
     */
    private int currentRetryCount = 0;

    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler;
    private RetryRequestPreparationHandler retryRequestPreparationHandler;

    public TCPIPConnection(InputStream inputStream,
                           OutputStream outputStream,
                           int timeout,
                           int forceDelay,
                           int maxRetries,
                           int clientAddress,
                           int serverAddress,
                           Logger logger) throws IOException {
        this(inputStream, outputStream, timeout, forceDelay, maxRetries, clientAddress, serverAddress, false, logger);
    }

    public TCPIPConnection(InputStream inputStream,
                           OutputStream outputStream,
                           int timeout,
                           int forceDelay,
                           int maxRetries,
                           int clientAddress,
                           int serverAddress,
                           boolean incrementFrameCounterForRetries,
                           Logger logger) throws IOException {
        super(inputStream, outputStream);
        this.logger = logger != null ? logger : Logger.getLogger(TCPIPConnection.class.getName());
        this.maxRetries = maxRetries;
        this.timeout = timeout;
        this.clientAddress = clientAddress;
        this.serverAddress = serverAddress;
        this.forceDelay = forceDelay;
        this.boolTCPIPConnected = false;
        this.invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();
        this.incrementFrameCounterForRetries = incrementFrameCounterForRetries;
    }

    public long getForceDelay() {
        return this.forceDelay;
    }

    public void setSNRMType(int type) {
        // absorb...
    }

    /**
     * Method that requests a MAC connection for the TCPIP layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     */
    public void connectMAC() throws IOException {
        if (this.boolTCPIPConnected == false) {
            if (this.hhuSignOn != null) {
                this.hhuSignOn.signOn("", this.meterId);
            }
            this.boolTCPIPConnected = true;
        }
    }

    /**
     * Method that requests a MAC disconnect for the TCPIP layer.
     */
    public void disconnectMAC() throws IOException {
        if (this.boolTCPIPConnected == true) {
            this.boolTCPIPConnected = false;
        } // if (boolTCPIPConnected==true)

    }

    private enum State {
        STATE_HEADER_VERSION,
        STATE_HEADER_SOURCE,
        STATE_HEADER_DESTINATION,
        STATE_HEADER_LENGTH,
        STATE_DATA
    }

    private WPDU receiveData() throws IOException {
        long protocolTimeout, interFrameTimeout;
        int kar;
        State state = State.STATE_HEADER_VERSION;
        int count = 0;
        WPDU wpdu = null;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();

        interFrameTimeout = System.currentTimeMillis() + this.timeout;
        protocolTimeout = System.currentTimeMillis() + this.TIMEOUT;

        resultArrayOutputStream.reset();
        copyEchoBuffer();

        while (true) {
            if ((kar = readIn()) != -1) {
                if (DEBUG >= 2) {
                    ProtocolUtils.outputHex(kar);
                }

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
                            if (wpdu.getVersion() == this.WRAPPER_VERSION) {
                                state = State.STATE_HEADER_SOURCE;
                            } else {
                                logger.warning("Received WPDU with wrong WPDU version! " +
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

                            int address = this.iskraWrapper == 1 ? this.serverAddress : this.clientAddress;
                            if (wpdu.getSource() != address) {
                                logger.warning("Received WPDU with wrong source address! Expected [" + address + "] but received [" + wpdu.getSource() + "].");
                                state = State.STATE_HEADER_VERSION;
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

                            int address = iskraWrapper == 1 ? this.clientAddress : this.serverAddress;
                            if (wpdu.getDestination() != address) {
                                logger.warning("Received WPDU with wrong destination address! " +
                                        "Expected [" + address + "] but received [" + wpdu.getDestination() + "].");
                                state = State.STATE_HEADER_VERSION;
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
                        if (DEBUG >= 2) {
                            System.out.println("KV_DEBUG> receive " + count + "bytes of data");
                        }
                        if (count == 0) {
                            wpdu.setLength(kar);
                            count++;
                        } else {
                            wpdu.setLength(wpdu.getLength() * 256 + kar);
                            count = wpdu.getLength();

                            if (DEBUG >= 2) {
                                System.out.println("KV_DEBUG> count=" + count);
                            }

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
                            if (DEBUG >= 1) {
                                System.out.println("KV_DEBUG> RX-->" + wpdu);
                            }
                            return wpdu;
                        }

                    }
                    break; // STATE_DATA STATE_IDLE

                }

            }

            if (((System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ConnectionException("receiveResponse() response timeout error", this.TIMEOUT_ERROR);
            }

            if (((System.currentTimeMillis() - interFrameTimeout)) > 0) {
                throw new ConnectionException("receiveResponse() interframe timeout error", this.TIMEOUT_ERROR);
            }

        } // while(true)
    } // private byte waitForTCPIPFrameStateMachine()


    public void setIskraWrapper(int type) {

        /**
         * GN|260208|: Creation of this method to switch the client an server addresses
         * Should have been correct but Actaris probably switched this and Iskra came last ...
         */
        this.iskraWrapper = type;
    }

    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        boolean firstRead = true;
        // this.currentTryCount contains the current try number - we should not start again from 0, but continue from current try number

        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[retryRequest.length - 3];
        System.arraycopy(retryRequest, 3, byteRequestBuffer, 0, byteRequestBuffer.length);

        while (true) {
            try {
                WPDU wpdu = new WPDU(this.clientAddress, this.serverAddress, byteRequestBuffer);
                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG> TX-->" + wpdu);
                }
                if (firstRead) {
                    firstRead = false;              // In the first iteration, do not send a retry, but start directly reading
                } else {
                    sendOut(wpdu.getFrameData());   // Do send out retry request
                }
                return receiveData().getData();
            } catch (ConnectionException e) {
                this.logger.warning(e.getMessage());
                if (this.currentRetryCount++ >= this.maxRetries) {
                    throw new NestedIOException(e, "sendRequest, IOException");
                } else {
                    retryRequest = getRetryRequestPreparationHandler().prepareRetryRequest(retryRequest);
                }
            }
        }
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {
        return this.readResponseWithRetries(retryRequest);
    }

    public byte[] sendRawBytes(byte[] data) throws IOException {
        resetCurrentRetryCount();
        while (true) {
            try {
                sendOut(data);
                return receiveData().getRawData();
            } catch (ConnectionException e) {
                this.logger.warning(e.getMessage());
                if (this.currentRetryCount++ >= this.maxRetries) {
                    throw new NestedIOException(e, "sendRawBytes, IOException");
                } else {
                    data = getRetryRequestPreparationHandler().prepareRetryRequest(data);
                }
            }
        }
    }

    /**
     * Method that sends an information data field and receives an information field.
     * @param data with the information field.
     * @return Response data with the information field.
     */
    public byte[] sendRequest(byte[] data) throws IOException {
        resetCurrentRetryCount();

        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[data.length - 3];
        System.arraycopy(data, 3, byteRequestBuffer, 0, byteRequestBuffer.length);

        while (true) {
            try {
                WPDU wpdu = new WPDU(this.clientAddress, this.serverAddress, byteRequestBuffer);
                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG> TX-->" + wpdu);
                }
                sendOut(wpdu.getFrameData());
                return receiveData().getData();
            } catch (ConnectionException e) {
                this.logger.warning(e.getMessage());
                if (this.currentRetryCount++ >= this.maxRetries) {
                    throw new NestedIOException(e, "sendRequest, IOException");
                } else {
                    data = getRetryRequestPreparationHandler().prepareRetryRequest(data);
                }
            }
        }

    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setRetries(int retries) {
        this.maxRetries = retries;
    }

    public byte[] sendRequest(final byte[] encryptedRequest, boolean isAlreadyEncrypted) throws IOException {
        return sendRequest(encryptedRequest);
    }

    public void sendUnconfirmedRequest(byte[] request) throws IOException {
        resetCurrentRetryCount();

        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[request.length - 3];
        System.arraycopy(request, 3, byteRequestBuffer, 0, byteRequestBuffer.length);

        while (true) {
            try {
                WPDU wpdu = new WPDU(this.clientAddress, this.serverAddress, byteRequestBuffer);
                sendOut(wpdu.getFrameData());
                return;
            } catch (ConnectionException e) {
                this.logger.log(Level.WARNING, e.getMessage(), e);
                if (this.currentRetryCount++ >= this.maxRetries) {
                    throw new NestedIOException(e, "sendRequest, IOException");
                } else {
                    request = getRetryRequestPreparationHandler().prepareRetryRequest(request);
                }

                this.logger.log(Level.WARNING, "Sleeping for [" + timeout + " ms] until next try ...");
                DLMSUtils.delay(timeout);

            }
        }
    }

    HHUSignOn hhuSignOn = null;
    String meterId = "";
    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
        this.hhuSignOn = hhuSignOn;
        this.meterId = meterId;
    }

	public void setHHUSignOn(HHUSignOn hhuSignOn,String meterId, int hhuSignonBaudRateCode) {
        setHHUSignOn(hhuSignOn, meterId);
	}

    public HHUSignOn getHhuSignOn() {
        return this.hhuSignOn;
    }

    class WPDU {

        private int version;
        private int source;
        private int destination;
        private int length;
        private byte[] data;

        public WPDU(int source, int destination, byte[] data) {
            this.version = TCPIPConnection.this.WRAPPER_VERSION;
            this.source = source;
            this.destination = destination;
            this.data = data;
            this.length = data.length;
        }


        public WPDU() {
            this.version = this.source = this.destination = this.length = -1;
        }

        public WPDU(byte[] byteBuffer) throws IOException {
            int offset = 0;
            setVersion(ProtocolUtils.getInt(byteBuffer, offset, 2));
            offset += 2;
            setSource(ProtocolUtils.getInt(byteBuffer, offset, 2));
            offset += 2;
            setDestination(ProtocolUtils.getInt(byteBuffer, offset, 2));
            offset += 2;
            setLength(ProtocolUtils.getInt(byteBuffer, offset, 2));
            offset += 2;
            setData(ProtocolUtils.getSubArray2(byteBuffer, offset, getLength() - 8));
        }

        @Override
        public String toString() {
            return ProtocolUtils.outputHexString(this.data);
        }

        public byte[] getFrameData() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeShort(getVersion());
            daos.writeShort(getSource());
            daos.writeShort(getDestination());
            daos.writeShort(getLength());
            daos.write(getData());
            return baos.toByteArray();
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

        public byte[] getRawData() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeShort(getVersion());
            daos.writeShort(getSource());
            daos.writeShort(getDestination());
            daos.writeShort(getLength());
            daos.write(ProtocolUtils.getSubArray(getData(), 3));
            return baos.toByteArray();
        }
    }

    private void resetCurrentRetryCount() {
        this.currentRetryCount = 0;
    }

    @Override
    public boolean incrementFrameCounterForRetries() {
        return incrementFrameCounterForRetries;
    }

    public void setRetryRequestPreparationHandler(RetryRequestPreparationHandler retryRequestPreparationHandler) {
        this.retryRequestPreparationHandler = retryRequestPreparationHandler;
    }

    public RetryRequestPreparationHandler getRetryRequestPreparationHandler() {
        if (this.retryRequestPreparationHandler == null) {
            this.retryRequestPreparationHandler = new RetryRequestPreparationHandler() {
                @Override
                public byte[] prepareRetryRequest(byte[] originalRequest) throws IOException {
                    return originalRequest; // Return the original request as-is
                }
            };
        }
        return retryRequestPreparationHandler;
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
}