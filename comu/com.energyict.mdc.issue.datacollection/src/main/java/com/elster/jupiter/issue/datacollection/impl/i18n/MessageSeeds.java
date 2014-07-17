package com.elster.jupiter.issue.datacollection.impl.i18n;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed {

    BASIC_TEMPLATE_DATACOLLECTION_NAME(1, "TemapleBasicDataCollectionName", "Create issue when specific event occurs", Level.INFO),
    BASIC_TEMPLATE_DATACOLLECTION_DESCRIPTION(2, "TemapleBasicDataCollectionDescription", "Create issue when specific event occurs", Level.INFO),
    SLOPE_DETECTION_TEMPLATE_NAME (3, "TemapleSlopeDetectionName", "Create issue when slope is detected", Level.INFO),
    SLOPE_DETECTION_TEMPLATE_DESCRIPTION (4, "TemapleSlopeDetectionDescription", "Create an issue based on predictive analysis / correlations", Level.INFO),
    TREND_PERIOD_UNIT_DAYS(5, "TrendPeriodUnitDays", " days", Level.INFO),
    TREND_PERIOD_UNIT_HOURS(6, "TrendPeriodUnitHours", " hours", Level.INFO),
    ISSUE_CREATION_RULE_PARAMETER_ABSENT(7, "IssueCreationRuleParameterAbsent", "Required parameter is absent", Level.SEVERE),

    PARAMETER_NAME_MAX_SLOPE(12, "ParameterNameMaxSlope", "Threshold", Level.INFO),
    PARAMETER_NAME_READING_TYPE(13, "ParameterNameReadingType", "CIM Reading type", Level.INFO),
    PARAMETER_NAME_TREND_PERIOD(14, "ParameterNameTrendPeriod", "Trend period", Level.INFO),
    PARAMETER_NAME_TREND_PERIOD_UNIT(15, "ParameterNameTrendPeriodUnit", "Trend period units", Level.INFO),
    PARAMETER_NAME_MAX_SLOPE_SUFFIX(16, "ParameterNameTrendPeriodUnitSuffix", "&deg;C/hours", Level.INFO),
    PARAMETER_NAME_READING_TYPE_DESCRIPTION(17, "ParameterNameReadingTypeDescription", "Provide the value for the 18 attributes of the CIM reading type. Separate each value with a \".\"", Level.INFO),
    TEMPLATE_EVT_AGGREGATION_NAME (18, "TemapleEvtAggregationName", "Events from meters of concentrator", Level.INFO),
    TEMPLATE_EVT_AGGREGATION_DESCRIPTION (19, "TemapleEvtAggregationDescription", "Create an issue based on multiple events that are related", Level.INFO),
    PARAMETER_NAME_EVENT_TYPE (20, "ParameterNameEventType", "Event", Level.INFO),
    PARAMETER_NAME_THRESHOLD (21, "ParameterNameThreshold", "Threshold", Level.INFO),
    PARAMETER_NAME_THRESHOLD_SUFFIX (21, "ParameterNameThresholdSuffix", "%", Level.INFO),
    ISSUE_CREATION_RULE_PARAMETER_INCORRECT(22, "IssueCreationRuleParameterIncorrect", "Parameter has wrong value", Level.SEVERE),
    EVENT_BAD_DATA_NO_DEVICE(23, "EventBadDataNoDevice", "Unable to process issue creation event because target device (id = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_END_DEVICE(24, "EventBadDataNoEndDevice", "Unable to process issue creation event because target end device (amrId = {0}) wasn't found", Level.SEVERE),
    EVENT_TITLE_UNKNOWN_INBOUND_DEVICE(25, "EventTitleUnknowInboundDevice", "Unknow Inbound Device", Level.INFO),
    EVENT_TITLE_UNKNOWN_OUTBOUND_DEVICE(26, "EventTitleUnknownOutboundDevice", "Unknown Outbound Device", Level.INFO),
    EVENT_TITLE_DEVICE_COMMUNICATION_FAILURE(27, "EventTitleDeviceCommunicationFailure", "Device Communication Failure", Level.INFO),
    EVENT_TITLE_DEVICE_CONNECTION_SETUP_FAILURE(28, "EventTitleDeviceConnectionSetupFailure", "Device Connection Setup Failure", Level.INFO),
    EVENT_TITLE_DEVICE_CONNECTION_FAILURE(29, "EventTitleDeviceConnectionFailure", "Device Connection Failure", Level.INFO),
    EVENT_TITLE_DEVICE_EVENT(30, "EventTitleDeviceCreated", "Device Created", Level.INFO),
    EVENT_BAD_DATA_NO_EVENT_IDENTIFIER(31, "EventBadDataNoVentIdentifier", "Unable to process issue creation event because target event identifier wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_WRONG_EVENT_TYPE(32, "EventBadDataWrongEventType", "Unable to process issue creation event because endDeviceEventType doesn't match to eventRecord type mrId", Level.SEVERE),
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

    public String getFormated(Object... args){
        return MessageFormat.format(this.getDefaultFormat(), args);
    }

    public static String getString(MessageSeed messageSeed, Thesaurus thesaurus, Object... args){
        String text = thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat());
        return MessageFormat.format(text, args);
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
}
