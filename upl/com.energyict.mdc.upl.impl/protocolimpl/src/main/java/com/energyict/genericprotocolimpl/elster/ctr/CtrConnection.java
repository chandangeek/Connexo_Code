package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.elster.ctr.common.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.common.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.crypto.*;
import java.io.*;
import java.security.*;

/**
 * Copyrights EnergyICT
 * Date: 4-okt-2010
 * Time: 11:17:33
 */
public class CtrConnection {

    private OutputStream out = new ByteArrayOutputStream();
    private InputStream in = new ByteArrayInputStream(new byte[0]);
    private int retries;
    private int timeOut;
    private int delayAfterError;

    public CtrConnection(InputStream in, OutputStream out, MTU155Properties properties) {
        this.in = in;
        this.out = out;
        this.retries = properties.getRetries();
        this.timeOut = properties.getTimeout(); 
        this.delayAfterError = properties.getDelayAfterError();
    }

    public GPRSFrame sendFrameGetResponse(GPRSFrame frame) throws IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, CTRParsingException {
        frame.setCrc();
        int attempts = 0;
        do {
            try {
                sendFrame(frame);
                return readFrame();
            } catch (CTRConnectionException e) {
                e.printStackTrace();
                delayAndFlushConnection();
                attempts++;
            }
        } while (attempts <= retries);
        return null;
    }

    private void delayAndFlushConnection() {
        ProtocolTools.delay(delayAfterError);
        try {
            do {
                ProtocolTools.delay(delayAfterError);
            } while (in.read(new byte[1024]) > 0);
        } catch (IOException e) {
            //Absorb
        }
    }

    private GPRSFrame readFrame() throws CTRConnectionException {
        byte[] rawFrame = readRawFrame();
        try {
            return new GPRSFrame().parse(rawFrame, 0);
        } catch (CTRParsingException e) {
            throw new CTRConnectionException("Invalid frame received!", e);
        }
    }

    private byte[] readRawFrame() throws CTRConnectionException {
        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        try {
            CtrConnectionState state = CtrConnectionState.WAIT_FOR_STX;
            do {
                int readByte = in.read() & 0x0FF;
                switch (state) {
                    case WAIT_FOR_STX:
                        if (readByte == GPRSFrame.STX) {
                            rawBytes.write(readByte);
                            state = CtrConnectionState.READ_MIN_LENGTH;
                        }
                        break;
                    case READ_MIN_LENGTH:
                        rawBytes.write(readByte);
                        if (rawBytes.size() >= GPRSFrame.LENGTH) {
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
                        break;
                    case READ_EXTENDED_LENGTH:
                        throw new CTRConnectionException("Long frames not supported yet!");
                }
            } while (state != CtrConnectionState.FRAME_RECEIVED);
        } catch (IOException e) {
            throw new CTRConnectionException("An error occured while reading the raw CtrFrame.", e);
        }
        return rawBytes.toByteArray();
    }

    private void sendFrame(GPRSFrame frame) throws CTRConnectionException {
        try {
            out.write(frame.getBytes());
            out.flush();
        } catch (IOException e) {
            throw new CTRConnectionException("Unable to send frame: " + e);
        }
    }

}
