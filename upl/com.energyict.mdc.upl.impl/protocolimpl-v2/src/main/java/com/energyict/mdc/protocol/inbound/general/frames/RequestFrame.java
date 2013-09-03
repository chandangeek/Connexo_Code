package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.protocolimplv2.identifiers.SerialNumberPlaceHolder;

/**
 * Copyrights EnergyICT
 * Date: 25/06/12
 * Time: 11:08
 * Author: khe
 */
public class RequestFrame extends AbstractInboundFrame {

    @Override
    protected FrameType getType() {
        return FrameType.REQUEST;
    }

    public RequestFrame(String frame, SerialNumberPlaceHolder serialNumberPlaceHolder) {
        super(frame, serialNumberPlaceHolder);
    }

    @Override
    public void doParse() {
        //Nothing else to parse, only parameters were sent
    }
}