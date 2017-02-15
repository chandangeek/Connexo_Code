/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;

import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

public class WriteConfiguration extends AbstractRequest<MessageEntry, MessageResult> {

    private int trackingId = -1;

    public WriteConfiguration(ACE4000Outbound ace4000, IssueService issueService) {
        super(ace4000, issueService);
    }

    protected void doBefore() {
    }

    @Override
    protected void doRequest() {
        if (getResult() != null) {
            return;   //Don't send if result is already known
        }

        //Retry if necessary
        if ((trackingId != -1) && !isReceivedRequest(RequestType.Config, trackingId)) {
            trackingId = getAce4000().getMessageExecutor().sendConfigurationMessage(getInput());
        }

        //The initial request
        if (trackingId == -1) {
            //Message executor parses the tags and executes the right configuration request
            Integer status = getAce4000().getMessageExecutor().sendConfigurationMessage(getInput());
            if (status == null) {
                setResult(MessageResult.createFailed(getInput(), "Unknown message tag"));
                return;
            }
            trackingId = status;
        }
    }

    @Override
    protected void parseResult() {
        if (isSuccessfulRequest(RequestType.Config, trackingId)) {
            setResult(MessageResult.createSuccess(getInput()));
        } else if (isFailedRequest(RequestType.Config, trackingId)) {
            setResult(MessageResult.createFailed(getInput(), "Config request returned NACK. " + getReasonDescription()));
        } else {
            setResult(MessageResult.createFailed(getInput(), "Meter didn't respond to config request"));
        }
    }
}