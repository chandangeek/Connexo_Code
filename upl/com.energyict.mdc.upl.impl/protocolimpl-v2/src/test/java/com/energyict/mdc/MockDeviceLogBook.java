/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.MeterProtocolEvent;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockDeviceLogBook implements CollectedLogBook {

    private List<MeterProtocolEvent> meterEvents;
    private LogBookIdentifier logBookIdentifier;
    private List<Issue> issues;

    public MockDeviceLogBook() {
        this.logBookIdentifier = null;
    }

    public MockDeviceLogBook(LogBookIdentifier logBookIdentifier) {
        this.logBookIdentifier = logBookIdentifier;
    }


    @Override
    public ResultType getResultType() {
        return ResultType.Supported;
    }

    @Override
    public List<Issue> getIssues() {
        if (issues == null) {
            issues = new ArrayList<>();
        }
        return issues;
    }

    @Override
    public void setFailureInformation(ResultType resultType, Issue issue) {
        getIssues().add(issue);
    }

    @Override
    public void setFailureInformation(ResultType resultType, List<Issue> issues) {
        getIssues().addAll(issues);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
        return comTask.isConfiguredToCollectEvents();
    }

    @Override
    @XmlAttribute
    public List<MeterProtocolEvent> getCollectedMeterEvents() {
        if (this.meterEvents == null) {
            return Collections.emptyList();
        }
        return this.meterEvents;
    }

    @Override
    @XmlAttribute
    public LogBookIdentifier getLogBookIdentifier() {
        return logBookIdentifier;
    }

    @Override
    public void setCollectedMeterEvents(List<MeterProtocolEvent> meterEvents) {
        this.meterEvents = meterEvents;
    }

    @Override
    public void addCollectedMeterEvents(List<MeterProtocolEvent> meterEvents) {
        if (this.meterEvents == null) {
            setCollectedMeterEvents(meterEvents);
        } else {
            this.meterEvents.addAll(meterEvents);
        }
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}