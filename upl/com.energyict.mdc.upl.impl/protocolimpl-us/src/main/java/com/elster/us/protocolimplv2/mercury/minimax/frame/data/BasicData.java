package com.elster.us.protocolimplv2.mercury.minimax.frame.data;

import com.elster.us.protocolimplv2.mercury.minimax.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.*;

/**
 * This is the simplest form of data passed to/from the device
 * It consists of only a two byte command code (and ETX)
 *
 * @author James Fox
 */
public class BasicData extends Data {

    private byte[] command;
    private byte[] data;

    protected void setData(byte[] data) {
        this.data = data;
    }

    private byte[] getData() {
        return data;
    }

    /**
     * Construct a new BasicData
     * @param command the command to be sent to the device
     */
    public BasicData(byte[] command) throws IOException {
        this.command = command;
        setData(command);
    }

    /**
     * Returns the two byte command code from the data
     * @return
     */
    public byte[] getCommand() {
        return command;
    }

    /**
     * Gets the byte array representation of this data, with or without ETX appended
     * @param includeEtx if true, ETX is appended, if false then it is not appended
     * @return a byte representation of the data
     * @throws IOException
     */
    @Override
    public byte[] toByteArray(boolean includeEtx) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(getData());
        if (includeEtx) {
            bos.write(CONTROL_ETX);
        }
        return bos.toByteArray();
    }
}
