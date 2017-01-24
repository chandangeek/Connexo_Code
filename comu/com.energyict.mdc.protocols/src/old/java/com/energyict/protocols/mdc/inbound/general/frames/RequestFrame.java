package com.energyict.protocols.mdc.inbound.general.frames;

import com.energyict.mdc.issues.IssueService;

import com.energyict.mdc.protocol.api.services.IdentificationService;

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

    public RequestFrame(String frame, IssueService issueService, IdentificationService identificationService) {
        super(frame, issueService, identificationService);
    }

    @Override
    public void doParse() {
        //Nothing else to parse, only parameters were sent
    }

}