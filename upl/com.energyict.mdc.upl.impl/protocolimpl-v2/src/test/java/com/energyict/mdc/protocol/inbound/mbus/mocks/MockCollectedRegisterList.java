/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.mocks;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MockCollectedRegisterList implements CollectedRegisterList {
    private final List<CollectedRegister> collectedRegisters = new ArrayList<>();

    @Override
    public void addCollectedRegister(CollectedRegister collectedRegister) {
        this.collectedRegisters.add(collectedRegister);
    }

    @Override
    public List<CollectedRegister> getCollectedRegisters() {
        return this.collectedRegisters;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return null;
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
