package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.abnt.common.exception.*;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Crc;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;
import com.energyict.protocolimplv2.abnt.common.frame.field.MeterSerialNumber;

import java.io.ByteArrayOutputStream;

/**
 * @author sva
 * @since 23/05/2014 - 15:08
 */
public class SerialConnection implements Connection {

    private static final Function NACK_FUNCTION = new Function(Function.FunctionCode.NACK);
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

    protected static ComServerExecutionException createUnexpectedResponseException(AbntException e) {
        return MdcManager.getComServerExceptionFactory().createUnexpectedResponse(e);
    }

    protected static ComServerExecutionException createNumberOfRetriesReachedException(int attempts, AbntException e) {
        return MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, attempts);
    }

    @Override
    public void sendFrame(RequestFrame request) throws ParsingException {
        // 1. Generate CRC
        request.generateAndSetCRC();

        // 2. send out frame
        doSendFrame(request);
    }

    @Override
    public ResponseFrame sendFrameGetResponse(RequestFrame request) throws ParsingException {
        return this.sendFrameGetResponse(request, 1);

    }

    @Override
    public ResponseFrame sendFrameGetResponse(RequestFrame request, int expectedNumberOfSegments) throws ParsingException {
        int numberOfSegments = 1;
        ResponseFrame response = doSendFrameGetResponse(request);

        // Check if response is segmented over multiple frames
        // and if so, then load in all segments
        if (responseIsSegmented(response)) {
            byte[] dataBytesFullResponse = response.getData().getBytes();
            ResponseFrame responseSegment = response;
            while (!isLastSegment(responseSegment)) {
                if (numberOfSegments >= expectedNumberOfSegments) {
                    throw createUnexpectedResponseException(new ConnectionException("Invalid segmentation of response, expected to receive the last segment."));
                }
                responseSegment = doSendFrameGetResponse(request, false);
                dataBytesFullResponse = ProtocolTools.concatByteArrays(dataBytesFullResponse, responseSegment.getData().getBytes());
                numberOfSegments++;
            }

            Data fullResponseData = new Data(dataBytesFullResponse.length, getProperties().getTimeZone());
            fullResponseData.parse(dataBytesFullResponse, 0);
            response.setData(fullResponseData);
        }
        return response;
    }

    protected ResponseFrame doSendFrameGetResponse(RequestFrame request) throws ParsingException {
        return this.doSendFrameGetResponse(request, true);
    }

    protected ResponseFrame doSendFrameGetResponse(RequestFrame request, boolean shouldSendOutRequest) throws ParsingException {
        int attempts = 0;
        do {
            try {
                // 1. Generate CRC
                request.generateAndSetCRC();

                // 2. send out frame (or the retry NACK command) and read in raw data
                ResponseFrame response = doSendBytesGetRawResponse(request.getBytes(), (attempts > 0) || shouldSendOutRequest);

                // 3. Check if the response is not a special command, else handle appropriate
                if (response.getFunction().getFunctionCode().equals(Function.FunctionCode.NACK)) {
                    // In case of a NACK, the reader must repeat the last frame
                    throw new ConnectionException("Received NACK from the device");
                }
                if (response.getFunction().getFunctionCode().equals(Function.FunctionCode.WAIT)) {
                    // The meter is not ready to respond or must process data, therefore it requests a wait.
                    // The reader should wait the amount of a response timeout before requesting the SAME command again
                    delayAfterError(-1);
                    throw new ConnectionException("Received WAIT from the device");
                }

                // 4. Verify the CRC of the frame
                byte[] partOfFrameCrcIsCalculatedFor = ProtocolTools.getSubArray(response.getBytes(), 0, response.getLength() - Crc.LENGTH);
                int crc16 = CRCGenerator.calcCRCDirect(partOfFrameCrcIsCalculatedFor);
                if (crc16 != response.getCrc().getCrc()) {
                    throw new CrcMismatchException("Failed to verify the CRC of the response frame");
                }

                //5. Send ACK command to acknowledge successful receive of response
                doSendACK();

                return response;
            } catch (CrcMismatchException | ConnectionException e) {
                delayAndFlushConnection(-1);
                attempts++;
                if (attempts > getProperties().getRetries()) {
                    throw createNumberOfRetriesReachedException(attempts, e);
                }
            }
        } while (true);
    }

    protected void doSendFrame(RequestFrame frame) {
        int attempts = 0;
        do {
            try {
                doSendRawBytes(frame.getBytes());
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

    protected void doSendACK() {
        RequestFrame ack = new RequestFrame(properties.getTimeZone());
        ack.setFunction(Function.fromFunctionCode(Function.FunctionCode.ACK));
        doSendFrame(ack);
        doForcedDelay();
    }

    protected ResponseFrame doSendBytesGetRawResponse(byte[] bytes, boolean shouldSendOutRequest) {
        int attempts = 0;
        TimeOutInfo timeOutInfo = null;
        do {
            try {
                if (attempts > 0) {
                    if (timeOutInfo != null && timeOutInfo.equals(TimeOutInfo.NO_RESPONSE_RECEIVED)) {
                        doSendRawBytes(bytes);  // Retry with original request frame
                    } else {
                        doSendRawBytes(NACK_FUNCTION.getBytes()); // Retry with a NACK
                    }
                } else {
                    if (shouldSendOutRequest) {
                        doSendRawBytes(bytes);
                    }
                }
                return readFrame();
            } catch (AbntException e) {
                if (e instanceof TimeOutException) {
                    timeOutInfo = ((TimeOutException) e).getTimeOutInfo();
                }
                delayAndFlushConnection(-1);
                attempts++;
                if (attempts > getProperties().getRetries()) {
                    throw createNumberOfRetriesReachedException(attempts, e);
                }
            }
        } while (true);
    }

    protected void doSendRawBytes(byte[] bytes) throws ConnectionException {
        try {
            doForcedDelay();
            ensureComChannelIsInWritingMode();
            comChannel.write(bytes);
        } catch (ComServerExecutionException e) {
            if (MdcManager.getComServerExceptionFactory().isCommunicationException(e)) {
                throw new ConnectionException("Unable to send frame", e);
            } else {
                throw e;
            }
        }
    }

    protected ResponseFrame readFrame() throws AbntException {
        byte[] rawFrame = readRawFrame();
        return new ResponseFrame(getProperties().getTimeZone()).parse(rawFrame, 0);
    }

    protected byte[] readRawFrame() throws ConnectionException {
        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        ConnectionState state = ConnectionState.READ_FUNCTION_CODE;
        long timeOutMoment = System.currentTimeMillis() + getProperties().getTimeout();
        Function function;
        int i = 0;
        do {
            if (System.currentTimeMillis() > timeOutMoment) {
                String message = "Timed out while receiving data. State='" + state + "', timeout='" + getProperties().getTimeout() + "'.";
                throw new TimeOutException(message, rawBytes.size() == 0 ? TimeOutInfo.NO_RESPONSE_RECEIVED : TimeOutInfo.SHORT_RESPONSE);
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
        } while (!state.equals(ConnectionState.FRAME_RECEIVED) && !state.equals(ConnectionState.SPECIAL_COMMAND));
        return rawBytes.toByteArray();
    }

    /**
     * Sleep for 'forcedDelay' millis.<br>
     * Note: The ComChannel is unaffected during this operation.
     */
    protected void doForcedDelay() {
        if (getProperties().getForcedDelay() > 0) {
            ProtocolTools.delay(getProperties().getForcedDelay());
        }
    }

    /**
     * Sleep for 'delayAfterError' millis, and remove al the pending bytes from
     * the input buffer. If there ae still bytes on the stream, repeat this,
     * until the buffer is empty.
     */
    protected void delayAndFlushConnection(int delay) {
        delayAfterError(delay);
        while (bytesFromDeviceAvailable()) {
            delayAfterError(delay);
            ensureComChannelIsInReadingMode();
            comChannel.read(new byte[1024]);
        }
    }

    protected void delayAfterError(int delay) {
        if (delay == -1) {
            if (getProperties().getDelayAfterError() > 0) {
                ProtocolTools.delay(getProperties().getDelayAfterError());
            }
        } else {
            ProtocolTools.delay(delay);
        }
    }

    protected boolean bytesFromDeviceAvailable() {
        ensureComChannelIsInReadingMode();
        return comChannel.available() > 0;
    }

    protected ComChannel getComChannel() {
        return comChannel;
    }

    protected void ensureComChannelIsInReadingMode() {
        comChannel.startReading();
    }

    protected void ensureComChannelIsInWritingMode() {
        comChannel.startWriting();
    }

    protected AbntProperties getProperties() {
        return properties;
    }

    protected boolean isLastSegment(ResponseFrame response) {
        return response.getBlockCount().isLastBlock();
    }

    protected boolean responseIsSegmented(ResponseFrame response) {
        return Function.allowsSegmentation(response.getFunction());
    }
}