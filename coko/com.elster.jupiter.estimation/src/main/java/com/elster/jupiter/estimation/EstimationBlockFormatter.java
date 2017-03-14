/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static com.elster.jupiter.util.streams.Functions.map;

public class EstimationBlockFormatter {

    private static final EstimationBlockFormatter INSTANCE = new EstimationBlockFormatter();

    public static EstimationBlockFormatter getInstance() {
        return INSTANCE;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DefaultDateTimeFormatters.mediumDate().withLongTime().build().withZone(ZoneId.systemDefault()).withLocale(Locale.ENGLISH);

    private boolean getMatchingMetrologyPurposes(MetrologyContract metrologyContract, ReadingType readingType) {
       return metrologyContract.getDeliverables()
                .stream()
                .filter(readingTypeDeliverable -> readingTypeDeliverable.getReadingType().equals((readingType)))
                .findAny()
                .isPresent();
    }

    public String format(EstimationBlock estimationBlock) {
        return " from " +
                DATE_TIME_FORMATTER.format(estimationBlock.estimatables().get(0).getTimestamp()) +
                " until " +
                DATE_TIME_FORMATTER.format(estimationBlock.estimatables().get(estimationBlock.estimatables().size() - 1).getTimestamp()) +
                " on " +
                estimationBlock.getReadingType().getFullAliasName() +
                " of purpose " +
                estimationBlock.getChannel().getChannelsContainer().getUsagePoint()
                        .get().getCurrentEffectiveMetrologyConfiguration().get()
                        .getMetrologyConfiguration().getContracts()
                        .stream()
                        .filter(metrologyContract -> getMatchingMetrologyPurposes(metrologyContract, estimationBlock.getReadingType()))
                        .map(metrologyContract -> metrologyContract.getMetrologyPurpose().getName())
                        .collect(Collectors.joining(" ,")) +
                " of " +
                estimationBlock.getChannel().getChannelsContainer().getUsagePoint().map(usagePoint -> " usage point " + usagePoint.getName()).orElse("") +
                estimationBlock.getChannel().getChannelsContainer().getMeter().map(meter -> " and meter " + meter.getName()).orElse("") +
                " with " +
                estimationBlock.estimatables().size() +
                " suspects";
    }
}
