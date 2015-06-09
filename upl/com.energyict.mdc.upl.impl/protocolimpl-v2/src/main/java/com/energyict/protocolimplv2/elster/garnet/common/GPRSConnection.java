package com.energyict.protocolimplv2.elster.garnet.common;

import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.garnet.GarnetProperties;
import com.energyict.protocolimplv2.elster.garnet.exception.*;
import com.energyict.protocolimplv2.elster.garnet.frame.RequestFrame;
import com.energyict.protocolimplv2.elster.garnet.frame.ResponseFrame;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Address;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Crc;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.NotExecutedErrorResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.field.NotExecutedError;

import java.io.ByteArrayOutputStream;

/**
 * @author sva
 * @since 23/05/2014 - 15:08
 */
public class GPRSConnection implements Connection {

    /**
     * The ComChannel used for TCP/IP communication with the device *
     */
    private final ComChannel comChannel;

    /**
     * The Properties object containing all properties of the device
     */
    private GarnetProperties properties;

    /**
     * The XTEAEncryptionHelper used for encryption/decryption
     */
    private XTEAEncryptionHelper encryptionHelper;

    public GPRSConnection(ComChannel comChannel, GarnetProperties properties) {
        this.comChannel = comChannel;
        this.properties = properties;
    }

    private static ComServerExecutionException createNumberOfRetriesReachedException(int attempts, GarnetException e) {
        return MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, attempts);
    }

    @Override
    public void sendFrame(RequestFrame frame) throws GarnetException {
        try {
            // 1. Generate CRC and encrypt the frame
            RequestFrame request = frame.doClone();   // Clone to avoid the original frame is altered (gets encrypted)
            request.generateAndSetCRC();
            request = (RequestFrame) getEncryptionHelper().encryptFrame(request);

            // 2. send out frame
            doSendFrame(request);
        } catch (CipheringException e) {
            throw createCipheringException(e);
        }
    }

    @Override
    public ResponseFrame sendFrameGetResponse(RequestFrame frame) throws GarnetException {
        int attempts = 0;
        do {
            try {
                // 1. Generate CRC and encrypt the frame
                RequestFrame request = frame.doClone();   // Clone to avoid the original frame is altered (gets encrypted)
                request.generateAndSetCRC();
                request = (RequestFrame) getEncryptionHelper().encryptFrame(request);

                // 2. send out frame and read in raw data
                ResponseFrame response = doSendFrameGetRawResponse(request);

                // 3. Decrypt the frame with correct key
                response = (ResponseFrame) getEncryptionHelper().decryptFrame(response);

                // 4. Verify the CRC of the decrypted frame
                int crc16 = CRCGenerator.calcCRC16(response.getBytes(), response.getLength() - Crc.LENGTH);
                if (crc16 != response.getCrc().getCrc()) {
                    throw new CrcMismatchException("Failed to verify the CRC of the response frame");
                }

                // 5. Do parsing of the decrypted frame
                response.doParseData();

                // 6. Check if the response is not an error, else do throw the proper ComServerExecutionException
                if (response.getData() instanceof NotExecutedErrorResponseStructure) {
                    throw new NotExecutedException(request, (NotExecutedErrorResponseStructure) response.getData());
                }
                return response;
            } catch (CrcMismatchException e) {
                delayAndFlushConnection(-1);
                attempts++;
                if (attempts > getProperties().getRetries()) {
                    throw createNumberOfRetriesReachedException(attempts, e);
                }
            } catch (NotExecutedException e) {
                if (e.getErrorStructure().getNotExecutedError().getErrorCodeId() == NotExecutedError.ErrorCode.CRC_ERROR.getErrorCode()) {
                    // Retry only in case of CRC error - for all other error codes retrying is useless
                    delayAndFlushConnection(-1);
                    attempts++;
                    if (attempts > getProperties().getRetries()) {
                        throw createNumberOfRetriesReachedException(attempts, e);
                    }
                } else if (e.getErrorStructure().getNotExecutedError().getErrorCode() == NotExecutedError.ErrorCode.MAJOR_DATA ||
                        e.getErrorStructure().getNotExecutedError().getErrorCode() == NotExecutedError.ErrorCode.MINOR_DATA) {
                    throw MdcManager.getComServerExceptionFactory().createUnexpectedResponse(e);
                } else {
                    throw e;
                }
            } catch (CipheringException e) {
                throw createCipheringException(e);
            }
        } while (true);
    }

    private void doSendFrame(RequestFrame frame) {
        int attempts = 0;
        do {
            try {
                doSendRawFrame(frame);
                return;
            } catch (ConnectionException e) {
                delayAndFlushConnection(-1);
                attempts++;
                if (attempts > getProperties().getRetries()) {
                    throw createNumberOfRetriesReachedException(attempts, e);
                }
            }
        } while (true);
    }

    private ResponseFrame doSendFrameGetRawResponse(RequestFrame frame) {
        int attempts = 0;
        do {
            try {
                doSendRawFrame(frame);
                return readFrame();
            } catch (GarnetException e) {
                delayAndFlushConnection(-1);
                attempts++;
                if (attempts > getProperties().getRetries()) {
                    throw createNumberOfRetriesReachedException(attempts, e);
                }
            }
        } while (true);
    }

    private void doSendRawFrame(RequestFrame frame) throws ConnectionException {
        try {
            doForcedDelay();
            ensureComChannelIsInWritingMode();
            comChannel.write(frame.getBytes());
        } catch (ComServerExecutionException e) {
            if (MdcManager.getComServerExceptionFactory().isCommunicationException(e)) {
                throw new ConnectionException("Unable to send frame", e);
            } else {
                throw e;
            }
        }
    }

    private ResponseFrame readFrame() throws GarnetException {
        byte[] rawFrame = readRawFrame();
        return new ResponseFrame(getProperties().getTimeZone()).parse(rawFrame, 0);
    }

    private byte[] readRawFrame() throws ConnectionException {
        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        ConnectionState state = ConnectionState.READ_DESTINATION_ID;
        long timeOutMoment = System.currentTimeMillis() + getProperties().getTimeout();
        FunctionCode functionCode = null;
        int i = 0;
        do {
            if (System.currentTimeMillis() > timeOutMoment) {
                String message = "Timed out while receiving data. State='" + state + "', timeout='" + getProperties().getTimeout() + "'.";
                throw new TimeoutException(message);
            }
            if (!bytesFromDeviceAvailable()) {
                ProtocolTools.delay(1);
            } else {
                i++;
                ensureComChannelIsInReadingMode();
                int readByte = comChannel.read();
                readByte &= 0x0FF;
                rawBytes.write(readByte);
                switch (state) {
                    case READ_DESTINATION_ID:
                        if (i == Address.LENGTH) {
                            state = ConnectionState.READ_FUNCTION_CODE;
                            i = 0;
                        }
                        break;
                    case READ_FUNCTION_CODE:
                        functionCode = FunctionCode.fromCode(readByte);
                        if (functionCode.usesShortFrameFormat()) {
                            state = ConnectionState.READ_FRAME_DATA;
                        } else {
                            state = ConnectionState.READ_SOURCE_ID;
                        }
                        i = 0;
                        break;
                    case READ_SOURCE_ID:
                        if (i == Address.LENGTH) {
                            state = ConnectionState.READ_EXTENDED_FRAME_CODE;
                            i = 0;
                        }
                        break;
                    case READ_EXTENDED_FRAME_CODE:
                        if (functionCode.usesExtendedFrameFormat()) {
                            state = ConnectionState.READ_EXTENDED_FRAME_PART;
                            i = 0;
                        } else {
                            state = ConnectionState.READ_FRAME_DATA;
                            i = 1;
                        }
                        break;
                    case READ_EXTENDED_FRAME_PART:
                        state = ConnectionState.READ_FRAME_DATA;
                        i = 0;
                        break;
                    case READ_FRAME_DATA:
                        if (i == functionCode.getDataLength()) {
                            state = ConnectionState.READ_CRC;
                            i = 0;
                        }
                        break;
                    case READ_CRC:
                        if (i == 2) {
                            state = ConnectionState.FRAME_RECEIVED;
                            i = 0;
                        }
                        break;
                    default:
                        break;
                }
            }
        } while (state != ConnectionState.FRAME_RECEIVED);
        return rawBytes.toByteArray();
    }

    /**
     * Sleep for 'forcedDelay' millis.<br>
     * Note: The ComChannel is unaffected during this operation.
     */
    private void doForcedDelay() {
        if (getProperties().getForcedDelay() > 0) {
            ProtocolTools.delay(getProperties().getForcedDelay());
        }
    }

    /**
     * Sleep for 'delayAfterError' millis, and remove al the pending bytes from
     * the input buffer. If there ae still bytes on the stream, repeat this,
     * until the buffer is empty.
     */
    private void delayAndFlushConnection(int delay) {
        delayAfterError(delay);
        while (bytesFromDeviceAvailable()) {
            delayAfterError(delay);
            ensureComChannelIsInReadingMode();
            comChannel.read(new byte[1024]);
        }
    }

    private boolean bytesFromDeviceAvailable() {
        ensureComChannelIsInReadingMode();
        return comChannel.available() > 0;
    }

    private void delayAfterError(int delay) {
        if (delay == -1) {
            if (getProperties().getDelayAfterError() > 0) {
                ProtocolTools.delay(getProperties().getDelayAfterError());
            }
        } else {
            ProtocolTools.delay(delay);
        }
    }

    private void ensureComChannelIsInReadingMode() {
        comChannel.startReading();
    }

    private void ensureComChannelIsInWritingMode() {
        comChannel.startWriting();
    }

    private ComServerExecutionException createCipheringException(CipheringException e) {
        return MdcManager.getComServerExceptionFactory().createCipheringException(e);
    }

    public ComChannel getComChannel() {
        return comChannel;
    }

    public XTEAEncryptionHelper getEncryptionHelper() {
        if (this.encryptionHelper == null) {
            this.encryptionHelper = new XTEAEncryptionHelper(getProperties());
        }
        return encryptionHelper;
    }

    public void setSessionKey(byte[] sessionKey) {
        getEncryptionHelper().setSessionKey(sessionKey);
    }

    public GarnetProperties getProperties() {
        return properties;
    }
}
