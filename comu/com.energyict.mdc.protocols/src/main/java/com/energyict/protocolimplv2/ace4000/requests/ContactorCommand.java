/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;

import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import com.energyict.protocolimplv2.ace4000.requests.tracking.RequestType;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ContactorCommand extends AbstractRequest<MessageEntry, MessageResult> {

    private int trackingId = -1;
    private int command;    //0 = connect, 1 = disconnect

    public ContactorCommand(ACE4000Outbound ace4000, IssueService issueService) {
        super(ace4000, issueService);
    }

    protected void doBefore() {
    }

    @Override
    protected void doRequest() {
        String[] parts = getInput().getContent().split("=");
        Date date = null;

        if (parts.length > 1) {
            String timeStamp = "";
            try {
                timeStamp = parts[1].substring(1, 20);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                date = formatter.parse(timeStamp);
            } catch (Exception e) {
                setResult(MessageResult.createFailed(getInput(), "Contactor control message failed, invalid argument: " + timeStamp));
                return;
            }
        }
        trackingId = getAce4000().getObjectFactory().sendContactorCommand(date, command);
    }

    @Override
    protected void parseResult() {
        if (getResult() != null) {
            return;           //Don't set the result again if it failed already (due to invalid arguments in the message)
        }

        if (isFailedRequest(RequestType.Contactor, trackingId)) {
            setResult(MessageResult.createFailed(getInput(), "Contactor command failed, meter returned NACK." + getReasonDescription()));
        } else {
            //If no nak has been received, assume that the command was successful. The meter does not send confirmation.
            setResult(MessageResult.createSuccess(getInput()));
        }
    }

    public void setCommand(int command) {
        this.command = command;
    }
}