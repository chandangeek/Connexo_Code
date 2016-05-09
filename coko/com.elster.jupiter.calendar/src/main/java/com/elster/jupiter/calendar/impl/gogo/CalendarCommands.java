package com.elster.jupiter.calendar.impl.gogo;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.TimeZone;

@Component(name = "com.elster.jupiter.calendar.impl.gogo", service = CalendarCommands.class,
        property = {"osgi.command.scope=cal",
                "osgi.command.function=createCalendar"
        }, immediate = true)
public class CalendarCommands {
    private volatile CalendarService calendarService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public void createCalendar() {
        System.out.println("Usage: createCalendar <name>");
    }

    public void createCalendar(String name) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            calendarService.newCalendar(name, TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
                    .endYear(Year.of(2020))
                    .description("Description remains to be completed :-)")
                    .mRID(name)
                    .addEvent("On peak", 3)
                    .addEvent("Off peak", 5)
                    .addEvent("Demand response", 97)
                    .newDayType("Summer weekday")
                        .event("Off peak").startsFrom(LocalTime.of(0,0,0))
                        .eventWithCode(3).startsFrom(LocalTime.of(13, 0, 0))
                        .event("Off peak").startsFrom(LocalTime.of(20, 0, 0))
                        .add()
                    .newDayType("Weekend")
                        .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                        .add()
                    .newDayType("Holiday")
                        .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                        .add()
                    .newDayType("Winter day")
                        .event("Off peak").startsFrom(LocalTime.of(0,0,0))
                        .event("On peak").startsFrom(LocalTime.of(5, 0, 0))
                        .event("Off peak").startsFrom(LocalTime.of(21, 0, 0))
                        .add()
                    .newDayType("Demand response")
                        .eventWithCode(97).startsFrom(LocalTime.MIDNIGHT)
                        .add()
                    .addPeriod("Summer", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Weekend", "Weekend")
                    .addPeriod("Winter", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day")
                    .on(MonthDay.of(5, 1)).transitionTo("Summer")
                    .on(MonthDay.of(11, 1)).transitionTo("Winter")
                    .except("Holiday")
                        .occursOnceOn(LocalDate.of(2016, 1, 18))
                        .occursOnceOn(LocalDate.of(2016, 2, 15))
                        .occursOnceOn(LocalDate.of(2016, 5, 30))
                        .occursAlwaysOn(MonthDay.of(7, 4))
                        .occursOnceOn(LocalDate.of(2016, 9, 5))
                        .occursOnceOn(LocalDate.of(2016, 10, 10))
                        .occursAlwaysOn(MonthDay.of(11, 11))
                        .occursOnceOn(LocalDate.of(2016, 11, 24))
                        .occursAlwaysOn(MonthDay.of(12, 25))
                        .occursAlwaysOn(MonthDay.of(12, 26))
                        .add()
                    .add();
            context.commit();
        }
    }

}
