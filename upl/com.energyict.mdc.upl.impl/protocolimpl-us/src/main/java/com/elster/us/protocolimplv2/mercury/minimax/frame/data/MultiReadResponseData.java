package com.elster.us.protocolimplv2.mercury.minimax.frame.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.getBytes;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.getString;

/**
 * Represents data received in a frame from the device as a result
 * of reading multiple register values
 *
 * @author James Fox
 */
public class MultiReadResponseData extends BasicResponseData {

    private List<String> responses = new ArrayList<String>();

    /**
     * Create a new MultiReadResponseData instance
     * @param bytes The data bytes received from the device (without ETX)
     * @throws IOException
     */
    public MultiReadResponseData(byte[] bytes) throws IOException {
        super(getBytes(RESPONSE_OK));
        parseBytes(bytes);
        setData(bytes);
    }

    private void parseBytes(byte[] bytes) {
        String str = getString(bytes);
        StringTokenizer st = new StringTokenizer(str, ",");
        while (st.hasMoreTokens()) {
            responses.add(st.nextToken().trim());
        }
    }

    /**
     * Return the response at the specified index
     * @param index The index of the response to get
     * @return The response from the device at the specified index
     */
    public String getResponse(int index) {
        return responses.get(index);
    }
}
