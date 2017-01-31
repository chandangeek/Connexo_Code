/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.io.ComChannel;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.AbntException;
import com.energyict.protocolimplv2.abnt.common.exception.ConnectionException;
import com.energyict.protocolimplv2.abnt.common.exception.TimeOutException;
import com.energyict.protocolimplv2.abnt.common.exception.TimeOutInfo;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Crc;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;
import com.energyict.protocolimplv2.abnt.common.frame.field.MeterSerialNumber;

import java.io.ByteArrayOutputStream;

/**
 * The optical connection is slightly different from the serial connection.
 * <p/>
 * In serial connection, the application (ComServer) is in control of the communication.
 * In other words: ComServer may send out a request at any time, expecting the meter to respond
 * <p/>
 * In optical connection, the meter is in control of the communication.
 * In other words: ComServer may not send out requests at-hoc, but these must be synchronized on the
 * heartbeat the meter is sending out (the enquiry (enq) command).
 * All incoming requests that are not synchronized to the heartbeat will be discarded;
 * *
 *
 * @author sva
 * @since 21/08/2014 - 16:24
 */
public class OpticalConnection extends SerialConnection {

    public OpticalConnection(ComChannel comChannel, AbntProperties properties) {
        super(comChannel, properties);
    }

    protected void doSendRawBytes(byte[] bytes) throws ConnectionException {
        if (bytes.length > 1) {
            synchronizeOnHeartBeat();   // Only full frames should be synchronized on the heartBeat (thus not needed for short command frames (ACK / NACK))
        }
        super.doSendRawBytes(bytes);
    }

    private void synchronizeOnHeartBeat() {
        int attempts = 0;
        do {
            try {
                delayAndFlushConnection(0); // Flush connection to ignore all old heartbeats
                ResponseFrame heartBeatResponse = readFrame(false);
                if (Function.isHeartBeatFunction(heartBeatResponse.getFunction())) {
                    return;
                } else {
                    throw new ConnectionException("Expected to receive ENQ heartbeat, but received function 0x" +
                            Integer.toHexString(heartBeatResponse.getFunction().getFunctionCode().getFunctionCode()));
                }
            } catch (AbntException e) {
                delayAndFlushConnection(-1);
                attempts++;
                if (attempts > getProperties().getRetries()) {
                    throw createNumberOfRetriesReachedException(attempts, e);
                }
            }
        } while (true);
    }

    protected ResponseFrame readFrame(boolean ignoreHeartBeat) throws AbntException {
        byte[] rawFrame = readRawFrame(ignoreHeartBeat);
        return new ResponseFrame(getProperties().getTimeZone()).parse(rawFrame, 0);
    }

    protected byte[] readRawFrame() throws ConnectionException {
        return readRawFrame(true);
    }

    protected byte[] readRawFrame(boolean ignoreHeartBeat) throws ConnectionException {
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
                int readByte = getComChannel().read();
                readByte &= 0x0FF;
                if (state != ConnectionState.READ_FUNCTION_CODE) {
                    rawBytes.write(readByte);
                }
                switch (state) {
                    case READ_FUNCTION_CODE:
                        function = Function.fromCode(readByte);
                        if (!(ignoreHeartBeat && Function.isHeartBeatFunction(function))) {
                            rawBytes.write(readByte);
                            state = Function.isRegularFunction(function)
                                    ? ConnectionState.READ_METER_SERIAL
                                    : ConnectionState.SPECIAL_COMMAND;  // Not a regular 258 byte frame, but a 1 byte special command frame
                            i = 0;
                        }
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
}