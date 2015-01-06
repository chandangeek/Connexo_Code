package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-27 (16:51)
 */
public class MockCollectedMessage implements CollectedMessage {

    private MessageIdentifier messageIdentifier;
    private DeviceMessageStatus deviceMessageStatus;
    private String deviceProtocolInformation;
    private ResultType resultType = ResultType.Supported;
    private List<Issue> issues = new ArrayList<>();

    public MockCollectedMessage(MessageIdentifier messageIdentifier) {
        super();
        this.messageIdentifier = messageIdentifier;
    }

    @Override
    public MessageIdentifier getMessageIdentifier() {
        return messageIdentifier;
    }

    @Override
    public DeviceMessageStatus getNewDeviceMessageStatus() {
        return deviceMessageStatus;
    }

    @Override
    public void setNewDeviceMessageStatus(DeviceMessageStatus deviceMessageStatus) {
        this.deviceMessageStatus = deviceMessageStatus;
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
    public String getDeviceProtocolInformation() {
        return deviceProtocolInformation;
    }

    @Override
    public void setDeviceProtocolInformation(String deviceProtocolInformation) {
        this.deviceProtocolInformation = deviceProtocolInformation;
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {

    }

}