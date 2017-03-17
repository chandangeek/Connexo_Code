/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.tou;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;

import javax.inject.Inject;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.stream.Stream;

/**
 * Creates the peak/offpeak {@link com.elster.jupiter.calendar.Calendar} for the Belgian electricity market.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-17 (13:17)
 */
public class CreateCalendarCommand {

    private static final String WORKDAY_DAYTYPE_NAME = "workday";
    private static final String HOLIDAY_DAYTYPE_NAME = "holiday";
    private static final String WEEKEND_DAYTYPE_NAME = "weekend";
    public static final String PERIOD_NAME = "Always";

    private final CalendarService calendarService;

    @Inject
    public CreateCalendarCommand(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    void createCalendar(EventSet eventSet) {
        CalendarService.CalendarBuilder builder = this.calendarService
                .newCalendar("Peak/Offpeak (Belgium)", Year.of(2015), eventSet)
                .category(this.getTimeOfUseCategory())
                .description("Default calendar for Belgian market (for demo purposes only)")
                .newDayType(WEEKEND_DAYTYPE_NAME).eventWithCode(EventCodes.OFFPEAK.getCode()).startsFrom(LocalTime.MIDNIGHT).add()
                .newDayType(HOLIDAY_DAYTYPE_NAME).eventWithCode(EventCodes.OFFPEAK.getCode()).startsFrom(LocalTime.MIDNIGHT).add()
                .newDayType(WORKDAY_DAYTYPE_NAME)
                .eventWithCode(EventCodes.OFFPEAK.getCode()).startsFrom(LocalTime.MIDNIGHT)
                .eventWithCode(EventCodes.PEAK.getCode()).startsFrom(LocalTime.of(7, 0))
                .eventWithCode(EventCodes.OFFPEAK.getCode()).startsFrom(LocalTime.of(21, 0))
                .add()
                .addPeriod(PERIOD_NAME, WORKDAY_DAYTYPE_NAME, WORKDAY_DAYTYPE_NAME, WORKDAY_DAYTYPE_NAME, WORKDAY_DAYTYPE_NAME, WORKDAY_DAYTYPE_NAME, WEEKEND_DAYTYPE_NAME, WEEKEND_DAYTYPE_NAME)
                .on(MonthDay.of(Month.JANUARY, 1)).transitionTo(PERIOD_NAME);
        Stream.of(RecurringHolidays.values()).forEach(recurringHolidays -> recurringHolidays.addTo(builder));
        Stream.of(FixedHolidays.values()).forEach(recurringHolidays -> recurringHolidays.addTo(builder));
        builder
            .add()
            .activate();
    }

    private Category getTimeOfUseCategory() {
        return this.calendarService
                .findCategoryByName(OutOfTheBoxCategory.TOU.name())
                .orElseThrow(() -> new IllegalStateException("Calendar service installer failure, time of use category is missing"));
    }

    private enum RecurringHolidays {
        NEWYEARS_DAY {
            @Override
            MonthDay recurringMonthDay() {
                return MonthDay.of(Month.JANUARY, 1);
            }
        },
        LABOR_DAY {
            @Override
            MonthDay recurringMonthDay() {
                return MonthDay.of(Month.MAY, 1);
            }
        },
        NATIONAL_HOLIDAY {
            @Override
            MonthDay recurringMonthDay() {
                return MonthDay.of(Month.JULY, 21);
            }
        },
        ASSUMPTION_DAY {
            @Override
            MonthDay recurringMonthDay() {
                return MonthDay.of(Month.AUGUST, 15);
            }
        },
        ALL_SOULS_DAY {
            @Override
            MonthDay recurringMonthDay() {
                return MonthDay.of(Month.NOVEMBER, 1);
            }
        },
        ARMISTICE_DAY {
            @Override
            MonthDay recurringMonthDay() {
                return MonthDay.of(Month.NOVEMBER, 11);
            }
        },
        CHRISTMAS {
            @Override
            MonthDay recurringMonthDay() {
                return MonthDay.of(Month.DECEMBER, 25);
            }
        };

        void addTo(CalendarService.CalendarBuilder builder) {
            builder.except(HOLIDAY_DAYTYPE_NAME).occursAlwaysOn(this.recurringMonthDay()).add();
        }

        abstract MonthDay recurringMonthDay();
    }

    private enum FixedHolidays {
        EASTER_MONDAY_2015 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.APRIL, 6);
            }
        },
        ASCENSION_DAY_2015 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MAY, 14);
            }
        },
        PENTECOST_MONDAY_2015 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MAY, 25);
            }
        },
        EASTER_MONDAY_2016 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MARCH, 28);
            }
        },
        ASCENSION_DAY_2016 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MAY, 5);
            }
        },
        PENTECOST_MONDAY_2016 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MAY, 16);
            }
        },
        EASTER_MONDAY_2017 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.APRIL, 17);
            }
        },
        ASCENSION_DAY_2017 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MAY, 25);
            }
        },
        PENTECOST_MONDAY_2017 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.JUNE, 5);
            }
        },
        EASTER_MONDAY_2018 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.APRIL, 2);
            }
        },
        ASCENSION_DAY_2018 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MAY, 10);
            }
        },
        PENTECOST_MONDAY_2018 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MAY, 21);
            }
        },
        EASTER_MONDAY_2019 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.APRIL, 22);
            }
        },
        ASCENSION_DAY_2019 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MAY, 30);
            }
        },
        PENTECOST_MONDAY_2019 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.JUNE, 10);
            }
        },
        EASTER_MONDAY_2020 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.APRIL, 13);
            }
        },
        ASCENSION_DAY_2020 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.MAY, 21);
            }
        },
        PENTECOST_MONDAY_2020 {
            @Override
            MonthDay monthDay() {
                return MonthDay.of(Month.JUNE, 1);
            }
        };

        public void addTo(CalendarService.CalendarBuilder builder) {
            builder.except(HOLIDAY_DAYTYPE_NAME).occursAlwaysOn(this.monthDay()).add();
        }

        abstract MonthDay monthDay();
    }

}