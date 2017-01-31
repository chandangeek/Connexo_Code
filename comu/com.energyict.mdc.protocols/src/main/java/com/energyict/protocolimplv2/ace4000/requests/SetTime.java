/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.requests;

import com.energyict.mdc.issues.IssueService;

import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;

import java.util.Date;

public class SetTime extends AbstractRequest<Date, Boolean> {

    public SetTime(ACE4000Outbound ace4000, IssueService issueService) {
        super(ace4000, issueService);
    }

    protected void doBefore() {
        getAce4000().setCachedMeterTimeDifference(null);   //Remove cached time
    }

    @Override
    protected void doRequest() {
        getAce4000().getObjectFactory().sendForceTime(getInput());   //The actual request
    }

    @Override
    protected void parseResult() {
        if (getAce4000().getCachedMeterTimeDifference() != null) {
            setResult(true);                             //Meter time has been received, success
        }
    }
}