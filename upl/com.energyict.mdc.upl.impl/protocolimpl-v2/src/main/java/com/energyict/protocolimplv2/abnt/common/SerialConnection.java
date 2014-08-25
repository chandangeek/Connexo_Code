package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.abnt.common.exception.AbntException;
import com.energyict.protocolimplv2.abnt.common.exception.ConnectionException;
import com.energyict.protocolimplv2.abnt.common.exception.CrcMismatchException;
import com.energyict.protocolimplv2.abnt.common.exception.TimeoutException;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Crc;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;
import com.energyict.protocolimplv2.abnt.common.frame.field.MeterSerialNumber;

import java.io.ByteArrayOutputStream;

/**
 * @author sva
 * @since 23/05/2014 - 15:08
 */
public class SerialConnection implements Connection {

    /**
     * The ComChannel used for communication with the device *
     */
    private final ComChannel comChannel;

    /**
     * The Properties object containing all properties of the device
     */
    private AbntProperties properties;

    public SerialConnection(ComChannel comChannel, AbntProperties properties) {
        this.comChannel = comChannel;
        this.properties = properties;
    }

    @Override
    public void sendFrame(RequestFrame request) throws AbntException {
        // 1. Generate CRC
        request.generateAndSetCRC();

        // 2. send out frame
        doSendFrame(request);
    }

    @Override
    public ResponseFrame sendFrameGetResponse(RequestFrame request) throws AbntException {
        int attempts = 0;
        boolean retrying = false;
        do {
            try {
                // 1. Generate CRC
                request.generateAndSetCRC();

                // 2. send out frame (or the retry NACK command) and read in raw data
                ResponseFrame response = retrying
                        ? doSendNACKGetRawResponse()
                        : doSendFrameGetRawResponse(request);

                // 3. Check if the response is not a special command, else handle appropriate
                if (response.getFunction().getFunctionCode().equals(Function.FunctionCode.NACK)) {
                    // In case of a NACK, the reader must repeat the last frame
                    //TODO!
                }
                if (response.getFunction().getFunctionCode().equals(Function.FunctionCode.WAIT)) {
                    // The meter is not ready to respond or must process data, therefore it requests a wait.
                    // The reader should wait the amount of a response timeout before requesting the SAME command again
                    //TODO!
                }

                // 4. Verify the CRC of the frame
                byte[] partOfFrameCrcIsCalculatedFor = ProtocolTools.getSubArray(response.getBytes(), 0, response.getLength() - Crc.LENGTH);
                int crc16 = CRCGenerator.calcCRCDirect(partOfFrameCrcIsCalculatedFor);
                if (crc16 != response.getCrc().getCrc()) {
                    throw new CrcMismatchException("Failed to verify the CRC of the response frame");
                }

                //5. Send ACK command to acknowledge successful receive of response
                doSendACK();

                // 6. Do parsing of the response frame
                response.doParseData();

                return response;
            } catch (CrcMismatchException e) {
                retrying = true;
                delayAndFlushConnection(-1);
                attempts++;
                if (attempts > getProperties().getRetries()) {
                    throw createNumberOfRetriesReachedException(attempts, e);
                }
            }
        } while (true);
    }

    private void doSendFrame(RequestFrame frame) {
        doSendBytes(frame.getBytes());
    }

    private void doSendACK() {
        doSendBytes(new byte[]{(byte) Function.FunctionCode.ACK.getFunctionCode()});
    }

    private void doSendNACK() {
        doSendBytes(new byte[]{(byte) Function.FunctionCode.NACK.getFunctionCode()});
    }

    private void doSendBytes(byte[] bytes) {
        int attempts = 0;
        do {
            try {
                doSendRawBytes(bytes);
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
        return doSendBytesGetRawResponse(frame.getBytes());
    }

    private ResponseFrame doSendNACKGetRawResponse() {
        return doSendBytesGetRawResponse(new byte[]{(byte) Function.FunctionCode.NACK.getFunctionCode()});
    }

    private ResponseFrame doSendBytesGetRawResponse(byte[] bytes) {
        int attempts = 0;
        do {
            try {
                doSendRawBytes(bytes);
                return readFrame();
            } catch (AbntException e) {
                delayAndFlushConnection(-1);
                attempts++;
                if (attempts > getProperties().getRetries()) {
                    throw createNumberOfRetriesReachedException(attempts, e);
                }
            }
        } while (true);
    }

    private void doSendRawBytes(byte[] bytes) throws ConnectionException {
        try {
            doForcedDelay();
            ensureComChannelIsInWritingMode();
            comChannel.write(bytes);
        } catch (CommunicationException e) {
            throw new ConnectionException("Unable to send frame", e);
        }
    }

    private ResponseFrame readFrame() throws AbntException {
        byte[] rawFrame = readRawFrame();
        return new ResponseFrame(getProperties().getTimeZone()).parse(rawFrame, 0);
    }

    private byte[] readRawFrame() throws ConnectionException {
        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        ConnectionState state = ConnectionState.READ_FUNCTION_CODE;
        long timeOutMoment = System.currentTimeMillis() + getProperties().getTimeout();
        Function function;
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
                    case READ_FUNCTION_CODE:
                        function = Function.fromCode(readByte);
                        state = Function.isRegularFunction(function)
                                ? ConnectionState.READ_METER_SERIAL
                                : ConnectionState.SPECIAL_COMMAND;  // Not a regular 258 byte frame, but a 1 byte special command frame
                        i = 0;
                        break;
                    case READ_METER_SERIAL:
                        if (i == MeterSerialNumber.LENGTH) {
                            state = ConnectionState.READ_FRAME_DATA;
                            i = 0;
                        }
                        break;
                    case READ_FRAME_DATA:
                        if (i == ResponseFrame.RESPONSE_DATA_LENGTH) {
                            state = ConnectionState.READ_CRC;
                            i = 0;
                        }
                        break;
                    case READ_CRC:
                        if (i == Crc.LENGTH) {
                            state = ConnectionState.FRAME_RECEIVED;
                            i = 0;
                        }
                        break;
                    default:
                        break;
                }
            }
        } while (state != ConnectionState.FRAME_RECEIVED || state != ConnectionState.SPECIAL_COMMAND);
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

    private static ComServerExecutionException createNumberOfRetriesReachedException(int attempts, AbntException e) {
        return MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, attempts);
    }

    public ComChannel getComChannel() {
        return comChannel;
    }

    public AbntProperties getProperties() {
        return properties;
    }
}