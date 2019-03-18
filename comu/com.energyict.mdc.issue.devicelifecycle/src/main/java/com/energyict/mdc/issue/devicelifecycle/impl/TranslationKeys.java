/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.issue.devicelifecycle.impl.actions.CloseIssueAction;
import com.energyict.mdc.issue.devicelifecycle.impl.event.DeviceLifecycleEventHandlerFactory;


public enum TranslationKeys implements TranslationKey {

    DEVICE_LIFECYCLE_ISSUE_TYPE("DeviceLifecycleIssueType", "Device lifecycle"),
    DEVICE_LIFECYCLE_ISSUE_REASON("DeviceLifecycleIssueReason", "Device lifecycle transition failure"),
    DEVICE_LIFECYCLE_ISSUE_REASON_DESCRIPTION("DeviceLifecycleIssueReasonDescription", "Device lifecycle transition failure on transition {0} from {1} to {2}"),
    DEVICE_LIFECYCLE_ISSUE_RULE_TEMPLATE_NAME("DeviceLifecycleIssueRuleTemplateName", "Create issue lifecycle transitions fail"),
    DEVICE_LIFECYCLE_ISSUE_RULE_TEMPLATE_DESCRIPTION("DeviceLifecycleIssueRuleTemplateDescription", "Create issue lifecycle transitions fail"),
    //DEVICE_CONFIGURATIONS_PROPERTY(DeviceLifecycleIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS, "Device configurations"),
    AQ_SUBSCRIBER(DeviceLifecycleEventHandlerFactory.AQ_DEVICE_LIFECYCLE_EVENT_SUBSCRIBER, "Create device lifecycle issues"),
    ACTION_RETRY_ESTIMATION("ActionLifecycleTransition", "Retry lifecycle transition"),
    ACTION_RETRY_LIFECYCLE_TRANSITION_SUCCESS("ActionRetryLifecycleTransitionSuccess", "Lifecycle transition retry succeeded"),
    ACTION_RETRY_LIFECYCLE_TRANSITION_FAIL("ActionRetryLifecycleTransitionFailed", "LIfecycle transition retry failed"),
    CLOSE_ACTION_PROPERTY_CLOSE_STATUS(CloseIssueAction.CLOSE_STATUS, "Close status"),
    CLOSE_ACTION_PROPERTY_COMMENT(CloseIssueAction.COMMENT, "Comment"),
    CLOSE_ACTION_WRONG_STATUS("action.wrong.status", "You are trying to apply the incorrect status"),
    CLOSE_ACTION_ISSUE_CLOSED("action.issue.closed", "Issue closed"),
    CLOSE_ACTION_ISSUE_ALREADY_CLOSED("action.issue.already.closed", "Issue already closed"),
    CLOSE_ACTION_CLOSE_ISSUE("issue.action.closeIssue", "Close issue"),
    ISSUE_CREATION_SELECTION_ON_RECURRENCE("issue.creation.selection.on.recurrence", "On recurrence"),
    CREATE_NEW_DEVICELIFECYCLE_ISSUE("create.new.devicelifecycle.issue", "Create new device lifecycle issue"),
    LOG_ON_EXISTING_DEVICELIFECYCLE_ISSUE("log.on.existing.devicelifecycle.issue", "Log on existing open device lifecycle issue"),
    PARAMETER_AUTO_RESOLUTION(DeviceLifecycleIssueCreationRuleTemplate.AUTORESOLUTION, "Auto resolution"),
    DEVICE_LIFECYCLE_TRANSITION_PROPS(DeviceLifecycleIssueCreationRuleTemplate.DEVICE_LIFECYCLE_TRANSITION_PROPS, "Transitions");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public String getTranslated(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }
}
