/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.i18n;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.issue.datacollection.impl.IssueProcessAssociationProvider;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.actions.CloseIssueAction;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;
import com.energyict.mdc.issue.datacollection.impl.templates.EventAggregationRuleTemplate;
import com.energyict.mdc.issue.datacollection.impl.templates.MeterRegistrationRuleTemplate;

public enum TranslationKeys implements TranslationKey {
    AQ_DATA_COLLECTION_EVENT_SUBSC(ModuleConstants.AQ_DATA_COLLECTION_EVENT_SUBSC, ModuleConstants.AQ_DATA_COLLECTION_EVENT_DISPLAYNAME),
    AQ_DELAYED_ISSUE_SUBSC(ModuleConstants.AQ_DELAYED_ISSUE_SUBSC, ModuleConstants.AQ_DELAYED_ISSUE_DISPLAYNAME),
    BASIC_TEMPLATE_DATACOLLECTION_NAME("TemplateBasicDataCollectionName", "Create issue when specific event occurs"),
    BASIC_TEMPLATE_DATACOLLECTION_DESCRIPTION("TemplateBasicDataCollectionDescription", "Create issue when specific event occurs"),
    TEMPLATE_EVT_AGGREGATION_NAME("TemplateEvtAggregationName", "Events from meters of concentrator"),
    TEMPLATE_EVT_AGGREGATION_DESCRIPTION("TemplateEvtAggregationDescription", "Create an issue based on multiple events that are related"),
    TEMPLATE_UNREGISTERED_FROM_GATEWAY_NAME("TemplateUnregisteredFromGatewayName", "Meter registration issues"),
    TEMPLATE_UNREGISTERED_FROM_GATEWAY_DESCRIPTION("TemplateUnregisteredFromGatewayDescription", "Create an issue when a slave is unregistered from a gateway for some time"),

    PARAMETER_NAME_EVENT_TYPE(BasicDataCollectionRuleTemplate.EVENTTYPE, "Event"),
    PARAMETER_EXCLUDED_COM_TASKS(BasicDataCollectionRuleTemplate.EXCLUDEDCOMTASKS, "Excluded communication tasks"),
    PARAMETER_AUTO_RESOLUTION(BasicDataCollectionRuleTemplate.AUTORESOLUTION, "Auto resolution"),
    PARAMETER_RADIO_GROUP(BasicDataCollectionRuleTemplate.RADIOGROUP, "When recurring"),
    PARAMETER_DO_NOTHING("WhenRecurringDoNothing", "Do nothing"),
    PARAMETER_INCREASE_URGENCY("WhenRecurringIncreaseUrgency", "Increase urgency (+1)"),
    PARAMETER_NAME_THRESHOLD(EventAggregationRuleTemplate.THRESHOLD, "Threshold (%)"),
    PARAMETER_NAME_DELAY_IN_HOURS(MeterRegistrationRuleTemplate.DELAY, "Delay (in hours)"),
    PARAMETER_NAME_EVENT_TYPE_FOR_AGGREGATION(EventAggregationRuleTemplate.EVENTTYPE, "Event"),

    ISSUE_TYPE_DATA_COLLECTION("IssueTypeDataCollection", "Data collection"),
    ISSUE_REASON_UNKNOWN_INBOUND_DEVICE("IssueReasonUnknownInboundDevice", "Unknown inbound device"),
    ISSUE_REASON_UNKNOWN_OUTBOUND_DEVICE("IssueReasonUnknownOutboundDevice", "Unknown outbound device"),
    ISSUE_REASON_FAILED_TO_COMMUNICATE("IssueReasonFailedToCommunicate", "Failed to communicate"),
    ISSUE_REASON_CONNECTION_SETUP_FAILED("IssueReasonConnectionSetupFailed", "Connection setup failed"),
    ISSUE_REASON_CONNECTION_FAILED("IssueReasonConnectionFailed", "Connection failed"),
    ISSUE_REASON_POWER_OUTAGE("IssueReasonPowerOutage", "Power outage"),
    ISSUE_REASON_TIME_SYNC_FAILED("IssueReasonSyncFailed", "Time sync failed"),
    ISSUE_REASON_UNREGISTERED_DEVICE("IssueReasonUnregisteredDevice", "Unregistered device"),
    ISSUE_REASON_DESCRIPTION_UNKNOWN_INBOUND_DEVICE("IssueReasonUnknownInboundDeviceDescription", "Unknown inbound device {0}"),
    ISSUE_REASON_DESCRIPTION_UNKNOWN_OUTBOUND_DEVICE("IssueReasonUnknownOutboundDeviceDescription", "Unknown outbound device {0}"),
    ISSUE_REASON_DESCRIPTION_FAILED_TO_COMMUNICATE("IssueReasonFailedToCommunicateDescription", "Failed to communicate with {0}"),
    ISSUE_REASON_DESCRIPTION_CONNECTION_SETUP_FAILED("IssueReasonConnectionSetupFailedDescription", "Connection setup failed to {0}"),
    ISSUE_REASON_DESCRIPTION_CONNECTION_FAILED("IssueReasonConnectionFailedDescription", "Connection failed to {0}"),
    ISSUE_REASON_DESCRIPTION_POWER_OUTAGE("IssueReasonPowerOutageDescription", "Power outage on {0}"),
    ISSUE_REASON_DESCRIPTION_TIME_SYNC_FAILED("IssueReasonSyncFailedDescription", "Time sync failed with {0}"),
    ISSUE_REASON_UNKNOWN("IssueReasonUnknown", "Issue Reason Unknown"),
    ISSUE_REASON_DESCRIPTION_UNREGISTERED_DEVICE("IssueReasonUnregisteredDeviceDescription", "Unregistered device {0}"),
    ISSUE_RELATIVE_PERIOD_CATEGORY("relativeperiod.category.issue", "Issue"),

    ACTION_RETRY_NOW("ActionRetryNow", "Retry now"),
    ACTION_RETRY("ActionRetry", "Retry"),
    ACTION_RETRY_CONNECTION_SUCCESS("ActionRetryConnectionSuccess", "Connection has been retriggered"),
    ACTION_RETRY_COM_TASK_SUCCESS("ActionRetryCommunicationSuccess", "Communication task has been retriggered"),
    CLOSE_ACTION_PROPERTY_CLOSE_STATUS(CloseIssueAction.CLOSE_STATUS, "Close status"),
    CLOSE_ACTION_PROPERTY_COMMENT(CloseIssueAction.COMMENT, "Comment"),
    CLOSE_ACTION_WRONG_STATUS("action.wrong.status", "You are trying to apply the incorrect status"),
    CLOSE_ACTION_ISSUE_CLOSED("action.issue.closed", "Issue closed"),
    CLOSE_ACTION_ISSUE_ALREADY_CLOSED("action.issue.already.closed", "Issue already closed"),
    CLOSE_ACTION_CLOSE_ISSUE("issue.action.closeIssue", "Close issue"),
    CLOSE_ACTION_DEVICE_EXCLUDED_FROM_CLOSING("action.issue.close.device.excluded", "Device ''{0}'' is excluded from autoclosure"),

    EVENT_TITLE_UNKNOWN_INBOUND_DEVICE("EventTitleUnknownInboundDevice", "Unknown inbound device"),
    EVENT_TITLE_UNKNOWN_OUTBOUND_DEVICE("EventTitleUnknownOutboundDevice", "Unknown outbound device"),
    EVENT_TITLE_UNREGISTERED_FROM_GATEWAY("EventTitleUnregisteredFromGateway", "Unregistered from gateway"),
    EVENT_TITLE_REGISTERED_FROM_GATEWAY("EventTitleRegisteredFromGateway", "Registered from gateway"),
    EVENT_TITLE_DEVICE_COMMUNICATION_FAILURE("EventTitleDeviceCommunicationFailure", "Device communication failure"),
    EVENT_TITLE_UNABLE_TO_CONNECT("EventTitleUnableToConnect", "Unable to connect"),
    EVENT_TITLE_CONNECTION_LOST("EventTitleConnectionLost", "Connection lost"),

    DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES(ModuleConstants.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, "Device lifecycle state in device type "),
    EVENT_TEMPORAL_THRESHOLD(ModuleConstants.EVENT_TEMPORAL_THRESHOLD, "Event time threshold"),

    DATA_COLLECTION_ISSUE_ASSOCIATION_PROVIDER(IssueProcessAssociationProvider.ASSOCIATION_TYPE, "Data collection issue"),
    DATA_COLLECTION_ISSUE_REASON_TITLE("issueReasons", "Issue reasons"),
    DATA_COLLECTION_ISSUE_REASON_COLUMN("issueReason", "Issue reason");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}
