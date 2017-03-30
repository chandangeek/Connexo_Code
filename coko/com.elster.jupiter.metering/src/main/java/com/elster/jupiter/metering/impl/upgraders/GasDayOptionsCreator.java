/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.DayMonthTime;

import org.osgi.framework.BundleContext;

import java.time.LocalTime;
import java.time.MonthDay;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates the {@link GasDayOptions}
 * from the configuration parameters if it does not exist yet.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (16:23)
 */
public class GasDayOptionsCreator {
    static final String GAS_DAY_START_PROPERTY_NAME = "com.elster.jupiter.gasday.start";
    private static final Pattern GAS_DAY_START_PROPERTY_PATTERN = Pattern.compile("(" + Month.names("|") + ")@(\\d\\d)([A|P]M)");
    private static final Logger LOGGER = Logger.getLogger(GasDayOptionsCreator.class.getName());

    private final ServerMeteringService meteringService;

    public GasDayOptionsCreator(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public void createIfMissing(BundleContext context) {
        Optional<GasDayOptions> gasDayOptions = this.meteringService.getGasDayOptions();
        if (!gasDayOptions.isPresent()) {
            this.createGasDayOptionsFromConfigurationParameters(context);
        } else {
            LOGGER.info(() -> "Gas day options already configured: " + gasDayOptions.get().getYearStart().toString());
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
                LOGGER.severe(() -> "Value for configuration property " + GAS_DAY_START_PROPERTY_NAME + " could not be parsed. Expected " + GAS_DAY_START_PROPERTY_PATTERN
                        .pattern() + " but got " + gasDayStartPropertyValue);
            }
        }
    }

    private DayMonthTime parseDayMonthTime(Matcher matcher) {
        return DayMonthTime.from(
                Month.parse(matcher.group(1)),
                this.parseLocalTimeFrom(matcher));
    }

    private LocalTime parseLocalTimeFrom(Matcher matcher) {
        int hour = this.matchingInt(matcher, 2);
        if ("PM".equals(matcher.group(3))) {
            hour = hour + 12;
        }
        return LocalTime.of(hour, 0);
    }

    private int matchingInt(Matcher matcher, int group) {
        return Integer.parseInt(matcher.group(group));
    }

    private enum Month {
        JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC;

        private MonthDay toMonthDay() {
            return MonthDay.of(this.ordinal() + 1, 1);
        }

        static String names(String separator) {
            return Stream.of(values()).map(Month::name).collect(Collectors.joining(separator));
        }

        static MonthDay parse(String value) {
            return Month.valueOf(value).toMonthDay();
        }
    }
}