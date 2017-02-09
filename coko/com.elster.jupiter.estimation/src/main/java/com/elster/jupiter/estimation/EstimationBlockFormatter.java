/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EstimationBlockFormatter {

    private static final EstimationBlockFormatter INSTANCE = new EstimationBlockFormatter();

    public static EstimationBlockFormatter getInstance() {
        return INSTANCE;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DefaultDateTimeFormatters.mediumDate().withLongTime().build().withZone(ZoneId.systemDefault()).withLocale(Locale.ENGLISH);

    public String format(EstimationBlock estimationBlock) {
        return estimationBlock.getReadingType().getAliasName() +
                " from " +
                DATE_TIME_FORMATTER.format(estimationBlock.estimatables().get(0).getTimestamp()) +
                " until " +
                DATE_TIME_FORMATTER.format(estimationBlock.estimatables().get(estimationBlock.estimatables().size() - 1).getTimestamp()) +
                " on " +
                estimationBlock.getReadingType().getAliasName() +
                estimationBlock.getChannel().getChannelsContainer().getMeter().map(meter -> " and meter " + meter.getName()).orElse("") +
                " with " +
                estimationBlock.estimatables().size() +
                " suspects";
    }
}
