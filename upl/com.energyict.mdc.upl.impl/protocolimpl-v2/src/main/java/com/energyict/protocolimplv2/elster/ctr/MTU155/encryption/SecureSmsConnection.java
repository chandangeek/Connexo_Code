package com.energyict.protocolimplv2.elster.ctr.MTU155.encryption;

import com.energyict.mdc.channels.sms.ProximusSmsSender;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.CtrConnection;
import com.energyict.protocolimplv2.elster.ctr.MTU155.CtrConnectionState;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRCipheringException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRTimeoutException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.SMSFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Connection class used to drop SMS messages on the Outbound SMS Queue.
 * <p/>
 * Copyrights EnergyICT
 * User: sva
 * Date: 8/03/12
 * Time: 15:02
 */
public class SecureSmsConnection implements CtrConnection<SMSFrame> {

    private static int HEADER_LENGTH = 2;

    /**
     * The ComChannel used for SMS communication with the device
     **/
    private final ComChannel comChannel;
    private final Logger logger;
    private final int timeOut;
    private final int forcedDelay;
    private CTREncryption ctrEncryption;

    /**
     * @param properties
     */
    public SecureSmsConnection(ComChannel comChannel, MTU155Properties properties, Logger logger) {
        this.comChannel = comChannel;
        this.logger = logger;
        this.timeOut = properties.getTimeout();
        this.forcedDelay = properties.getForcedDelay();
        this.ctrEncryption = new CTREncryption(properties);
    }

    /**
     * Construct a proper SMS message out of the SMSFrame and drop it in the SMS Queue.
     *
     * @param frame
     * @return
     * @throws CTRConnectionException
     */
    public SMSFrame sendFrameGetResponse(SMSFrame frame) throws CTRConnectionException {
        try {
            SMSFrame encryptedFrame = (SMSFrame) ctrEncryption.encryptFrame(frame);
            encryptedFrame.setCrc();

            sendFrame(encryptedFrame);
            ProximusSmsSender.ResultType result = readFrame();
            if (result.ordinal() != ProximusSmsSender.ResultType.SUCCESSFUL.ordinal()) {
                // An error occurred while sending out the SMS (either ComServer or broker) -> throw an exception, so we can let the message fail.
                throw new CTRConnectionException(result.getDescription());
            }
            return null;
        } catch (CTRCipheringException e) {
            throw MdcManager.getComServerExceptionFactory().createCipheringException(e);
        }
    }

    private void sendFrame(SMSFrame frame) throws CTRConnectionException {
        try {
            doForcedDelay();
            ensureComChannelIsInWritingMode();
            comChannel.write(frame.getBytes());
        } catch (ComServerExecutionException e) {
            if (MdcManager.getComServerExceptionFactory().isCommunicationException(e)) {
                throw new CTRConnectionException("Unable to send frame.", e);
            } else {
                throw e;
            }
        }
    }

    private void doForcedDelay() {
        if (forcedDelay > 0) {
            ProtocolTools.delay(forcedDelay);
        }
    }

    /**
     * @return
     * @throws CTRConnectionException
     */
    private ProximusSmsSender.ResultType readFrame() throws CTRConnectionException {
        byte[] rawFrame = readRawFrame();
        return ProximusSmsSender.ResultType.getResultTypeFromByteStream(rawFrame);
    }

    private byte[] readRawFrame() throws CTRConnectionException {
        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        try {
            CtrConnectionState state = CtrConnectionState.WAIT_FOR_STX;
            long timeOutMoment = System.currentTimeMillis() + timeOut;
            int messageLength = 0;
            do {
                if (System.currentTimeMillis() > timeOutMoment) {
                    String message = "Timed out while receiving data. State='" + state + "', timeout='" + timeOut + "'.";
                    throw new CTRTimeoutException(message);
                }
                if (!bytesFromDeviceAvailable()) {
                    ProtocolTools.delay(1);
                } else {
                    byte[] buffer = new byte[260];
                    ensureComChannelIsInReadingMode();
                    int len = comChannel.read(buffer);
                    for (int ptr = 0; ptr < len; ptr++) {
                        int readByte = buffer[ptr];
                        readByte &= 0x0FF;
                        switch (state) {
                            case WAIT_FOR_STX:
                                rawBytes.write(readByte);
                                state = CtrConnectionState.READ_MIN_LENGTH;
                                break;
                            case READ_MIN_LENGTH:
                                rawBytes.write(readByte);
                                messageLength = readByte;
                                if (messageLength > 0) {
                                    state = CtrConnectionState.READ_FRAME;
                                } else {
                                    state = CtrConnectionState.FRAME_RECEIVED;
                                }
                                break;
                            case READ_FRAME:
                                rawBytes.write(readByte);
                                if (rawBytes.size() == (messageLength + HEADER_LENGTH)) {
                                    state = CtrConnectionState.FRAME_RECEIVED;
                                }
                        }
                    }
                }
            } while (state != CtrConnectionState.FRAME_RECEIVED);
            delayAndFlushConnection(50);
        } catch (CTRTimeoutException e) {
            throw e;
        } catch (IOException e) {
            throw new CTRConnectionException("An error occurred while reading the SMS response frame.", e);
        }
        return rawBytes.toByteArray();
    }

    /**
     * @return
     * @throws java.io.IOException
     */
    private boolean bytesFromDeviceAvailable() throws IOException {
        ensureComChannelIsInReadingMode();
        return comChannel.available() > 0;
    }

    /**
     * Sleep for a number of millis, and remove al the pending bytes from
     * the input buffer. If there ae still bytes on the stream, repeat this,
     * until the buffer is empty.
     */
    private void delayAndFlushConnection(int delay) {
        ProtocolTools.delay(delay);
        try {
            while (bytesFromDeviceAvailable()) {
                ProtocolTools.delay(delay);
                ensureComChannelIsInReadingMode();
                comChannel.read(new byte[1024]);
            }
        } catch (IOException e) {
            //Absorb
        }
    }

    public CTREncryption getCTREncryption() {
        return ctrEncryption;
    }

    private void ensureComChannelIsInReadingMode() {
        comChannel.startReading();
    }

    private void ensureComChannelIsInWritingMode() {
        comChannel.startWriting();
    }
}