/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MockDeviceRegister implements CollectedRegister {

    /**
     * The identifier of the referenced Register
     */
    private final RegisterIdentifier registerIdentifier;

    /**
     * The collected {@link Quantity}
     */
    private Quantity collectedQuantity;

    /**
     * Defines the optional collected reading text
     */
    private String text;

    /**
     * The timeStamp when the data was collected
     */
    private Date readTime;

    /**
     * Defines the start of the measurement period covered by this reading. Most
     * registers are since the start of measurement, in this case fromTime is null
     */
    private Date fromTime;

    /**
     * Defines the end of the measurement period covered by this reading. For
     * most registers this will be equal to the read time. For billing point
     * register, this is the time of the billing point
     */
    private Date toTime;

    /**
     * Defines the time the metered event took place. For most registers this
     * will be null. For maximum demand registers, this is the interval time the
     * maximum demand was registered
     */
    private Date eventTime;
    private List<Issue> issues;

    /**
     * Default constructor
     *
     * @param registerIdentifier the identifier of the Register
     */
    public MockDeviceRegister(RegisterIdentifier registerIdentifier) {
        super();
        this.registerIdentifier = registerIdentifier;
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
        return comTask.isConfiguredToCollectRegisterData();
    }

    /**
     * Set all collected device information, leave the text field empty
     */
    public void setCollectedData(final Quantity collectedQuantity) {
        this.collectedQuantity = collectedQuantity;
    }

    /**
     * Set all collected device information
     *
     * @param collectedQuantity the collected Quantity value
     * @param text              the optional text, collected from the device
     */
    @Override
    public void setCollectedData(final Quantity collectedQuantity, final String text) {
        this.collectedQuantity = collectedQuantity;
        this.text = text;
    }

    /**
     * Set all collected device information
     *
     * @param text the optional text, collected from the device
     */
    public void setCollectedData(final String text) {
        this.text = text;
    }

    public Quantity getCollectedQuantity() {
        return collectedQuantity;
    }

    public String getText() {
        return text;
    }

    public Date getReadTime() {
        return readTime;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public Date getEventTime() {
        return eventTime;
    }

    @Override
    public RegisterIdentifier getRegisterIdentifier() {
        return this.registerIdentifier;
    }

    @Override
    public boolean isTextRegister() {
        return false;
    }

    public void setReadTime(final Date readTime) {
        this.readTime = readTime;
    }

    protected void setFromTime(final Date fromTime) {
        this.fromTime = fromTime;
    }

    protected void setToTime(final Date toTime) {
        this.toTime = toTime;
    }

    protected void setEventTime(final Date eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * Set the collected timeStamps<br/>
     * <i>Should be used for BillingRegisters</i>
     *
     * @param readTime the {@link #readTime}
     * @param fromTime the {@link #fromTime}
     * @param toTime   the {@link #toTime}
     */
    @Override
    public void setCollectedTimeStamps(final Date readTime, final Date fromTime, final Date toTime) {
        setReadTime(readTime);
        setFromTime(fromTime);
        setToTime(toTime);
        setEventTime(null);
    }

    /**
     * Set the collected timeStamps.<br/>
     * <i>Should be used for MaximumDemand registers</i>
     *
     * @param readTime  the {@link #readTime}
     * @param fromTime  the {@link #fromTime}
     * @param toTime    the {@link #toTime}
     * @param eventTime the {@link #eventTime}
     */
    @Override
    public void setCollectedTimeStamps(final Date readTime, final Date fromTime, final Date toTime, final Date eventTime) {
        setReadTime(readTime);
        setFromTime(fromTime);
        setToTime(toTime);
        setEventTime(eventTime);
    }
}