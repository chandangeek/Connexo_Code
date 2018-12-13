/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.util.UpdatableHolder;
import com.google.common.collect.ImmutableList;

import java.text.DecimalFormat;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CertificateExportTagReplacer {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmmss");
    private final Clock clock;


    CertificateExportTagReplacer(Clock clock) {
        this.clock = clock;
    }

    static CertificateExportTagReplacer asTagReplacer(Clock clock) {
        return new CertificateExportTagReplacer(clock);
    }

    public String replaceTags(String template) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return new TagReplacerForNow(now).replaceTags(template);
    }

    private class TagReplacerForNow {
        private final ZonedDateTime now;
        private final DecimalFormat threeDigits = new DecimalFormat("00");
        private final DecimalFormat fourDigits = new DecimalFormat("#0000");
        private final DecimalFormat twoDigits = new DecimalFormat("00");
        private final List<UnaryOperator<String>> replacements = ImmutableList.of(
                replaceDate(),
                replaceDateFormat(),
                replaceTime(),
                replaceYear(),
                replaceMonth(),
                replaceDay(),
                replaceSecond(),
                replaceMilliSec()
        );

        public TagReplacerForNow(ZonedDateTime now) {
            this.now = now;

        }

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
    }

}
