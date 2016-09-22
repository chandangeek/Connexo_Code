package com.elster.us.protocolimplv2.mercury.minimax.frame;

import com.elster.us.protocolimplv2.mercury.minimax.frame.data.BasicData;

/**
 * A representation of a frame sent to the device
 *
 * @author James Fox
 */
public class RequestFrame extends Frame {
    /**
     * Construct a new RequestFrame instance
     *
     * @param data a {@link BasicData} instance containing the data
     */
    public RequestFrame(BasicData data) {
        super(data);
    }
}
