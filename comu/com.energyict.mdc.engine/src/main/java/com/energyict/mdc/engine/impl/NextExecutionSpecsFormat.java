package com.energyict.mdc.engine.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.scheduling.NextExecutionSpecs;

import java.util.Calendar;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides a human readable representation of a {@link com.energyict.mdc.scheduling.NextExecutionSpecs} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (16:04)
 */
class NextExecutionSpecsFormat {

    public enum TranslationKeys implements TranslationKey {
        SUNDAY("Sunday"),
        MONDAY("Monday"),
        TUESDAY("Tuesday"),
        WEDNESDAY("Wednesday"),
        THURSDAY("Thursday"),
        FRIDAY("Friday"),
        SATURDAY("Saturday"),
        EVERY_MONTH("Every {0} month(s) on day {1} at {2,number,00}:{3,number,00}"),
        EVERY_WEEK("Every {0} week(s) on {1} at {2,number,00}:{3,number,00}"),
        EVERY_DAY("Every {0} day(s) at {1,number,00}:{2,number,00}"),
        EVERY_HOUR("Every {0} hour(s) at {1} minute(s)"),
        EVERY_MINUTE("Every {0} minute(s)");

        private final String defaultFormat;

        TranslationKeys(String defaultFormat) {
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return "mdc.next.execution.specs." + this.name().toLowerCase();
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    private enum Weekday {
        SUNDAY(Calendar.SUNDAY),
        MONDAY(Calendar.MONDAY),
        TUESDAY(Calendar.TUESDAY),
        WEDNESDAY(Calendar.WEDNESDAY),
        THURSDAY(Calendar.THURSDAY),
        FRIDAY(Calendar.FRIDAY),
        SATURDAY(Calendar.SATURDAY);

        private final int calendarId;

        Weekday(int calendarId) {
            this.calendarId = calendarId;
        }

        TranslationKeys translationKey() {
            return TranslationKeys.valueOf(this.name());
        }

        int calendarId() {
            return this.calendarId;
        }
    }
    private final Thesaurus thesaurus;
    private final Map<Integer, String> weekDaysMap;

    NextExecutionSpecsFormat(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.weekDaysMap =
                Stream
                    .of(Weekday.SUNDAY, Weekday.MONDAY, Weekday.TUESDAY, Weekday.WEDNESDAY, Weekday.THURSDAY, Weekday.FRIDAY, Weekday.SATURDAY)
                    .collect(Collectors.toMap(
                            Weekday::calendarId,
                            weekday -> thesaurus.getFormat(weekday.translationKey()).format()));
    }

    public String format(NextExecutionSpecs specs) {
        TemporalExpression temporalExpression = specs.getTemporalExpression();
        TemporalExpression.Offsets offsets = temporalExpression.getOffsetInDaysHoursMinutes();
        int days = offsets.getDays();
        int hours = offsets.getHours();
        int minutes = offsets.getMinutes();
        TimeDuration frequency = temporalExpression.getEvery();
        switch (frequency.getTimeUnitCode()) {
            case Calendar.MONTH: {
                return this.thesaurus
                            .getFormat(TranslationKeys.EVERY_MONTH)
                            .format(
                                frequency.getCount(),
                                days + 1,
                                hours,
                                minutes);
            }
            case Calendar.WEEK_OF_YEAR: {
                return this.thesaurus
                        .getFormat(TranslationKeys.EVERY_WEEK)
                        .format(
                            frequency.getCount(),
                            dayNameForOffset(days),
                            hours,
                            minutes);
            }
            case Calendar.DATE: {
                return this.thesaurus
                        .getFormat(TranslationKeys.EVERY_DAY)
                        .format(
                            frequency.getCount(),
                            hours,
                            minutes);
            }
            case Calendar.HOUR_OF_DAY: {
                return this.thesaurus
                        .getFormat(TranslationKeys.EVERY_HOUR)
                        .format(
                            frequency.getCount(),
                            minutes);
            }
            case Calendar.MINUTE: {
                return this.thesaurus
                        .getFormat(TranslationKeys.EVERY_MINUTE)
                        .format(frequency.getCount());
            }
            default: {
                return "?";
            }
        }
    }

    private String dayNameForOffset(int offset) {
        int firstDay = Calendar.getInstance().getFirstDayOfWeek();
        if (firstDay + offset <= 7) {
            return this.weekDaysMap.get(firstDay+offset);
        } else {
            return this.weekDaysMap.get(firstDay + offset - 7);
        }
    }

}