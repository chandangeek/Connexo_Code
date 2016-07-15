/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.GasDayOptions;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.DayMonthTime;

import org.osgi.framework.BundleContext;

import java.time.LocalTime;
import java.time.MonthDay;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates the {@link com.elster.jupiter.metering.impl.GasDayOptions}
 * from the configuration parameters if it does not exist yet.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (16:23)
 */
public class GasDayOptionsCreator {
    static final String GAS_DAY_START_PROPERTY_NAME = "com.elster.jupiter.gasday.start";
    private static final Pattern GAS_DAY_START_PROPERTY_PATTERN = Pattern.compile("(\\d\\d)-(\\d\\d)@(\\d\\d)([A|P]M)");
    private static final Logger LOGGER = Logger.getLogger(GasDayOptionsCreator.class.getName());

    private final ServerMeteringService meteringService;

    public GasDayOptionsCreator(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public void createIfMissing(BundleContext context) {
        GasDayOptions gasDayOptions = this.meteringService.getGasDayOptions();
        if (gasDayOptions == null) {
            this.createGasDayOptionsFromConfigurationParameters(context);
        } else {
            LOGGER.info(() -> "Gas day options already configured: " + gasDayOptions.getYearStart().toString());
        }
    }

    private void createGasDayOptionsFromConfigurationParameters(BundleContext context) {
        String gasDayStartPropertyValue = context.getProperty(GAS_DAY_START_PROPERTY_NAME);
        if (Checks.is(gasDayStartPropertyValue).emptyOrOnlyWhiteSpace()) {
            LOGGER.warning(() -> "No value found for configuration property " + GAS_DAY_START_PROPERTY_NAME + ", not creating the gas day options entity, which will result in errors when data aggregation is executed for gas related data");
        } else {
            Matcher matcher = GAS_DAY_START_PROPERTY_PATTERN.matcher(gasDayStartPropertyValue);
            if (matcher.matches()) {
                this.meteringService.createGasDayOptions(this.parseDayMonthTime(matcher));
            } else {
                LOGGER.severe(() -> "Value for configuration property " + GAS_DAY_START_PROPERTY_NAME + " could not be parsed. Expected " + GAS_DAY_START_PROPERTY_PATTERN.pattern() + " but got " + gasDayStartPropertyValue);
            }
        }
    }

    private DayMonthTime parseDayMonthTime(Matcher matcher) {
        return DayMonthTime.from(
                MonthDay.of(this.matchingInt(matcher, 1), this.matchingInt(matcher, 2)),
                this.parseLocalTimeFrom(matcher));
    }

    private int matchingInt(Matcher matcher, int group) {
        return Integer.parseInt(matcher.group(group));
    }

    private LocalTime parseLocalTimeFrom(Matcher matcher) {
        int hour = this.matchingInt(matcher, 3);
        if ("PM".equals(matcher.group(4))) {
            hour = hour + 12;
        }
        return LocalTime.of(hour, 0);
    }

}