package com.elster.jupiter.issue.datacollection.impl.i18n;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed {

    BASIC_TEMPLATE_DATACOLLECTION_NAME(1, "TemapleBasicDataCollectionName", "Basic data collection issues", Level.INFO),
    BASIC_TEMPLATE_DATACOLLECTION_DESCRIPTION(2, "TemapleBasicDataCollectionDescription", "Creates issue based on specified issue datacollection event topic", Level.INFO),
    SLOPE_DETECTION_TEMPLATE_NAME (3, "TemapleSlopeDetectionName", "Slope detection", Level.INFO),
    SLOPE_DETECTION_TEMPLATE_DESCRIPTION (4, "TemapleSlopeDetectionDescription", "Create an issue based on predictive analysis / correlations", Level.INFO),
    TREND_PERIOD_UNIT_DAYS(5, "TrendPeriodUnitDays", " days", Level.INFO),
    TREND_PERIOD_UNIT_HOURS(6, "TrendPeriodUnitHours", " hours", Level.INFO),
    ISSUE_CREATION_RULE_PARAMETER_ABSENT(7, "IssueCreationRuleParameterAbsent", "Required parameter is absent", Level.SEVERE),
    METER_TEMPLATE_NAME(8, "TemapleMeterName", "Basic meter issues", Level.INFO),
    METER_TEMPLATE_DESCRIPTION(9, "TemapleMeterDescription", "Creates issue based on specified meter", Level.INFO),
    PARAMETER_NAME_END_DEVICE_TYPE(10, "ParameterNameEndDeviceType", "End device event type", Level.INFO),
    PARAMETER_NAME_EVENT_TOPIC(11, "ParameterNameEventTopic", "Event topic", Level.INFO),
    PARAMETER_NAME_MAX_SLOPE(12, "ParameterNameMaxSlope", "Threshold", Level.INFO),
    PARAMETER_NAME_READING_TYPE(13, "ParameterNameReadingType", "CIM Reading type", Level.INFO),
    PARAMETER_NAME_TREND_PERIOD(14, "ParameterNameTrendPeriod", "Trend period", Level.INFO),
    PARAMETER_NAME_TREND_PERIOD_UNIT(15, "ParameterNameTrendPeriodUnit", "Trend period units", Level.INFO),
    PARAMETER_NAME_MAX_SLOPE_SUFFIX(16, "ParameterNameTrendPeriodUnitSuffix", "C/hours", Level.INFO),
    PARAMETER_NAME_READING_TYPE_DESCRIPTION(17, "ParameterNameReadingTypeDescription", "Provide the value for the 18 attributes of the CIM reading type. Separate each value with a \".\"", Level.INFO),
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
