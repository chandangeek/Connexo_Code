/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.event;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.devicelifecycle.DeviceLifecycleIssueFilter;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.impl.MessageSeeds;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public abstract class DeviceLifecycleEvent implements IssueEvent {


    protected Long device;
    protected Long lifecycle;
    protected Long transition;
    protected Long from;
    protected Long to;
    protected String cause;


    private final Thesaurus thesaurus;
    private final DeviceService deviceService;
    private final IssueDeviceLifecycleService issueDeviceLifecycleService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final IssueService issueService;
    private final MeteringService meteringService;

    @Inject
    public DeviceLifecycleEvent(Thesaurus thesaurus, DeviceService deviceService, IssueDeviceLifecycleService issueDeviceLifecycleService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, IssueService issueService, MeteringService meteringService) {
        this.thesaurus = thesaurus;
        this.deviceService = deviceService;
        this.issueDeviceLifecycleService = issueDeviceLifecycleService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.issueService = issueService;
        this.meteringService = meteringService;
    }

    abstract void init(Map<?, ?> jsonPayload);

    @Override
    public String getEventType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<EndDevice> getEndDevice() {
        if (getDevice() == null || !getDevice().isPresent()) { //for unknown inbound device
            return Optional.empty();
        }
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(device));
            if (meterRef.isPresent()) {
                return Optional.of(meterRef.get());
            }
        }
        throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_KORE_DEVICE, device);
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        DeviceLifecycleIssueFilter filter = new DeviceLifecycleIssueFilter();
        getEndDevice().ifPresent(filter::setDevice);
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
        Optional<? extends IssueDeviceLifecycle> foundIssue = issueDeviceLifecycleService.findAllDeviceLifecycleIssues(filter)
                .find()
                .stream()
                .findFirst();//It is going to be only zero or one open issue per device
        return foundIssue.map(issueDeviceLifecycle -> (OpenIssue) issueDeviceLifecycle);
    }

    protected Optional<Device> getDevice() {
        return deviceService.findDeviceById(device);
    }


    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected Optional<DeviceLifeCycle> getDeviceLifecycle() {
        return deviceLifeCycleConfigurationService.findDeviceLifeCycle(lifecycle);
    }

    protected Optional<StateTransition> getTransition() {
        if (getDeviceLifecycle().isPresent()) {
            return getDeviceLifecycle().get()
                    .getFiniteStateMachine()
                    .getTransitions()
                    .stream()
                    .filter(t -> t.getId() == transition)
                    .findFirst();
        } else {

            return Optional.empty();
        }
    }

    protected Optional<State> getFrom() {

        if (getTransition().isPresent()) {
            StateTransition stateTransition = getTransition().get();
            return stateTransition.getFrom()
                    .getId() == from ? Optional.of(stateTransition.getFrom()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    protected Optional<State> getTo() {
        if (getTransition().isPresent()) {
            StateTransition stateTransition = getTransition().get();
            return stateTransition.getTo().getId() == to ? Optional.of(stateTransition.getTo()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    public boolean isResolveEvent() {
        return false;
    }
}


