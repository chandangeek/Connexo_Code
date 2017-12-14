/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ChannelsContainer;
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
import java.util.StringJoiner;

public class EstimationBlockFormatter {

    private static final EstimationBlockFormatter INSTANCE = new EstimationBlockFormatter();

    public static EstimationBlockFormatter getInstance() {
        return INSTANCE;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DefaultDateTimeFormatters.mediumDate().withLongTime().build().withZone(ZoneId.systemDefault()).withLocale(Locale.ENGLISH);

    public String format(EstimationBlock estimationBlock) {
        StringJoiner joiner = new StringJoiner("");
        if (!estimationBlock.estimatables().isEmpty()) {
            Instant start = estimationBlock.estimatables().get(0).getTimestamp();
            Instant end = estimationBlock.estimatables().get(estimationBlock.estimatables().size() - 1).getTimestamp();
            joiner.add("from ")
                  .add(DATE_TIME_FORMATTER.format(start))
                  .add(" until ")
                  .add(DATE_TIME_FORMATTER.format(end));
        }
        ChannelsContainer channelsContainer = estimationBlock.getChannel().getChannelsContainer();
        return joiner.add(" on ")
                .add(channelsContainer instanceof MetrologyContractChannelsContainer ?
                        channelsContainer.getUsagePoint().map(UsagePoint::getName).orElse("") :
                        channelsContainer.getMeter().map(Meter::getName).orElse(""))
                .add(Optional.of(channelsContainer)
                        .filter(container -> container instanceof MetrologyContractChannelsContainer)
                        .map(MetrologyContractChannelsContainer.class::cast)
                        .map(MetrologyContractChannelsContainer::getMetrologyContract)
                        .map(MetrologyContract::getMetrologyPurpose)
                        .map(MetrologyPurpose::getName)
                        .map(name -> "/" + name + "/")
                        .orElse("/"))
                .add(estimationBlock.getReadingType().getFullAliasName())
                .add(" with ")
                .add(String.valueOf(estimationBlock.estimatables().size()))
                .add(" suspects")
                .toString();
    }
}