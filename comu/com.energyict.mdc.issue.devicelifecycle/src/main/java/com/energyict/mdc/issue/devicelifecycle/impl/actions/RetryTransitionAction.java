/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl.actions;


import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.impl.TranslationKeys;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RetryTransitionAction extends AbstractIssueAction {

    private static final String NAME = "RetryTransitionAction";

    private IssueService issueService;
    private DeviceLifeCycleService deviceLifeCycleService;
    private DeviceService deviceService;
    private Clock clock;

    @Inject
    public RetryTransitionAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, DeviceLifeCycleService deviceLifeCycleService, DeviceService deviceService, Clock clock) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.deviceService = deviceService;
        this.clock = clock;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        if (isApplicable(issue)) {
            issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
            issue.update();
            if (retry(issue)) {
                result.success(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_LIFECYCLE_TRANSITION_SUCCESS).format());
            } else {
                result.fail(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_LIFECYCLE_TRANSITION_FAIL).format());
            }
        }
        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        if (issue != null && !issue.getStatus().isHistorical() && issue instanceof IssueDeviceLifecycle) {
            IssueDeviceLifecycle dvIssue = (IssueDeviceLifecycle) issue;
            if (!dvIssue.getStatus().isHistorical()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_ESTIMATION).format();
    }

    private boolean retry(Issue issue) {
        IssueDeviceLifecycle dliIssue = (IssueDeviceLifecycle) issue;
        Device device = deviceService.findDeviceById(Long.parseLong(dliIssue.getDevice()
                .getAmrId())).orElse(null);
        dliIssue.getFailedTransitions().forEach(transition -> {
            List<AuthorizedAction> actions = deviceLifeCycleService.getExecutableActions(device)
                    .stream().map(ExecutableAction::getAction).collect(Collectors.toList());
            List<AuthorizedAction> list = transition.getLifecycle().getAuthorizedActions(dliIssue.getDevice().getState(Instant.now(clock)).get());
            Set<AuthorizedAction> finalSet = new HashSet<AuthorizedAction>() {{
                addAll(actions);
                addAll(list);
            }}.stream().filter(action -> action instanceof AuthorizedTransitionAction).collect(Collectors.toSet());

            List<ExecutableActionProperty> props = new ArrayList<>();
            finalSet.stream()
                    .forEach(action -> DecoratedStream.decorate(((AuthorizedTransitionAction) action).getActions().stream())
                            .flatMap(microAction -> this.deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                            .distinct(PropertySpec::getName)
                            .map(ps -> this.toExecutableActionProperty(ps, Instant.now(clock))).forEach(props::add));
            finalSet.stream().map(action -> ((AuthorizedTransitionAction) action)).forEach(action -> this.deviceLifeCycleService.execute(action, device, Instant.now(clock), props));
        });
        return (dliIssue.getFailedTransitions().size() == 0);
    }


    private ExecutableActionProperty toExecutableActionProperty(PropertySpec propertySpec, Instant effectiveTimestamp) {
        try {
            if (DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key().equals(propertySpec.getName())) {
                return this.deviceLifeCycleService.toExecutableActionProperty(effectiveTimestamp, propertySpec);
            } else {
                throw new IllegalArgumentException("Unknown or unsupported PropertySpec: " + propertySpec.getName() + " that requires value type: " + propertySpec.getValueFactory().getValueType());
            }
        } catch (InvalidValueException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }
}
