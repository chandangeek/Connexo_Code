package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.MeterProtocolEvent;

import javax.xml.bind.annotation.XmlElement;
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
    public void setCollectedMeterEvents(List<MeterProtocolEvent> collectedMeterEvents) {
        this.collectedMeterEvents = collectedMeterEvents;
    }

    @Override
    public void addCollectedMeterEvents(List<MeterProtocolEvent> meterEvents) {
        if (collectedMeterEvents == null) {
            collectedMeterEvents = new ArrayList<>();
        }
        this.collectedMeterEvents.addAll(meterEvents);
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
    public void setFailureInformation(ResultType resultType, List<Issue> issues) {
        this.setResultType(resultType);
        this.issues.addAll(issues);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return false;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getSimpleName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}