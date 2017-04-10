/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class EstimationBlockFormatter {

    private static final EstimationBlockFormatter INSTANCE = new EstimationBlockFormatter();

    public static EstimationBlockFormatter getInstance() {
        return INSTANCE;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DefaultDateTimeFormatters.mediumDate().withLongTime().build().withZone(ZoneId.systemDefault()).withLocale(Locale.ENGLISH);

    public String format(EstimationBlock estimationBlock) {
        Instant start = estimationBlock.estimatables().get(0).getTimestamp();
        Instant end = estimationBlock.estimatables().get(estimationBlock.estimatables().size() - 1).getTimestamp();
        Optional<UsagePoint> usagePointOptional = estimationBlock.getChannel().getChannelsContainer().getUsagePoint();
        Optional<Meter> meterOptional = estimationBlock.getChannel().getChannelsContainer().getMeter();
        return " from " + DATE_TIME_FORMATTER.format(start)
                + " until " + DATE_TIME_FORMATTER.format(end)
                + " on " + estimationBlock.getReadingType().getFullAliasName()
                + Optional.of(estimationBlock.getChannel().getChannelsContainer())
                        .filter(container -> container instanceof MetrologyContractChannelsContainer)
                        .map(MetrologyContractChannelsContainer.class::cast)
                        .map(MetrologyContractChannelsContainer::getMetrologyContract)
                        .map(MetrologyContract::getMetrologyPurpose)
                        .map(MetrologyPurpose::getName)
                        .map(name -> " of purpose " + name)
                        .orElse("")
                + " of"
                + usagePointOptional
                        .map(UsagePoint::getName)
                        .map(name -> " usage point " + name)
                        .orElse("")
                + usagePointOptional // if both usage point and meter are present, add 'and'
                        .flatMap(usagePoint -> meterOptional)
                        .map(meter -> " and")
                        .orElse("")
                + meterOptional
                        .map(Meter::getName)
                        .map(name -> " meter " + name)
                        .orElse("")
                + " with " + estimationBlock.estimatables().size() + " suspects";
    }
}
