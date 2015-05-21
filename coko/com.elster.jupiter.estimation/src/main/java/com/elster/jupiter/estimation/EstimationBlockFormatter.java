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
        return new StringBuilder().append(estimationBlock.getReadingType().getAliasName())
                .append(" from ")
                .append(DATE_TIME_FORMATTER.format(estimationBlock.estimatables().get(0).getTimestamp()))
                .append(" until ")
                .append(DATE_TIME_FORMATTER.format(estimationBlock.estimatables().get(estimationBlock.estimatables().size() - 1).getTimestamp()))
                .append(" on ")
                .append(estimationBlock.getReadingType().getAliasName())
                .append(estimationBlock.getChannel().getMeterActivation().getMeter().map(meter -> " and device " + meter.getMRID()).orElse(""))
                .append(" with ")
                .append(estimationBlock.estimatables().size())
                .append(" suspects")
                .toString();
    }
}
