/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.event;

import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.impl.MessageSeeds;

import com.google.inject.Inject;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransitionFailureEvent extends DeviceLifecycleEvent {


    private Instant modTime;
    private static final String COLON_SEPARATOR = ":";
    private static final String DASH_SEPARATOR = "-";
    private static final String SEMI_COLON_SEPARATOR = ";";
    private int ruleId;


    @Inject
    public TransitionFailureEvent(Thesaurus thesaurus, DeviceService deviceService, IssueDeviceLifecycleService issueDeviceLifecycleService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, IssueService issueService, MeteringService meteringService) {
        super(thesaurus, deviceService, issueDeviceLifecycleService, deviceLifeCycleConfigurationService, issueService, meteringService);
    }

    @Override
    public void init(Map<?, ?> jsonPayload) {
        try {
            this.device = ((Number) jsonPayload.get("device")).longValue();
            this.lifecycle = ((Number) jsonPayload.get("lifecycle")).longValue();
            this.transition = ((Number) jsonPayload.get("transition")).longValue();
            this.from = ((Number) jsonPayload.get("from")).longValue();
            this.to = ((Number) jsonPayload.get("to")).longValue();
            this.cause = (String) jsonPayload.get("cause");
            this.modTime = Instant.ofEpochMilli(((Number) jsonPayload.get("modTime")).longValue());

        } catch (Exception e) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDeviceLifecycle) {
            OpenIssueDeviceLifecycle deviceLifecycleIssue = (OpenIssueDeviceLifecycle) issue;
            deviceLifecycleIssue.addFailedTransition(getDeviceLifecycle().get(), getTransition().get(),
                    getFrom().get(), getTo().get(), modTime, cause);
        }
    }

    public boolean logOnSameIssue(String check) {
        return Boolean.valueOf(true);
    }

    public boolean checkConditions(int ruleId, String deviceLifecycleTransitionProps) {
        setCreationRule(ruleId);
        return parseRawInputToList(deviceLifecycleTransitionProps, SEMI_COLON_SEPARATOR).stream().map(value -> parseRawInputToList(value, COLON_SEPARATOR))
                .anyMatch(valueSet -> valueSet.size() == 4 &&
                        this.getDevice().get().getDeviceType().getId() == Long.parseLong(valueSet.get(0)) &&
                        this.getDevice().get().getDeviceType().getDeviceLifeCycle().getId() == Long.parseLong(valueSet.get(1)) &&
                        this.getDevice().get().getDeviceType().getDeviceLifeCycle().getFiniteStateMachine().getTransitions().stream().anyMatch(t -> t.getId() == Long.parseLong(valueSet.get(2))) &&
                        parseRawInputToList(valueSet.get(3), DASH_SEPARATOR).size() == 2 &&
                        Long.parseLong(parseRawInputToList(valueSet.get(3), DASH_SEPARATOR).get(0)) == this.getDevice().get().getDeviceType().getDeviceLifeCycle().getFiniteStateMachine().getTransitions().stream()
                                .filter(t -> t.getId() == Long.parseLong(valueSet.get(2))).findFirst().get().getFrom().getId() &&
                        Long.parseLong(parseRawInputToList(valueSet.get(3), DASH_SEPARATOR).get(1)) == this.getDevice().get().getDeviceType().getDeviceLifeCycle().getFiniteStateMachine().getTransitions().stream()
                                .filter(t -> t.getId() == Long.parseLong(valueSet.get(2))).findFirst().get().getTo().getId());
    }

    private List<String> parseRawInputToList(String rawInput, String delimiter) {
        return Arrays.stream(rawInput.split(delimiter)).map(String::trim).collect(Collectors.toList());
    }



    private void setCreationRule(int ruleId){
        this.ruleId = ruleId;
    }
}
