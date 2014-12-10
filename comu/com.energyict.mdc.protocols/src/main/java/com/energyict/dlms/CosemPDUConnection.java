package com.energyict.dlms;

import com.energyict.dialer.connection.Connection;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the TCPIP transport layer wrapper protocol.
 * @version 1.0
 */

public class CosemPDUConnection extends Connection implements DLMSConnection {

    private static final Logger logger = Logger.getLogger(CosemPDUConnection.class.getName());

    private static final byte DEBUG = 2;
    private final long TIMEOUT = 300000;


    // TCPIP specific
    // Sequence numbering
    private boolean connected;

    private int maxRetries;
    private int clientAddress;
    private int serverAddress;
    long timeout;
    private long forceDelay;

    private int iskraWrapper = 0;
    protected int currentTryCount = 0;

    private static final byte CLIENT_NO_SECURITY = 0;
    private static final byte CLIENT_LOWLEVEL_SECURITY = 1;

    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler;

    public CosemPDUConnection(InputStream inputStream,
                              OutputStream outputStream,
                              int timeout,
                              int forceDelay,
                              int maxRetries,
                              int clientAddress,
                              int serverAddress) throws IOException {
        super(inputStream, outputStream);
        this.maxRetries = maxRetries;
        this.timeout = timeout;
        this.clientAddress = clientAddress;
        this.serverAddress = serverAddress;
        this.forceDelay = forceDelay;
        connected = false;
        this.invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();

    } // public TCPIPConnection(...)

    public byte[] sendRawBytes(byte[] data) throws IOException {
        return new byte[0];
    }

    public void setSNRMType(int type) {
        // absorb...
    }

    /**
     * Method that requests a MAC connection for the TCPIP layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     *
     * @throws IOException
     */
    public void connectMAC() throws IOException {
        if (connected == false) {
            if (hhuSignOn != null) {
                hhuSignOn.signOn("", meterId);
            }
            connected = true;
        } // if (connected==false)
    } // public void connectMAC() throws IOException

    /**
     * Method that requests a MAC disconnect for the TCPIP layer.
     *
     * @throws IOException
     */
    public void disconnectMAC() throws IOException {
        if (connected == true) {
            connected = false;
        } // if (connected==true)

    } // public void disconnectMAC() throws IOException

    private byte[] receiveData() throws IOException {
        byte[] data;
        long interFrameTimeout;
        copyEchoBuffer();
        interFrameTimeout = System.currentTimeMillis() + timeout;
        while (true) {
            if ((data = readInArray()) != null) {
                if (DEBUG >= 2) {
                    ProtocolUtils.outputHexString(data);
                }
                byte[] dataWithLLC = new byte[data.length + 3];
                System.arraycopy(data, 0, dataWithLLC, 3, data.length);
                return dataWithLLC;
            } // if ((iNewKar = readIn()) != -1)
            if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {
                throw new ConnectionException("receiveData() response timeout error", TIMEOUT_ERROR);
            }

        } // while(true)
    } // private byte waitForTCPIPFrameStateMachine()


    public void setIskraWrapper(int type) {

        /**
         * GN|260208|: Creation of this method to switch the client an server addresses
         * Should have been correct but Actaris probably switched this and Iskra came last ...
         */
        iskraWrapper = type;
    }

    /**
     * Method that sends an information data field and receives an information field.
     *
     * @param data with the information field.
     * @return Response data with the information field.
     * @throws IOException
     */
    public byte[] sendRequest(byte[] data) throws IOException {
        resetCurrentTryCount();

        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[data.length - 3];
        System.arraycopy(data, 3, byteRequestBuffer, 0, byteRequestBuffer.length);

        while (true) {
            try {
                sendOut(byteRequestBuffer);

                byte[] cosemBytes;
                do {
                    cosemBytes = receiveData();
                } while (isEventNotification(cosemBytes));

                return cosemBytes;
            } catch (ConnectionException e) {
                if (this.currentTryCount++ >= maxRetries) {
                    throw new IOException("sendRequest, IOException", e);
                }
            }
        }

    } // public byte[] sendRequest(byte[] byteBuffer) throws TCPIPConnectionException

    public byte[] sendRequest(final byte[] encryptedRequest, boolean isAlreadyEncrypted) throws IOException {
        return sendRequest(encryptedRequest);
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        boolean firstRead = true;
        // this.currentTryCount contains the current try number - we should not start again from 0, but continue from current try number

        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[retryRequest.length - 3];
        System.arraycopy(retryRequest, 3, byteRequestBuffer, 0, byteRequestBuffer.length);

        while (true) {
            try {
                if (firstRead) {
                    firstRead = false;              // In the first iteration, do not send a retry, but start directly reading
                } else {
                    sendOut(byteRequestBuffer);     // Do send out retry request
                }

                byte[] cosemBytes;
                do {
                    cosemBytes = receiveData();
                } while (isEventNotification(cosemBytes));

                return cosemBytes;
            } catch (ConnectionException e) {
                if (this.currentTryCount++ >= maxRetries) {
                    throw new IOException("sendRequest, IOException", e);
                }
            }
        }
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {
        return this.readResponseWithRetries(retryRequest);
    }

    /**
     * Checks if the received packet is an spontaneous request
     *
     * @param rawBytes The raw cosem bytes (with the 3 legacy bytes)
     * @return True if the data is 100% sure a event notification packet
     */
    private boolean isEventNotification(final byte[] rawBytes) {
        if (rawBytes == null) {
            return false;
        }

        if (rawBytes.length <= DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET) {
            return false;
        }

        final byte cosemTag = rawBytes[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET];
        final boolean isEventNotification = cosemTag == DLMSCOSEMGlobals.COSEM_EVENTNOTIFICATIONRESUEST;
        if (isEventNotification) {
            final byte[] cosemBytes = DLMSUtils.getSubArray(rawBytes, DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET, rawBytes.length);
            final String hexValue = DLMSUtils.getHexStringFromBytes(cosemBytes);
            logger.log(Level.FINEST, "Received Event notification: [" + hexValue + "]. Dropping this packet!");
        }
        return isEventNotification;
    }

    public void sendUnconfirmedRequest(final byte[] request) throws IOException {
        resetCurrentTryCount();

        // strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
        byte[] byteRequestBuffer = new byte[request.length - 3];
        System.arraycopy(request, 3, byteRequestBuffer, 0, byteRequestBuffer.length);

        while (true) {
            try {
                sendOut(byteRequestBuffer);
                return;
            } catch (ConnectionException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                if (this.currentTryCount++ >= maxRetries) {
                    throw new IOException("sendRequest, IOException", e);
                }
            }

            logger.log(Level.WARNING, "Sleeping for [" + timeout + " ms] until next try ...");
            DLMSUtils.delay(timeout);

        }

    }

    // KV 18092003
    HHUSignOn hhuSignOn = null;
    String meterId = "";

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
        this.hhuSignOn = hhuSignOn;
        this.meterId = meterId;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId, int hhuSignonBaudRateCode) {
        setHHUSignOn(hhuSignOn, meterId);
    }

    public HHUSignOn getHhuSignOn() {
        return hhuSignOn;
    }

    protected void resetCurrentTryCount() {
        this.currentTryCount = 0;
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

    public long getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setRetries(int retries) {
        this.maxRetries = retries;
    }

    @Override
    public int getMaxTries() {
        return getMaxRetries() + 1;
    }

    public long getForceDelay() {
        return forceDelay;
    }

}