package com.elster.us.protocolimplv2.mercury.minimax.frame.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.*;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.*;

/**
 * Represents data received in a frame from the device as a result
 * of reading a single register value
 *
 * @author James Fox
 */
public class SingleReadResponseData extends BasicResponseData {

    private String responseValue = null;

    /**
     * Create a new SingleReadResponseData instance
     * @param bytes The data bytes received from the device (without ETX)
     * @throws IOException
     */
    public SingleReadResponseData(byte[] bytes) throws IOException {
        super(getBytes(RESPONSE_OK));
        parseBytes(bytes);
        setData(bytes);
    }

    private void parseBytes(byte[] bytes) {
        responseValue = getString(bytes).substring(4,bytes.length);
    }

    /**
     * Gets the response value
     * @return The response value from the device
     */
    public String getValue() {
        return responseValue;
    }
}
