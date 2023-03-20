/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.mocks;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.ArrayList;
import java.util.List;

public class MockCollectedLogBook implements CollectedLogBook {
    private List<MeterProtocolEvent> meterEvents;

    @Override
    public List<MeterProtocolEvent> getCollectedMeterEvents() {
        return this.meterEvents;
    }

    @Override
    public boolean isAwareOfPushedEvents() {
        return false;
    }

    @Override
    public LogBookIdentifier getLogBookIdentifier() {
        return null;
    }

    @Override
    public void setCollectedMeterEvents(List<MeterProtocolEvent> meterEvents) {
        this.meterEvents = meterEvents;
    }

    @Override
    public void addCollectedMeterEvents(List<MeterProtocolEvent> meterEvents) {
        if (this.meterEvents == null){
            this.meterEvents = new ArrayList<>();
        }
        this.meterEvents.addAll(meterEvents);
    }

    @Override
    public ResultType getResultType() {
        return null;
    }

    @Override
    public List<Issue> getIssues() {
        return null;
    }

    @Override
    public void setFailureInformation(ResultType resultType, Issue issue) {

    }

    @Override
    public void setFailureInformation(ResultType resultType, List<Issue> issues) {

    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
        return false;
    }
}
