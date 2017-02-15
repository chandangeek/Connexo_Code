/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.inbound.general.frames;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.services.IdentificationService;

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