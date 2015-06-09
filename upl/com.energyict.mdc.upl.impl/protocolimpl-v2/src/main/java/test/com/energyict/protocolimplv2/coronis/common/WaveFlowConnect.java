package test.com.energyict.protocolimplv2.coronis.common;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;

public class WaveFlowConnect {

    private final int timeout;
    private final ComChannel comChannel;
    private final int retries;
    private boolean connected = false;

    /**
     * Reference to the escape command factory. This factory allows calling
     * wavenis protocolstack specific commands if implemented...
     */
    private EscapeCommandFactory escapeCommandFactory;

    public EscapeCommandFactory getEscapeCommandFactory() {
        return escapeCommandFactory;
    }

    public WaveFlowConnect(final ComChannel comChannel, final int timeout, final int retries) {
        this.comChannel = comChannel;
        this.timeout = timeout;
        this.retries = retries;
        escapeCommandFactory = new EscapeCommandFactory(this);
        escapeCommandFactory.setWavenisStackConfigRFResponseTimeout(timeout);
    }

    public byte[] readRadioAddress() {
        return escapeCommandFactory.getRadioAddress();
    }

    public byte[] sendEscapeData(byte[] bytes) {
        int retry = 0;
        while (true) {
            //try {
                write(bytes);
                return readAll();
            //} catch (CommunicationException e) {  //TODO revise, don't use comserver exceptions
            //    if (retry++ >= retries) {
            //        IOException exception = new IOException(e.getMessage());
            //        throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(exception, retries + 1);
            //    }
            //}
        }
    }

    /**
     * Read in all available bytes.
     * If no bytes are available for a certain amount of time, an exception is thrown.
     */
    private byte[] readAll() {
        comChannel.startReading();
        long timeoutMoment = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < timeoutMoment) {
            int available = comChannel.available();
            if (available > 0) {
                delay(50);
                byte[] response = new byte[available];
                comChannel.read(response);
                return response;
            } else {
                delay(50);
            }
        }
        IOException exception = new IOException(getExtraInfo() + "Didn't receive a response.");
        throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(exception, 1);
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
        }
    }

    private void write(byte[] bytes) {
        comChannel.startWriting();
        comChannel.write(bytes);
    }

    public byte[] sendData(byte[] bytes) {
        int retry = 0;
        int communicationAttemptNr = 0;
        while (true) {
            //try {
                escapeCommandFactory.setWavenisStackCommunicationAttemptNr(communicationAttemptNr);
                write(bytes);
                byte[] response = readAll();
                connected = true;
                return response;
            //} catch (CommunicationException e) {  //TODO revise, don't use comserver exceptions
            //    communicationAttemptNr++;
            //    if (retry++ >= retries) {
            //        IOException exception = new IOException(e.getMessage());
            //        throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(exception, retries + 1);
            //    }
            //}
        }
    }

    /**
     * Log extra info if the meter was never reached.
     *
     * @return extra info
     */
    private String getExtraInfo() {
        return !connected ? "Couldn't reach RF module! " : "";
    }
}