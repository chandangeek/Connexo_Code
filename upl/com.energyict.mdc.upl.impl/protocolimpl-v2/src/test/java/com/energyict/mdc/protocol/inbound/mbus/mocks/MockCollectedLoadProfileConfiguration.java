/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.mocks;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;

import java.util.List;

public class MockCollectedLoadProfileConfiguration implements CollectedLoadProfileConfiguration {
    private final ObisCode profileObisCode;
    private final String meterSerialNumber;
    private DeviceIdentifier deviceIdentifier;
    private int profileInterval;

    public MockCollectedLoadProfileConfiguration(ObisCode profileObisCode, String meterSerialNumber) {
        this.profileObisCode = profileObisCode;
        this.meterSerialNumber = meterSerialNumber;
    }

    public MockCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier, String meterSerialNumber) {
        this.profileObisCode = profileObisCode;
        this.meterSerialNumber = meterSerialNumber;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public ObisCode getObisCode() {
        return profileObisCode;
    }

    @Override
    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    @Override
    public int getProfileInterval() {
        return profileInterval;
    }

    @Override
    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    @Override
    public int getNumberOfChannels() {
        return 0;
    }

    @Override
    public List<ChannelInfo> getChannelInfos() {
        return null;
    }

    @Override
    public void setChannelInfos(List<ChannelInfo> channelInfos) {

    }

    @Override
    public boolean isSupportedByMeter() {
        return false;
    }

    @Override
    public void setSupportedByMeter(boolean supportedByMeter) {

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
