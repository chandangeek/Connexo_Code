package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.elster.ctr.common.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;

import java.io.*;

/**
 * Copyrights EnergyICT
 * Date: 4-okt-2010
 * Time: 11:17:33
 */
public class CtrConnection {

    private OutputStream out = new ByteArrayOutputStream();
    private InputStream in = new ByteArrayInputStream(new byte[0]);
    private int retries;

    public CtrConnection(InputStream in, OutputStream out, MTU155Properties properties) {
        this.in = in;
        this.out = out;
        retries = properties.getRetries();
    }

    public GPRSFrame sendFrameGetResponse(GPRSFrame frame) {
        int attempts = 0;
        do {
            try {
                sendFrame(frame);
                return readFrame();
            } catch (CTRConnectionException e) {
                e.printStackTrace();
                attempts++;
            }
        } while (attempts <= retries);
        return null;
    }

    private GPRSFrame readFrame() throws CTRConnectionException {
        byte[] rawFrame = readRawFrame();
        return null;
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
                        break;
                    case READ_EXTENDED_LENGTH:
                        break;
                }


            } while (state != CtrConnectionState.FRAME_RECEIVED);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
