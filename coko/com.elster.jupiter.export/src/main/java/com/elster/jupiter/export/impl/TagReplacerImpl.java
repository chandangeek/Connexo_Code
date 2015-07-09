package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.util.UpdatableHolder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.text.DecimalFormat;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TagReplacerImpl implements TagReplacer {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmmss");
    private final StructureMarker structureMarker;
    private final Clock clock;
    private final int sequenceNumber;

    TagReplacerImpl(StructureMarker structureMarker, Clock clock, int sequenceNumber) {
        this.structureMarker = structureMarker;
        this.clock = clock;
        this.sequenceNumber = sequenceNumber;
    }

    static TagReplacer asTagReplacer(Clock clock, StructureMarker structureMarker, int sequenceNumber) {
        return new TagReplacerImpl(structureMarker, clock, sequenceNumber);
    }

    @Override
    public String replaceTags(String template) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return new TagReplacerForNow(now).replaceTags(template);
    }

    private class TagReplacerForNow implements TagReplacer {
        private final ZonedDateTime now;
        private final DecimalFormat threeDigits = new DecimalFormat("00");
        private final DecimalFormat fourDigits = new DecimalFormat("#0000");
        private final DecimalFormat twoDigits = new DecimalFormat("00");
        private final List<UnaryOperator<String>> replacements = ImmutableList.of(
                replaceIdentifier(), 
                replaceDate(), 
                replaceDateFormat(),
                replaceTime(),
                replaceYear(), 
                replaceMonth(),
                replaceDay(),
                replaceSecond(),
                replaceMilliSec(),
                replaceDataDate(),
                replaceDataTime(),
                replaceDataEndDate(),
                replaceDataEndTime(),
                replaceDataYearAndMonth(),
                replaceSeqNrWithinDay()
                );

        private UnaryOperator<String> replaceDataYearAndMonth() {
            return dataTime()
                    .map(dataTime -> (UnaryOperator<String>) (string -> string.replace("<datayearandmonth>", YEAR_MONTH_FORMAT.format(dataTime))))
                    .orElse(string -> string);
        }

        private UnaryOperator<String> replaceDataDate() {
            return dataTime()
                    .map(dataTime -> (UnaryOperator<String>) (string -> string.replace("<datadate>", DATE_FORMAT.format(dataTime))))
                    .orElse(string -> string);
        }

        private UnaryOperator<String> replaceDataEndDate() {
            return dataEndTime()
                    .map(dataTime -> (UnaryOperator<String>) (string -> string.replace("<dataenddate>", DATE_FORMAT.format(dataTime))))
                    .orElse(string -> string);
        }

        private UnaryOperator<String> replaceDataEndTime() {
            return dataEndTime()
                    .map(dataTime -> (UnaryOperator<String>) (string -> string.replace("<dataendtime>", TIME_FORMAT.format(dataTime))))
                    .orElse(string -> string);
        }

        private Optional<ZonedDateTime> dataTime() {
            return structureMarker.getPeriod()
                            .filter(Range::hasLowerBound)
                            .map(Range::lowerEndpoint)
                            .map(instant -> ZonedDateTime.ofInstant(instant, clock.getZone()));
        }

        private Optional<ZonedDateTime> dataEndTime() {
            return structureMarker.getPeriod()
                    .filter(Range::hasUpperBound)
                    .map(Range::upperEndpoint)
                    .map(instant -> ZonedDateTime.ofInstant(instant, clock.getZone()));
        }

        private UnaryOperator<String> replaceDataTime() {
            return dataTime()
                    .map(dataTime -> (UnaryOperator<String>) (string -> string.replace("<datatime>", TIME_FORMAT.format(dataTime))))
                    .orElse(string -> string);
        }

        public TagReplacerForNow(ZonedDateTime now) {
            this.now = now;
            
        }

        @Override
        public String replaceTags(String template) {
            UpdatableHolder<String> result = new UpdatableHolder<>(template);
            replacements.forEach(result::update);
            return result.get();
        }

        private String replacingDateFormat(String template) {
            Pattern pattern = Pattern.compile("<dateformat:([^>]*)>");
            Matcher matcher = pattern.matcher(template);
            if (matcher.find()) {
                String replacement = DateTimeFormatter.ofPattern(matcher.group(1)).format(now);
                return matcher.replaceAll(replacement);
            }
            return template;
        }

        private UnaryOperator<String> replaceIdentifier() {
            return string -> string.replace("<identifier>", structureMarker.getStructurePath().get(0));
        }

        private UnaryOperator<String> replaceSecond() {
            return string -> string.replace("<sec>", twoDigits.format(now.getSecond()));
        }

        private UnaryOperator<String> replaceDay() {
            return string -> string.replace("<dateday>", twoDigits.format(now.getDayOfMonth()));
        }

        private UnaryOperator<String> replaceMonth() {
            return string -> string.replace("<datemonth>", twoDigits.format(now.getMonthValue()));
        }

        private UnaryOperator<String> replaceYear() {
            return string -> string.replace("<dateyear>", fourDigits.format(now.getYear()));
        }

        private UnaryOperator<String> replaceMilliSec() {
            return string -> string.replace("<millisec>", threeDigits.format(now.get(ChronoField.MILLI_OF_SECOND)));
        }

        private UnaryOperator<String> replaceDateFormat() {
            return this::replacingDateFormat;
        }

        private UnaryOperator<String> replaceTime() {
            return string -> string.replace("<time>", TIME_FORMAT.format(now));
        }

        private UnaryOperator<String> replaceDate() {
            return string -> string.replace("<date>", DATE_FORMAT.format(now));
        }

        private UnaryOperator<String> replaceSeqNrWithinDay() {
            return string -> string.replace("<seqnrwithinday>", new DecimalFormat("00").format(sequenceNumber));
        }
    }

}
