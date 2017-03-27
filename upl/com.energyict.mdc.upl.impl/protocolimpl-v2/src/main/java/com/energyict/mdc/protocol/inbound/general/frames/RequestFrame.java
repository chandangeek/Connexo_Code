package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.identifiers.CallHomeIdPlaceHolder;

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

    public RequestFrame(String frame, CallHomeIdPlaceHolder callHomeIdPlaceHolder, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(frame, callHomeIdPlaceHolder, collectedDataFactory, issueFactory);
    }

    @Override
    public void doParse() {
        //Nothing else to parse, only parameters were sent
    }
}