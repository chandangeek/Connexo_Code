package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-27 (16:13)
 */
public class MockCollectedMessageList implements CollectedMessageList {

    private ResultType resultType = ResultType.Supported;
    private List<Issue> issues = new ArrayList<>();
    private List<CollectedMessage> collectedMessages = new ArrayList<>();

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
    public void addCollectedMessages(CollectedMessage collectedMessage) {
        this.collectedMessages.add(collectedMessage);
    }

    @Override
    public List<CollectedMessage> getCollectedMessages() {
        return this.collectedMessages;
    }

    @Override
    public List<CollectedMessage> getCollectedMessages(MessageIdentifier messageIdentifier) {
        return this.getCollectedMessages().stream().filter(x -> x.getMessageIdentifier().equals(messageIdentifier)).collect(Collectors.toList());

    }
}