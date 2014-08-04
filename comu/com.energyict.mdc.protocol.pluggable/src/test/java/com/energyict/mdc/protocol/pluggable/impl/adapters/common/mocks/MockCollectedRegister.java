package com.energyict.mdc.protocol.pluggable.impl.adapters.common.mocks;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-27 (13:47)
 */
public class MockCollectedRegister implements CollectedRegister {
    private final ReadingType readingType;
    private Date fromTime;
    private Date toTime;
    private Date eventTime;
    private Date readTime;
    private String text;
    private RegisterIdentifier registerIdentifier;
    private Quantity collectedQuantity;
    private ResultType resultType;
    private List<Issue> issues = new ArrayList<>();

    public MockCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType) {
        super();
        this.readingType = readingType;
        this.setRegisterIdentifier(registerIdentifier);
    }

    @Override
    public RegisterIdentifier getRegisterIdentifier() {
        return registerIdentifier;
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType;
    }

    public void setRegisterIdentifier(RegisterIdentifier registerIdentifier) {
        this.registerIdentifier = registerIdentifier;
    }

    @Override
    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    @Override
    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    @Override
    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public Date getReadTime() {
        return readTime;
    }

    @Override
    public void setReadTime(Date readTime) {
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
    public void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime) {
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
    public void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime, Date eventTime) {
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