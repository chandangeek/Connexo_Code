package com.elster.us.protocolimplv2.mercury.minimax.frame;

import com.elster.us.protocolimplv2.mercury.minimax.frame.data.BasicData;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Represents a frame received from or sent to the device
 *
 * @author James Fox
 */
public abstract class Frame {

    private BasicData data;

    /**
     * Constructs a new empty frame
     */
    public Frame() {}

    /**
     * Constructs a frame containing data
     * @param data The data to include in the frame
     */
    public Frame(BasicData data) {
        this.data = data;
    }

    /**
     * Get the data contained in the Frame
     * @return a {@link BasicData} instance containing the data
     */
    public BasicData getData() {
        return data;
    }

    /**
     * Sets data into the frame
     * @param data A {@link BasicData} object containing the data
     */
    public void setData(BasicData data) {
        this.data = data;
    }

    /**
     * Returns a byte array representation of the frame
     * Includes the data with ETX and SOH and EOT framing bytes
     * @return the byte representation of the frame
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(CONTROL_SOH);
        outputStream.write(data.toByteArray(true));
        outputStream.write(data.generateCRC());
        outputStream.write(CONTROL_EOT);
        return outputStream.toByteArray();
    }


}