package com.energyict.mdc.protocol.pluggable.impl.adapters.common.mocks;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.tasks.Issue;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-27 (13:47)
 */
public class MockCollectedRegister implements CollectedRegister {
    private final String readingTypeMRID;
    private Instant fromTime;
    private Instant toTime;
    private Instant eventTime;
    private Instant readTime;
    private String text;
    private RegisterIdentifier registerIdentifier;
    private Quantity collectedQuantity;
    private ResultType resultType;
    private List<Issue> issues = new ArrayList<>();

    public MockCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID) {
        super();
        this.readingTypeMRID = readingTypeMRID;
        this.setRegisterIdentifier(registerIdentifier);
    }

    @Override
    public RegisterIdentifier getRegisterIdentifier() {
        return registerIdentifier;
    }

    @Override
    public String getReadingTypeMRID() {
        return this.readingTypeMRID;
    }

    @Override
    public boolean isTextRegister() {
        return false;
    }

    public void setRegisterIdentifier(RegisterIdentifier registerIdentifier) {
        this.registerIdentifier = registerIdentifier;
    }

    @Override
    public Instant getFromTime() {
        return fromTime;
    }

    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }

    @Override
    public Instant getToTime() {
        return toTime;
    }

    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }

    @Override
    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public Instant getReadTime() {
        return readTime;
    }

    @Override
    public void setReadTime(Instant readTime) {
        this.readTime = readTime;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Quantity getCollectedQuantity() {
        return collectedQuantity;
    }

    public void setCollectedQuantity(Quantity collectedQuantity) {
        this.collectedQuantity = collectedQuantity;
    }

    @Override
    public void setCollectedTimeStamps(Instant readTime, Instant fromTime, Instant toTime) {
        this.setReadTime(readTime);
        this.setFromTime(fromTime);
        this.setToTime(toTime);
    }

    @Override
    public void setCollectedData(String text) {
        this.setText(text);
    }

    @Override
    public void setCollectedData(Quantity collectedQuantity, String text) {
        this.setCollectedQuantity(collectedQuantity);
        this.setText(text);
    }

    @Override
    public void setCollectedData(Quantity collectedQuantity) {
        this.setCollectedQuantity(collectedQuantity);
    }

    @Override
    public void setCollectedTimeStamps(Instant readTime, Instant fromTime, Instant toTime, Instant eventTime) {
        this.setReadTime(readTime);
        this.setEventTime(eventTime);
        this.setFromTime(fromTime);
        this.setToTime(toTime);
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