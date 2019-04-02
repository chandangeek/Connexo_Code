/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.devicelifecycle.FailedTransition;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.rest.impl.DeviceLifecycleIssueInfo.FailedTransitionInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService.COMPONENT_NAME;
import static com.energyict.mdc.issue.devicelifecycle.rest.impl.DeviceLifecycleIssueInfo.FailedTransitionDataInfo;
import static com.energyict.mdc.issue.devicelifecycle.rest.impl.IssueDeviceLifecycleApplication.ISSUEDEVICELIFECYCLE_REST_COMPONENT;

@Component(name = "issue.devicelifecycle.info.factory", service = {InfoFactory.class}, immediate = true)
public class DeviceLifecycleIssueInfoFactory implements InfoFactory<IssueDeviceLifecycle> {

    private DeviceService deviceService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    public DeviceLifecycleIssueInfoFactory() {
    }

    @Inject
    public DeviceLifecycleIssueInfoFactory(DeviceService deviceService, NlsService nlsService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceService = deviceService;
        setNlsService(nlsService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(ISSUEDEVICELIFECYCLE_REST_COMPONENT, Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus);
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    public DeviceLifecycleIssueInfo asInfo(IssueDeviceLifecycle issue, Class<? extends DeviceInfo> deviceInfoClass) {
        DeviceLifecycleIssueInfo issueInfo = asShortInfo(issue, deviceInfoClass);
        if (issue.getDevice() == null || !issue.getDevice().getAmrSystem().is(KnownAmrSystem.MDC)) {
            return issueInfo;
        }
        Optional<Device> device = deviceService.findDeviceById(Long.parseLong(issue.getDevice().getAmrId()));
        if (device.isPresent()) {

            List<FailedTransition> failedTransitions = new ArrayList<>(issue.getFailedTransitions());
            issueInfo.failedTransitionData.add(createFailedTransitiondataInfo(failedTransitions));
        }
        Collections.<FailedTransitionInfo>sort(issueInfo.failedTransitionData,
                Comparator.comparing(info -> info.modTime));
        return issueInfo;
    }

    private FailedTransitionDataInfo createFailedTransitiondataInfo(List<FailedTransition> failedTransitions) {
        FailedTransitionDataInfo info = new FailedTransitionDataInfo();
        info.failedTransitions = failedTransitions.stream().map(failedTransition -> {

            FailedTransitionInfo transitionInfo = new FailedTransitionInfo();
            transitionInfo.lifecycle = new IdWithNameInfo(failedTransition.getLifecycle().getId(), failedTransition.getLifecycle().getName());
            transitionInfo.transition = new IdWithNameInfo(failedTransition.getTransition().getId(), failedTransition.getTransition().getName(thesaurus));
            transitionInfo.from = new IdWithNameInfo(failedTransition.getFrom().getId(), getTransitionStateDisplayName(failedTransition.getFrom()));
            transitionInfo.to = new IdWithNameInfo(failedTransition.getTo().getId(), getTransitionStateDisplayName(failedTransition.getTo()));
            transitionInfo.cause = failedTransition.getCause();
            transitionInfo.modTime = failedTransition.getOccurrenceTime();
            return transitionInfo;
        }).sorted(Comparator.comparing(transition -> transition.modTime)).collect(Collectors.toList());
        return info;
    }

    public List<DeviceLifecycleIssueInfo> asInfo(List<? extends IssueDeviceLifecycle> issues) {
        return issues.stream().map(issue -> this.asShortInfo(issue, DeviceShortInfo.class)).collect(Collectors.toList());
    }

    private DeviceLifecycleIssueInfo asShortInfo(IssueDeviceLifecycle issue, Class<? extends DeviceInfo> deviceInfoClass) {
        return new DeviceLifecycleIssueInfo<>(issue, deviceInfoClass);
    }

    private String getTransitionStateDisplayName(State state) {
        return DefaultState.from(state).map(deviceLifeCycleConfigurationService::getDisplayName).orElseGet(state::getName);
    }

    @Override
    public Object from(IssueDeviceLifecycle issueDeviceLifecycle) {
        return asInfo(issueDeviceLifecycle, DeviceInfo.class);
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        return new ArrayList<>();
    }

    @Override
    public Class<IssueDeviceLifecycle> getDomainClass() {
        return IssueDeviceLifecycle.class;
    }
}
