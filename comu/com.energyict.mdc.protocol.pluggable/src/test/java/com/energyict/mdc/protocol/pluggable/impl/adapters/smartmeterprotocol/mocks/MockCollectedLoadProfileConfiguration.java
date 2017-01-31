/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.TestSerialNumberDeviceIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-27 (15:26)
 */
public class MockCollectedLoadProfileConfiguration implements CollectedLoadProfileConfiguration {

    private ObisCode obisCode;
    private String meterSerialNumber;
    private boolean supported;
    private ResultType resultType;
    private List<Issue> issues = new ArrayList<>();
    private List<ChannelInfo> deviceChannelInfo = Collections.emptyList();

    public MockCollectedLoadProfileConfiguration(ObisCode obisCode, String meterSerialNumber) {
        this(obisCode, meterSerialNumber, true);
    }

    public MockCollectedLoadProfileConfiguration(ObisCode obisCode, String meterSerialNumber, boolean supported) {
        super();
        this.obisCode = obisCode;
        this.meterSerialNumber = meterSerialNumber;
        this.supported = supported;
    }

    @Override
    public ObisCode getObisCode() {
        return obisCode;
    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return new TestSerialNumberDeviceIdentifier(meterSerialNumber);
    }

    @Override
    public boolean isSupportedByMeter() {
        return this.supported;
    }

    @Override
    public void setSupportedByMeter(boolean b) {
        this.supported = b;
    }

    @Override
    public List<ChannelInfo> getChannelInfos() {
        return this.deviceChannelInfo;
    }

    @Override
    public void setChannelInfos(List<ChannelInfo> channelInfos) {
        this.deviceChannelInfo = channelInfos;
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

    @Override
    public int getProfileInterval() {
        return 0;
    }

    @Override
    public int getNumberOfChannels() {
        return 0;
    }

    @Override
    public void setProfileInterval(int intervalInSeconds) {

    }
}