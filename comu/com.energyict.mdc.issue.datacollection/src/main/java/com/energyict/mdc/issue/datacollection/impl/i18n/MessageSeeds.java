package com.energyict.mdc.issue.datacollection.impl.i18n;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issue.datacollection.impl.actions.CloseIssueAction;
import com.energyict.mdc.issue.datacollection.impl.templates.BasicDataCollectionRuleTemplate;
import com.energyict.mdc.issue.datacollection.impl.templates.EventAggregationRuleTemplate;

import java.text.MessageFormat;
import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed, TranslationKey {

    // Templates 1 - 99
    BASIC_TEMPLATE_DATACOLLECTION_NAME(1, "TemapleBasicDataCollectionName", "Create issue when specific event occurs", Level.INFO),
    BASIC_TEMPLATE_DATACOLLECTION_DESCRIPTION(2, "TemapleBasicDataCollectionDescription", "Create issue when specific event occurs", Level.INFO),
    TEMPLATE_EVT_AGGREGATION_NAME(5, "TemapleEvtAggregationName", "Events from meters of concentrator", Level.INFO),
    TEMPLATE_EVT_AGGREGATION_DESCRIPTION(6, "TemapleEvtAggregationDescription", "Create an issue based on multiple events that are related", Level.INFO),

    // Parameters 101 - 499
    PARAMETER_NAME_EVENT_TYPE(109, BasicDataCollectionRuleTemplate.EVENTTYPE, "Event", Level.INFO),
    PARAMETER_AUTO_RESOLUTION(110, BasicDataCollectionRuleTemplate.AUTORESOLUTION, "Auto resolution", Level.INFO),
    PARAMETER_NAME_THRESHOLD(111, EventAggregationRuleTemplate.THRESHOLD, "Threshold (%)", Level.INFO),
    PARAMETER_NAME_EVENT_TYPE_FOR_AGGREGATION(112, EventAggregationRuleTemplate.EVENTTYPE, "Event", Level.INFO),

    // Events 701 - 999
    EVENT_BAD_DATA_NO_DEVICE(701, "EventBadDataNoDevice", "Unable to process issue creation event because target device (id = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_KORE_DEVICE(702, "EventBadDataNoEndDevice", "Unable to process issue creation event because target kore device (amrId = {0}) wasn't found", Level.SEVERE),
    EVENT_TITLE_UNKNOWN_INBOUND_DEVICE(703, "EventTitleUnknownInboundDevice", "Unknown inbound device", Level.INFO),
    EVENT_TITLE_UNKNOWN_OUTBOUND_DEVICE(704, "EventTitleUnknownOutboundDevice", "Unknown outbound device", Level.INFO),
    EVENT_TITLE_DEVICE_COMMUNICATION_FAILURE(706, "EventTitleDeviceCommunicationFailure", "Device communication failure", Level.INFO),
    EVENT_TITLE_UNABLE_TO_CONNECT(706, "EventTitleUnableToConnect", "Unable to connect", Level.INFO),
    EVENT_TITLE_CONNECTION_LOST(707, "EventTitleConnectionLost", "Connection lost", Level.INFO),

    // Reasons & issue types 1000 - 1099
    ISSUE_TYPE_DATA_COLELCTION(1000, "IssueTypeDataCollection", "Data Collection", Level.INFO),
    ISSUE_REASON_UNKNOWN_INBOUND_DEVICE(1001, "IssueReasonUnknownInboundDevice", "Unknown inbound device", Level.INFO),
    ISSUE_REASON_UNKNOWN_OUTBOUND_DEVICE(1002, "IssueReasonUnknownOutboundDevice", "Unknown outbound device", Level.INFO),
    ISSUE_REASON_FAILED_TO_COMMUNICATE(1003, "IssueReasonFailedToCommunicate", "Failed to communicate", Level.INFO),
    ISSUE_REASON_CONNECTION_SETUP_FAILED(1004, "IssueReasonConnectionSetupFailed", "Connection setup failed", Level.INFO),
    ISSUE_REASON_CONNECTION_FAILED(1005, "IssueReasonConnectionFailed", "Connection failed", Level.INFO),
    ISSUE_REASON_POWER_OUTAGE(1006, "IssueReasonPowerOutage", "Power outage", Level.INFO),
    ISSUE_REASON_TIME_SYNC_FAILED(1007, "IssueReasonSyncFailed", "Time sync failed", Level.INFO),

    ISSUE_REASON_DESCRIPTION_UNKNOWN_INBOUND_DEVICE(1051, "IssueReasonUnknownInboundDeviceDescription", "Unknown inbound device {0}", Level.INFO),
    ISSUE_REASON_DESCRIPTION_UNKNOWN_OUTBOUND_DEVICE(1052, "IssueReasonUnknownOutboundDeviceDescription", "Unknown outbound device {0}", Level.INFO),
    ISSUE_REASON_DESCRIPTION_FAILED_TO_COMMUNICATE(1053, "IssueReasonFailedToCommunicateDescription", "Failed to communicate with {0}", Level.INFO),
    ISSUE_REASON_DESCRIPTION_CONNECTION_SETUP_FAILED(1054, "IssueReasonConnectionSetupFailedDescription", "Connection setup failed to {0}", Level.INFO),
    ISSUE_REASON_DESCRIPTION_CONNECTION_FAILED(1055, "IssueReasonConnectionFailedDescription", "Connection failed to {0}", Level.INFO),
    ISSUE_REASON_DESCRIPTION_POWER_OUTAGE(1056, "IssueReasonPowerOutageDescription", "Power outage on {0}", Level.INFO),
    ISSUE_REASON_DESCRIPTION_TIME_SYNC_FAILED(1057, "IssueReasonSyncFailedDescription", "Time sync failed {0}", Level.INFO),
    // Validation 1101 - 1499
    FIELD_CAN_NOT_BE_EMPTY (1101, Keys.FIELD_CAN_NOT_BE_EMPTY, "Field can't be empty", Level.SEVERE),

    // Actions 1501 -
    ACTION_RETRY_NOW(1501, "ActionRetryNow", "Retry now", Level.INFO),
    ACTION_RETRY(1502, "ActionRetry", "Retry", Level.INFO),
    ACTION_RETRY_CONNECTION_SUCCESS(1503, "ActionRetryConnectionSuccess", "Connection has been retriggered", Level.INFO),
    ACTION_RETRY_COM_TASK_SUCCESS(1503, "ActionRetryCommunicationSuccess", "Communication task has been retriggered", Level.INFO),
    CLOSE_ACTION_PROPERTY_CLOSE_STATUS(1504, CloseIssueAction.CLOSE_STATUS, "Close status", Level.INFO),
    CLOSE_ACTION_PROPERTY_COMMENT(1505, CloseIssueAction.COMMENT, "Comment", Level.INFO),
    CLOSE_ACTION_WRONG_STATUS(1506, "action.wrong.status", "You are trying to apply the incorrect status" , Level.INFO),
    CLOSE_ACTION_ISSUE_WAS_CLOSED(1507, "action.issue.was.closed", "Issue was closed" , Level.INFO),
    CLOSE_ACTION_ISSUE_ALREADY_CLOSED(1508, "action.issue.already.closed", "Issue already closed" , Level.INFO),
    CLOSE_ACTION_CLOSE_ISSUE(1509, "issue.action.closeIssue", "Close issue", Level.INFO),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return IssueService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public String getFormated(Object... args) {
        return MessageFormat.format(this.getDefaultFormat(), args);
    }

    public static String getString(MessageSeed messageSeed, Thesaurus thesaurus, Object... args) {
        String text = thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat());
        return MessageFormat.format(text, args);
    }

    public String getTranslated(Thesaurus thesaurus, Object... args){
        if (thesaurus == null) {
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
    }

    public static MessageSeeds getByKey(String key) {
        if (key != null) {
            for (MessageSeeds column : MessageSeeds.values()) {
                if (column.getKey().equals(key)) {
                    return column;
                }
            }
        }
        return null;
    }

    public static class Keys {
        private Keys() {}

        public static final String FIELD_CAN_NOT_BE_EMPTY = "FieldCanNotBeEmpty";
    }
}
