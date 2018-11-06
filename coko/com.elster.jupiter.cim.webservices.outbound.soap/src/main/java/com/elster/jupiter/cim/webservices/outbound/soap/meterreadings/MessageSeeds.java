/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    NO_WEB_SERVICE_ENDPOINTS(1, Constants.NO_WEB_SERVICE_ENDPOINTS, "No published web service endpoint is found to send meter readings.", Level.SEVERE),
    NO_READINGS_IN_EVENT(2, Constants.NO_READINGS_IN_EVENT, "No readings found to send out.", Level.SEVERE),
    READINGS_METER_IS_NOT_THE_SAME(3, Constants.READINGS_METER_IS_NOT_THE_SAME, "Readings do not relate to the same meter.", Level.SEVERE),
    READINGS_USAGE_POINT_IS_NOT_THE_SAME(4, Constants.READINGS_USAGE_POINT_IS_NOT_THE_SAME, "Readings do not relate to the same usage point.", Level.SEVERE),;

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
        return SendMeterReadingsProvider.NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public enum Constants {
        ;
        public static final String NO_WEB_SERVICE_ENDPOINTS = "NoWebServiceEndpoints";
        public static final String NO_READINGS_IN_EVENT = "NoReadingsInEvent";
        public static final String READINGS_METER_IS_NOT_THE_SAME = "ReadingsMeterNotTheSame";
        public static final String READINGS_USAGE_POINT_IS_NOT_THE_SAME = "ReadingsUsagePointNotTheSame";
    }
}