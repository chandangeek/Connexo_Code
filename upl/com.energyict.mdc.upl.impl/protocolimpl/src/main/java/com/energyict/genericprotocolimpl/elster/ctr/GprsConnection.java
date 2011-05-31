package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.elster.ctr.encryption.CTREncryption;
import com.energyict.genericprotocolimpl.elster.ctr.exception.*;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.structure.NackStructure;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.*;

/**
 * Copyrights EnergyICT
 * Date: 4-okt-2010
 * Time: 11:17:33
 */
public class GprsConnection implements CtrConnection<GPRSFrame> {

    private final OutputStream out;
    private final InputStream in;

    private final int retries;
    private final int timeOut;
    private final int delayAfterError;
    private final int forcedDelay;
    private final boolean debug;

    /**
     * @param in
     * @param out
     * @param properties
     */
    public GprsConnection(InputStream in, OutputStream out, MTU155Properties properties) {
        this.in = in;
        this.out = out;
        this.retries = properties.getRetries();
        this.timeOut = properties.getTimeout();
        this.delayAfterError = properties.getDelayAfterError();
        this.forcedDelay = properties.getForcedDelay();
        this.debug = properties.isDebug();
    }

    /**
     * Send a GPRS frame, and wait for the response from the meter.
     * This is the place where the retry mechanism is implemented.
     * After every error, we clear the input buffer and try again
     * for the number of retries.
     *
     * @param frame
     * @return
     * @throws CTRConnectionException
     */
    public GPRSFrame sendFrameGetResponse(GPRSFrame frame) throws CTRConnectionException {
        frame.setCrc();

        int attempts = 0;
        do {
            try {
                sendFrame(frame);
                GPRSFrame gprsFrame = readFrame();
                Data data = gprsFrame.getData();
                if (data instanceof NackStructure) {
                    //throw new CTRNackException((NackStructure) data);
                }
                return gprsFrame;
            } catch (CTRNackException e) {
                throw e;
            } catch (CTRConnectionException e) {
                delayAndFlushConnection(-1);
                attempts++;
                if (attempts > retries) {
                    throw new CTRConnectionException("Number of retries reached: [" + --attempts + "/" + retries + "].", e);
                }
            }
        } while (true);
    }

    /**
     * Sleep for 'delayAfterError' millis, and remove al the pending bytes from
     * the input buffer. If there ae still bytes on the stream, repeat this,
     * until the buffer is empty.
     */
    private void delayAndFlushConnection(int delay) {
        delayAfterError(delay);
        try {
            while (bytesFromDeviceAvailable()) {
                delayAfterError(delay);
                in.read(new byte[1024]);
            }
        } catch (IOException e) {
            //Absorb
        }
    }

    private void delayAfterError(int delay) {
        if (delay == -1) {
            if (delayAfterError > 0) {
                ProtocolTools.delay(delayAfterError);
            }
        } else {
            ProtocolTools.delay(delay);
        }
    }

    /**
     * @return
     * @throws CTRConnectionException
     */
    private GPRSFrame readFrame() throws CTRConnectionException {
        byte[] rawFrame = readRawFrame();
        try {
            return new GPRSFrame().parse(rawFrame, 0);
        } catch (CTRParsingException e) {
            throw new CTRConnectionException("Invalid frame received!", e);
        }
    }

    /**
     * @return
     * @throws CTRConnectionException
     */
    private byte[] readRawFrame() throws CTRConnectionException {
        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        try {
            CtrConnectionState state = CtrConnectionState.WAIT_FOR_STX;
            long timeOutMoment = System.currentTimeMillis() + timeOut;
            do {
                if (System.currentTimeMillis() > timeOutMoment) {
                    String message = "Timed out while receiving data. State='" + state + "', timeout='" + timeOut + "'.";
                    throw new CTRTimeoutException(message);
                }
                if (!bytesFromDeviceAvailable()) {
                    ProtocolTools.delay(1);
                } else {
                    byte[] buffer = new byte[1024];
                    int len = in.read(buffer);
                    for (int ptr = 0; ptr < len; ptr++) {
                        int readByte = buffer[ptr];
                        readByte &= 0x0FF;
                        switch (state) {
                            case WAIT_FOR_STX:
                                state = waitForSTX(rawBytes, state, readByte);
                                break;
                            case READ_MIN_LENGTH:
                                state = readMinLength(rawBytes, state, readByte);
                                break;
                            case READ_EXTENDED_LENGTH:
                                state = waitExtendedLength(rawBytes, state, readByte);
                                break;
                        }
                    }
                }
            } while (state != CtrConnectionState.FRAME_RECEIVED);
            delayAndFlushConnection(50);
        } catch (CTRTimeoutException e) {
            throw e;
        } catch (IOException e) {
            throw new CTRConnectionException("An error occured while reading the raw CtrFrame.", e);
        }
        return rawBytes.toByteArray();
    }

    /**
     * @return
     * @throws IOException
     */
    private boolean bytesFromDeviceAvailable() throws IOException {
        return in.available() > 0;
    }

    /**
     * @param rawBytes
     * @param state
     * @param readByte
     * @return
     * @throws CTRConnectionException
     */
    private CtrConnectionState waitExtendedLength(ByteArrayOutputStream rawBytes, CtrConnectionState state, int readByte) throws CTRConnectionException {
        throw new CTRConnectionException("Long frames not supported yet!");
    }

    /**
     * @param rawBytes
     * @param state
     * @param readByte
     * @return
     * @throws CTRParsingException
     * @throws CTRConnectionException
     */
    private CtrConnectionState readMinLength(ByteArrayOutputStream rawBytes, CtrConnectionState state, int readByte) throws CTRParsingException, CTRConnectionException {
        rawBytes.write(readByte);
        if (rawBytes.size() >= GPRSFrame.LENGTH_SHORT) {
            GPRSFrame gprsFrame = new GPRSFrame().parse(rawBytes.toByteArray(), 0);
            if (gprsFrame.getProfi().isLongFrame()) {
                state = CtrConnectionState.READ_EXTENDED_LENGTH;
            } else {
                if (readByte != GPRSFrame.ETX) {
                    String fromBytes = ProtocolTools.getHexStringFromBytes(new byte[]{(byte) (readByte & 0x0FF)});
                    throw new CTRConnectionException("Expected ETX, but received " + fromBytes);
                } else {
                    state = CtrConnectionState.FRAME_RECEIVED;
                }
            }
        }
        return state;
    }

    /**
     * @param rawBytes
     * @param state
     * @param readByte
     * @return
     */
    private CtrConnectionState waitForSTX(ByteArrayOutputStream rawBytes, CtrConnectionState state, int readByte) {
        if (readByte == GPRSFrame.STX) {
            rawBytes.write(readByte);
            state = CtrConnectionState.READ_MIN_LENGTH;
        }
        return state;
    }

    /**
     * @param frame
     * @throws CTRConnectionException
     */
    private void sendFrame(GPRSFrame frame) throws CTRConnectionException {
        try {
            doForcedDelay();
            if (out != null) {
                out.write(frame.getBytes());
                out.flush();
            } else {
                throw new CTRConnectionException("Unable to send frame. OutputStream was null.");
            }
        } catch (IOException e) {
            throw new CTRConnectionException("Unable to send frame.", e);
        }
    }

    /**
     *
     */
    private void doForcedDelay() {
        if (forcedDelay > 0) {
            ProtocolTools.delay(forcedDelay);
        }
    }

    public CTREncryption getCTREncryption() {
        return null;
    }

}
