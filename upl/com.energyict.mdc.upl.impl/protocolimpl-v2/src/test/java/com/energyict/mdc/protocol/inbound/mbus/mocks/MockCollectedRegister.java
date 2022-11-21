/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.mocks;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.identifiers.RegisterIdentifierById;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.obis.ObisCode;

import java.util.Date;
import java.util.List;

public class MockCollectedRegister implements CollectedRegister {
    private String text;
    private final RegisterIdentifier registerIdentifier;
    private Quantity quantity;
    private Date readTime;

    public MockCollectedRegister(RegisterIdentifier registerIdentifier) {
        this.registerIdentifier = registerIdentifier;
    }

    @Override
    public Quantity getCollectedQuantity() {
        return this.quantity;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public Date getReadTime() {
        return this.readTime;
    }

    @Override
    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    @Override
    public Date getFromTime() {
        return null;
    }

    @Override
    public Date getToTime() {
        return null;
    }

    @Override
    public Date getEventTime() {
        return null;
    }

    @Override
    public RegisterIdentifier getRegisterIdentifier() {
        return this.registerIdentifier;
    }

    @Override
    public boolean isTextRegister() {
        return false;
    }

    @Override
    public void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime, Date eventTime) {

    }

    @Override
    public void setCollectedData(Quantity collectedQuantity) {
        this.quantity = collectedQuantity;
    }

    @Override
    public void setCollectedData(Quantity collectedQuantity, String text) {
        this.quantity = collectedQuantity;
    }

    @Override
    public void setCollectedData(String text) {
        this.text = text;
    }

    @Override
    public void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime) {

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
