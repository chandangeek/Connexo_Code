package com.energyict.dlms;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 5/16/12
 * Time: 9:36 AM
 */
public class IF2Connection implements DLMSConnection {

    /**
     * If this connection ID is used, the IF2Connection will switch from 9600 baud to 115200 baud
     */
    public static final int HIGH_SPEED_CID = 14;

    /**
     * Default baudrate of 9600 baud
     */
    public static final int NORMAL_BAUDRATE = 9600;

    /**
     * High speed baudrate of 115200 baud
     */
    public static final int HIGH_BAUDRATE = 115200;

    /**
     * The configured or default invokeIdAndPriorityHandler
     */
    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler;

    /**
     * The HHUSignOn object used during the connect and disconnect
     */
    private HHUSignOn hhuSignOn = null;

    /**
     * The meter id to use when connecting the HHU
     */
    private String meterId = null;

    /**
     * The connection state of this IF2Connection
     */
    private boolean connected;

    /**
     * The maximum number of retries if there occurred an error while reading or writing from/to the IF2Connection
     */
    private int maxRetries;

    /**
     * The current try count, 0-based
     */
    protected int currentTryCount;

    /**
     * The timeout in milli seconds to wait for a valid response
     */
    private long timeout;

    /**
     * The address used to identify the client
     */
    private final int clientAddress;

    /**
     * The address used to identify the server (remote IF2 device)
     */
    private final int serverAddress;

    /**
     * The connection ID used to communicate to the module. Apollo meter normally uses CID 2, but we'll use a special id 14.
     * This id allows us to send bigger packets (1024 bytes instead of 300), and to switch to a higher baudrate during the connect (115200)
     * <pre>
     *  - IF2CONN_CLIENT_INT  (1)   -> chosen by us, for module/meter connection
     *  - IF2CONN_SERVER      (2)   -> chosen by Apollo
     *  - IF2CONN_CLIENT_EXT  (3)   -> chosen by us, for head-end/meter connection
     *  - IF2CONN_SERVER_EIS  (14)  -> chosen by us. Special IF2 connection ID that overrules default IF2 properties for faster use in EICT tools
     * </pre>
     */
    private final int connectionId;

    /**
     * The logger used in this IF2Connection
     */
    private final Logger logger;

    /**
     * This is the IF2 link layer, used to read and write IF2Packets from and to the IF2 interface
     */
    private final IF2LinkLayer if2LinkLayer;

    /**
     * Some devices requires us to switch the client and server addresses.
     */
    private boolean switchAddresses = false;

    /**
     * Create a new IF2 dlms data transport connection with the given parameters
     *
     * @param in           The InputStream to read IF2 packets from
     * @param out          The OutputStream to write IF2 packets to
     * @param timeout      The timeout used while waiting for a correct IF2 response
     * @param retries      The number of retries used wile sending / reading data from the IF2 interface
     * @param forcedDelay  The number of milliseconds to wait after each write
     * @param clientAddr   The client address to use for each IF2 packet
     * @param serverAddr   The server address to use for each IF2 packet
     * @param connectionId The connection ID to use over IF2 (2 = slow, 14 = fast)
     * @param logger       The logger to use during the connection.
     *                     The IF2Connection will create a new logger if the given logger was null
     */
    public IF2Connection(InputStream in, OutputStream out, int timeout, int retries, int forcedDelay, int deviceBufferSize, int clientAddr, int serverAddr, int connectionId, Logger logger) {
        this.maxRetries = retries;
        this.timeout = timeout;
        this.clientAddress = clientAddr;
        this.serverAddress = serverAddr;
        this.connectionId = connectionId;
        this.logger = logger != null ? logger : Logger.getLogger(getClass().getName());
        this.invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();
        this.if2LinkLayer = new IF2LinkLayer(in, out, timeout, deviceBufferSize <= 0 ? IF2LinkLayer.DEFAULT_DEVICE_BUFFER_SIZE : deviceBufferSize, forcedDelay, logger);
        this.if2LinkLayer.setDebug(true);
        this.connected = false;
    }

    /**
     * Set the 'Hand Held Unit' sign on object, used with some optical connections
     *
     * @param hhuSignOn The HHUSignOn object to use during the connect
     * @param meterId   The id of the meter used to connect to
     */
    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
        this.hhuSignOn = hhuSignOn;
        this.meterId = meterId;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId, int hhuSignonBaudRateCode) {
        setHHUSignOn(hhuSignOn, meterId);
    }

    /**
     * Get the 'Hand Held Unit' sign on object, used with some optical connections
     *
     * @return The HHUSignOn object to use during the connect
     */
    public HHUSignOn getHhuSignOn() {
        return this.hhuSignOn;
    }

    public byte[] sendRawBytes(byte[] data) throws IOException {
        return new byte[0];
    }

    /**
     * This method is not required for IF2, so it's not implemented and does nothing
     *
     * @param type The SNRM type
     */
    public void setSNRMType(int type) {
    }

    /**
     * Some devices switched up the client and server address.
     *
     * @param iskraWrapper Set to 1 to switch the client and server address
     */
    public void setIskraWrapper(int iskraWrapper) {
        this.switchAddresses = iskraWrapper == 1;
    }

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        return this.invokeIdAndPriorityHandler;
    }

    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
        this.invokeIdAndPriorityHandler = iiapHandler;
    }

    /**
     * @return the maximum number of retries
     */
    public int getMaxRetries() {
        return this.maxRetries;
    }

    @Override
    public int getMaxTries() {
        return getMaxRetries() + 1;
    }

    /**
     * Do the HHUSignOn if required, send an ack-power-up and connect request message to the IF2 interface.
     *
     * @throws IOException             If the ack-power-up or connect message fails for some reason
     * @throws DLMSConnectionException This implementation does not use the DLMSConnectionException
     */
    public void connectMAC() throws IOException, DLMSConnectionException {
        if (this.connected == false) {

            if (this.hhuSignOn != null && isHighSpeedEnabled()) {
                this.hhuSignOn.signOn("", this.meterId, NORMAL_BAUDRATE);
            }

//            if2LinkLayer.write(IF2Packet.createAckPowerUp());

            delay(500);

            if2LinkLayer.write(IF2Packet.createConnectRequest(this.connectionId));

            delay(500);

            if (this.hhuSignOn != null && isHighSpeedEnabled()) {
                this.hhuSignOn.signOn("", this.meterId, HIGH_BAUDRATE);
            }

            this.connected = true;
        }
    }

    /**
     * Checks if we're using the {@see IF2Connection#HIGH_SPEED_CID} connection id
     * This enables us to switch from 9600 baud to 115200 baud
     *
     * @return true if high speed is enabled
     */
    public final boolean isHighSpeedEnabled() {
        return connectionId == HIGH_SPEED_CID;
    }

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
        }
    }

    /**
     * Notify the IF2 interface of a disconnect of the reserved CID and mark the connection as disconnected.
     *
     * @throws IOException             If the disconnect message fails for some reason
     * @throws DLMSConnectionException This implementation does not use the DLMSConnectionException
     */
    public void disconnectMAC() throws IOException, DLMSConnectionException {
        if2LinkLayer.write(IF2Packet.createDisconnectRequest(this.connectionId));

        delay(500);

        if (this.hhuSignOn != null) {
            this.hhuSignOn.signOn("", this.meterId, 9600);
        }

        delay(500);

        this.connected = false;
    }

    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        boolean firstRead = true;
        // this.currentTryCount contains the current try number - we should not start again from 0, but continue from current try number

        // Strip the first 3 bytes of the request to get the plain cosem APDU
        final byte[] cosemApdu = DLMSUtils.getSubArray(retryRequest, 3);

        do {
            try {
                if (firstRead) {
                    firstRead = false;      // In the first iteration, do not send a retry, but start directly reading
                } else {
                    if2LinkLayer.write(IF2Packet.createDataIndication(this.connectionId, clientAddress, serverAddress, cosemApdu));     // Do send out retry request
                }
                return doReadResponse();
            } catch (IOException e) {
                this.logger.log(Level.WARNING, "Unable to send and/or receive IF2 packet after [" + this.currentTryCount + "/" + getMaxRetries() + "] retries: " + e.getMessage(), e);
                this.currentTryCount++;
            }
        } while (this.currentTryCount <= getMaxRetries());
        throw new IOException("Unable to send and/or receive IF2 packet after [" + getMaxRetries() + "] retries.");
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {
        return this.readResponseWithRetries(retryRequest);
    }

    /**
     * Write a given request to the IF2 interface, using the correct IF2 command
     * and wait for the response. This is a blocking call and only returns if an
     * answer was received or if the (timeout * retries) was reached.
     *
     * @param request The APDU request (+3 legacy prefix bytes)
     * @return The response from the device (cosem apdu + 3 legacy bytes)
     * @throws IOException If there was an error while reading or writing the IF2 packet after ... retries
     */
    public byte[] sendRequest(byte[] request) throws IOException {
        // Strip the first 3 bytes of the request to get the plain cosem APDU
        final byte[] cosemApdu = DLMSUtils.getSubArray(request, 3);

        resetCurrentTryCount();
        do {
            try {
                return doSendRequest(cosemApdu);
            } catch (IOException e) {
                this.logger.log(Level.WARNING, "Unable to send and/or receive IF2 packet after [" + this.currentTryCount + "/" + getMaxRetries() + "] retries: " + e.getMessage(), e);
                this.currentTryCount++;
            }
        } while (this.currentTryCount <= getMaxRetries());
        throw new IOException("Unable to send and/or receive IF2 packet after [" + getMaxRetries() + "] retries.");
    }

    public byte[] sendRequest(final byte[] encryptedRequest, boolean isAlreadyEncrypted) throws IOException {
        return sendRequest(encryptedRequest);
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

    /**
     * Write a given request to the IF2 interface, using the correct IF2 command
     * and wait for the response. This is a blocking call and only returns if an
     * answer was received or if the (timeout * retries) was reached.
     *
     * @param request The unconfirmed request to send
     * @throws IOException If there occurred an error while sending the request
     */
    public void sendUnconfirmedRequest(final byte[] request) throws IOException {
        // Strip the first 3 bytes of the request to get the plain cosem APDU
        final byte[] cosemApdu = DLMSUtils.getSubArray(request, 3);

        resetCurrentTryCount();
        do {
            try {
                // Wrap the COSEM apdu in an IF2 data indication packet, and send it to the IF2 interface
                if2LinkLayer.write(IF2Packet.createDataIndication(this.connectionId, clientAddress, serverAddress, cosemApdu));
                return;
            } catch (IOException e) {
                this.logger.log(Level.WARNING, "Unable to send and/or receive IF2 packet after [" + this.currentTryCount + "/" + getMaxRetries() + "] retries: " + e.getMessage(), e);
                this.currentTryCount++;

                if (this.currentTryCount <= getMaxRetries()) {
                    this.logger.log(Level.WARNING, "Sleeping for [" + timeout + " ms] until next try ...");
                    delay(timeout);
                }
            }

        } while (this.currentTryCount <= getMaxRetries());
        throw new IOException("Unable to send unconfirmed IF2 packet after [" + getMaxRetries() + "] retries.");
    }

    /**
     * Write a given request to the IF2 interface, using the correct IF2 command
     * and wait for the response. This is a blocking call and only returns if an
     * answer was received or if the timeout was reached. This method takes no retries in account
     *
     * @param cosemApdu The cosem APDU
     * @return The response from the device (cosem apdu + 3 legacy bytes)
     * @throws IOException If there was an error while reading or writing the IF2 packet
     */
    private byte[] doSendRequest(byte[] cosemApdu) throws IOException {
        // Wrap the COSEM apdu in an IF2 data indication packet, and send it to the IF2 interface
        if2LinkLayer.write(IF2Packet.createDataIndication(this.connectionId, clientAddress, serverAddress, cosemApdu));
        return doReadResponse();
    }

    private byte[] doReadResponse() throws IOException {
        // Wait for the data request packet, and answer some other IF2 requests if required (power-up, ping, ...)
        long replyTimeout = System.currentTimeMillis() + this.timeout;
        do {
            IF2Packet packet = if2LinkLayer.read();
            if (packet != null) {
                if (packet.isReportPowerUp()) {
                    //if2LinkLayer.write(IF2Packet.createAckPowerUp());
                } else if (packet.isPingRequest()) {
                    //if2LinkLayer.write(IF2Packet.createPingResponse());
                } else if (packet.isRequestIndication()) {
                    final byte[] content = packet.getData();
                    final byte cid = content[2];
                    if (cid == this.connectionId) {
                        final byte[] cosemReply = DLMSUtils.getSubArray(content, 6);
                        return DLMSUtils.concatByteArrays(new byte[3], cosemReply);
                    }
                }
            }

        } while (replyTimeout > System.currentTimeMillis());
        throw new IOException("Timeout while waiting for IF2 reply.");
    }

    private void resetCurrentTryCount() {
        this.currentTryCount = 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("IF2Connection");
        sb.append("{clientAddress=").append(clientAddress);
        sb.append(", invokeIdAndPriorityHandler=").append(invokeIdAndPriorityHandler);
        sb.append(", hhuSignOn=").append(hhuSignOn);
        sb.append(", meterId='").append(meterId).append('\'');
        sb.append(", connected=").append(connected);
        sb.append(", maxRetries=").append(maxRetries);
        sb.append(", timeout=").append(timeout);
        sb.append(", serverAddress=").append(serverAddress);
        sb.append(", connectionId=").append(connectionId);
        sb.append(", if2LinkLayer=").append(if2LinkLayer);
        sb.append(", switchAddresses=").append(switchAddresses);
        sb.append('}');
        return sb.toString();
    }
}
