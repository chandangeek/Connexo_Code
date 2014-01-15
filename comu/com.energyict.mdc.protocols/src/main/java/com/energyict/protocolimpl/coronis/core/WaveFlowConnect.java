package com.energyict.protocolimpl.coronis.core;

import com.energyict.dialer.connection.Connection;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class WaveFlowConnect extends Connection implements ProtocolConnection, ProtocolStackLink {

    protected final int timeout;
    protected final Logger logger;
    protected final long forceDelay;
    protected final int retries;
    protected boolean connected = false;
    protected int waveFlowId = -1;

    /**
     * Reference to the escape command factory. this factory allows calling
     * wavenis protocolstack specific commands if implemented...
     */
    private EscapeCommandFactory escapeCommandFactory;

    final public EscapeCommandFactory getEscapeCommandFactory() {
        return escapeCommandFactory;
    }

    public int getWaveFlowId() {
        return waveFlowId;
    }

    public void setWaveFlowId(int waveFlowId) {
        this.waveFlowId = waveFlowId;
    }

    public int getTxCounter() {
        return 0;
    }

    public int getRxCounter() {
        return 0;
    }

    public int getTxFrameCounter() {
        return 0;
    }

    public int getRxFrameCounter() {
        return 0;
    }

    public WaveFlowConnect(final InputStream inputStream, final OutputStream outputStream, final int timeout, final Logger logger, final long forceDelay, final int retries) throws ConnectionException {
        super(inputStream, outputStream, forceDelay, 0);
        this.timeout = timeout;
        this.logger = logger;
        this.forceDelay = forceDelay;
        this.retries = retries;
        escapeCommandFactory = new EscapeCommandFactory(this);
    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException, ProtocolConnectionException {
        // Set the RF response timeout
        escapeCommandFactory.setWavenisStackConfigRFResponseTimeout(timeout);
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

    public byte[] readRadioAddress() throws IOException {
        return escapeCommandFactory.getRadioAddress();
    }

    public byte[] sendEscapeData(byte[] bs) throws NestedIOException, ConnectionException {
        int retry = 0;
        delay(forceDelay);

        while (true) {
            try {
                sendOut(bs);
                break;
            } catch (ConnectionException e) {

                if (e.getMessage().contains("WavenisStackInterruptedException")) {
                    throw new NestedIOException(new InterruptedException());
                }

                if (e.getMessage().contains("error Timeout ")) {
                    if (retry++ >= retries) {
                        throw new ConnectionException("After 3 retries, " + e.getMessage());
                    }

                    logger.warning("Waveflow connect, sendData() retry [" + retry + "] with timeout [" + (timeout * (retry + 1)) + "] ms");
                } else {
                    throw e;
                }
            }
        }

        long timeoutInterFrame = System.currentTimeMillis() + timeout;
        copyEchoBuffer();
        byte[] data;
        do {
            delay(100);
            data = readInArray();

            if (System.currentTimeMillis() - timeoutInterFrame > 0) {
                throw new ProtocolConnectionException("WaveFlowConnect, interframe timeout error", TIMEOUT_ERROR);
            }

        } while (data == null);

        return data;
    }

    public byte[] sendData(byte[] bs) throws IOException, ConnectionException {

        int communicationAttemptNr = 0;
        int retry = 0;
        delay(forceDelay);

        while (true) {
            try {
                escapeCommandFactory.setWavenisStackCommunicationAttemptNr(communicationAttemptNr);
            } catch (IOException e1) {
                logger.severe("Error setting the communicationAttemptNr in the wavenis stack!");
            }

            try {
                sendOut(bs);
                break;
            } catch (ConnectionException e) {

                if (e.getMessage().contains("WavenisStackInterruptedException")) {
                    throw new NestedIOException(new InterruptedException());
                }
                // Only increment communicationAttemptNr after a timeout exception
                if ((e.getMessage().contains("WavenisProtocolTimeoutException")) || (e.getMessage().contains("WavenisProtocolControlTimeoutException"))) {
                    communicationAttemptNr++;
                }

                if (retry++ >= retries) {
                    throw new ConnectionException(getExtraInfo() + "After [" + retries + "] retries, " + e.getMessage());
                } else {
                    logger.warning("Waveflow connect, sendData() retry [" + communicationAttemptNr + "] timeout [" + (timeout * (communicationAttemptNr + 1)) + "] ms");
                }

            } // catch(ConnectionException e)

        } // while(true)


        long timeoutInterFrame = System.currentTimeMillis() + timeout;
        copyEchoBuffer();
        byte[] data;
        do {
            delay(100);
            data = readInArray();

            if (System.currentTimeMillis() - timeoutInterFrame > 0) {
                throw new ProtocolConnectionException("WaveFlowConnect, interframe timeout error", TIMEOUT_ERROR);
            }

        } while (data == null);

        connected = true;
        return data;
    }

    /**
     * Log extra info if the meter was never reached.
     * @return extra info
     */
    private String getExtraInfo() {
        return !connected ? "Couldn't reach RF module! " : "";
    }

    public Logger getLogger() {
        return logger;
    }
}