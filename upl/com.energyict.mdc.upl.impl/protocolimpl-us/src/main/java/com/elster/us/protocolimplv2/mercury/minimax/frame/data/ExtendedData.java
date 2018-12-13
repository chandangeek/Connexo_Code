package com.elster.us.protocolimplv2.mercury.minimax.frame.data;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Represents data passed to/from the device
 * It consists of only a two byte command code, optional
 * command parameters and (and ETX)
 *
 * @author James Fox
 */
public class ExtendedData extends BasicData {
    private byte[] commandParams;

    /**
     * Construct a new ExtendedData instance
     * Do not pass in ETX, this will be added automatically
     * @param commandCode The two-byte command code
     * @param commandParams Optional command params
     * @throws IOException
     */
    public ExtendedData(byte[] commandCode, byte[] commandParams) throws IOException {
        super(commandCode);
        this.commandParams = commandParams;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(commandCode);
        if (commandParams != null && commandParams.length > 0) {
            bos.write(commandParams);
        }
        setData(bos.toByteArray());
    }

    /**
     * Gets the command params associated with this data
     * @return a byte array of command params, or null if the command params are not set
     */
    public byte[] getCommandParams() {
        return commandParams;
    }
}
