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
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.devicelifecycle.FailedTransition;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.impl.TranslationKeys;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
            IssueStatus statusBeforeRetry = issue.getStatus();
            issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
            try {
                if (retry(issue)) {
                    result.success(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_LIFECYCLE_TRANSITION_SUCCESS).format());
                    ((OpenIssueDeviceLifecycle) issue).close(issueService.findStatus(IssueStatus.RESOLVED).get());
                } else {
                    result.fail(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_LIFECYCLE_TRANSITION_FAIL).format());
                    issue.setStatus(statusBeforeRetry);
                }
                issue.update();
            } catch (NotFoundException | IllegalStateException | MultipleMicroCheckViolationsException e) {
                String exceptionMessage = e.getLocalizedMessage() == null ? e.getMessage() : e.getLocalizedMessage();
                result.fail(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_LIFECYCLE_TRANSITION_FAIL).format() + ":" + exceptionMessage);
                issue.setStatus(statusBeforeRetry);
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
        FailedTransition failedTransition = getFailedTransitionToRetry(dliIssue.getFailedTransitions());
        List<AuthorizedAction> possibleActionsFromCurrentDeviceState = failedTransition.getLifecycle().getAuthorizedActions(dliIssue.getDevice().getState(Instant.now(clock))
                .orElseThrow(() -> new IllegalStateException(getThesaurus().getFormat(TranslationKeys.NO_AVAILABLE_TRANSITION_FROM_CURRENT_STATE).format())));
        List<ExecutableActionProperty> props = new ArrayList<>();
        Optional<AuthorizedTransitionAction> failedTransitionActionToRetry = possibleActionsFromCurrentDeviceState.stream()
                .filter((action -> action instanceof AuthorizedTransitionAction))
                .map(action -> ((AuthorizedTransitionAction) action))
                .filter(authorizedTransitionAction -> isFailedTransitionAction(failedTransition, authorizedTransitionAction))
                .findFirst();


        if (failedTransitionActionToRetry.isPresent()) {
            updateRequiredExecutableActionProperties(props, failedTransitionActionToRetry.get());
            return executeAction(dliIssue, props, failedTransitionActionToRetry.get());
        } else {
            throw new IllegalStateException(getThesaurus().getFormat(TranslationKeys.TRANSITION_NOT_POSSIBLE_FROM_CURRENT_DEVICE_STATE).format());
        }
    }

    private boolean isFailedTransitionAction(FailedTransition failedTransition, AuthorizedTransitionAction authorizedTransitionAction) {
        return authorizedTransitionAction.getStateTransition().getFrom().equals(failedTransition.getFrom()) && authorizedTransitionAction.getStateTransition().getTo().equals(failedTransition.getTo());
    }

    private boolean executeAction(IssueDeviceLifecycle dliIssue, List<ExecutableActionProperty> props, AuthorizedTransitionAction action) {
        Device dev = deviceService.findDeviceById(Long.parseLong(dliIssue.getDevice()
                .getAmrId())).orElse(null);
        this.deviceLifeCycleService.execute(action, dev, Instant.now(clock), props);
        return true;
    }

    /**
     * @param failedTransitions
     * @return the last failed transition
     */
    private FailedTransition getFailedTransitionToRetry(List<FailedTransition> failedTransitions) {
        return failedTransitions.stream()
                .min(Comparator.comparing(FailedTransition::getOccurrenceTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .orElseThrow(() -> new NotFoundException(getThesaurus().getFormat(TranslationKeys.UNABLE_TO_FIND_FAILED_TRANSITION_TO_RETRY).format()));

    }

    private void updateRequiredExecutableActionProperties(List<ExecutableActionProperty> props, AuthorizedTransitionAction failedTransitionActionToRetry) {
        failedTransitionActionToRetry.getActions()
                .stream()
                .flatMap(microAction -> this.deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                .map(ps -> this.toExecutableActionProperty(ps, Instant.now(clock)))
                .forEach(executableActionProperty ->
                        {
                            if(executableActionProperty != null) {
                                props.add(executableActionProperty);
                            }
                        }
                );
    }

    private ExecutableActionProperty toExecutableActionProperty(PropertySpec propertySpec, Instant effectiveTimestamp) {
        try {
            if (DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key().equals(propertySpec.getName())) {
                return this.deviceLifeCycleService.toExecutableActionProperty(effectiveTimestamp, propertySpec);
            }
            return null;
        } catch (InvalidValueException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }
}
