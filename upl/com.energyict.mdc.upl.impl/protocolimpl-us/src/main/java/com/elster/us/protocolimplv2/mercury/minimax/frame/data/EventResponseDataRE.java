package com.elster.us.protocolimplv2.mercury.minimax.frame.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.getBytes;

/**
 * Represents the data returned from the device in a response frame as a
 * result of sending an "RE" command to read multiple event records
 *
 * @author James Fox
 */
public class EventResponseDataRE extends BasicResponseData {

    /**
     * Construct a new EventResponseDataRE
     *
     * @param bytes the data bytes received in the frame from the device
     * @throws IOException
     */
    public EventResponseDataRE(byte[] bytes) throws IOException {
        super(getBytes(RESPONSE_OK));
        parseBytes(bytes);
        setData(bytes);
    }

    private void parseBytes(byte[] bytes) {
        throw new UnsupportedOperationException("parseBytes not implemented for " + getClass().getName());
    }
}
