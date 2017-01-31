/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-28 (21:39)
 */
public class MockCollectedLogBook implements CollectedLogBook {

    private final LogBookIdentifier logBookIdentifier;
    private List<MeterProtocolEvent> collectedMeterEvents;
    private ResultType resultType = ResultType.Supported;
    private List<Issue> issues = new ArrayList<>();

    public MockCollectedLogBook(LogBookIdentifier logBookIdentifier) {
        super();
        this.logBookIdentifier = logBookIdentifier;
    }

    @Override
    public LogBookIdentifier getLogBookIdentifier() {
        return this.logBookIdentifier;
    }

    @Override
    public List<MeterProtocolEvent> getCollectedMeterEvents() {
        return collectedMeterEvents;
    }

    @Override
    public void setMeterEvents(List<MeterProtocolEvent> collectedMeterEvents) {
        this.collectedMeterEvents = collectedMeterEvents;
    }

    @Override
    public ResultType getResultType() {
        return this.resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    @Override
    public List<Issue> getIssues() {
        return this.issues;
    }

    @Override
    public void setFailureInformation(ResultType resultType, Issue issue) {
        this.setResultType(resultType);
        this.issues.add(issue);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return false;
    }

}