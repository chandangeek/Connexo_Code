package com.elster.jupiter.calendar.impl.gogo;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;
import java.util.TimeZone;

@Component(name = "com.elster.jupiter.calendar.impl.gogo", service = CalendarCommands.class,
        property = {"osgi.command.scope=cal",
                    "osgi.command.function=help",
                    "osgi.command.function=createCalendar",
                    "osgi.command.function=deleteCalendar",
                    "osgi.command.function=createEiserverActivityCalendar"
        }, immediate = true)
@SuppressWarnings("unused")
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

    @Activate
    public void activate() {
        System.out.println("Calendar gogo commands are ready for business");
        this.help();
    }

    public void help() {
        this.createCalendar();
        this.deleteCalendar();
        this.createEiserverActivityCalendar();
    }

    public void deleteCalendar() {
        System.out.println("Usage: deleteCalendar <id> | <name>");
    }

    public void deleteCalendar(long id) {
        Optional<Calendar> obsolete = this.calendarService.findCalendar(id);
        if (obsolete.isPresent()) {
            this.deleteCalendar(obsolete.get());
        } else {
            System.out.println("Calendar with id " + id + " not found!");
        }
    }

    public void deleteCalendar(String name) {
        Optional<Calendar> obsolete = this.calendarService.findCalendarByName(name);
        if (obsolete.isPresent()) {
            if (this.calendarService.isCalendarInUse(obsolete.get())) {
                System.out.println("Calendar with name " + name + " is in use!");
            } else {
                this.deleteCalendar(obsolete.get());
            }
        } else {
            System.out.println("Calendar with name " + name + " not found!");
        }
    }

    private void deleteCalendar(Calendar obsolete) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            obsolete.delete();
            context.commit();
        }
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

    public void createEiserverActivityCalendar() {
        System.out.println("Usage: createEiserverActivityCalendar <name>");
        System.out.println("       and creates a calendar that conforms to a typical old style eiServer CodeTable, as expected by legacy protocols");
    }

    public void createEiserverActivityCalendar(String name) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            calendarService.newCalendar(name, TimeZone.getTimeZone("Europe/Brussels"), Year.of(2009))
                    .description("Conforms to typical old style eiServer Code, as expected by legacy protocols")
                    .addEvent("One", 1)
                    .addEvent("Three", 3)
                    .addEvent("Four", 4)
                    .newDayType("Day1")
                        .eventWithCode(4).startsFrom(LocalTime.of(7, 0, 0))
                        .eventWithCode(3).startsFrom(LocalTime.of(18, 0, 0))
                        .eventWithCode(1).startsFrom(LocalTime.of(22, 0, 0))
                        .add()
                    .newDayType("Day2")
                        .eventWithCode(1).startsFrom(LocalTime.MIDNIGHT)
                        .add()
                    .newDayType("Day3")
                        .eventWithCode(3).startsFrom(LocalTime.MIDNIGHT)
                        .add()
                    .newDayType("Day4")
                        .eventWithCode(4).startsFrom(LocalTime.MIDNIGHT)
                        .add()
                    .addPeriod("January", "Day1", "Day1", "Day1", "Day1", "Day1", "Day4", "Day4")
                    .addPeriod("Winter", "Day1", "Day1", "Day1", "Day1", "Day1", "Day4", "Day1")
                    .addPeriod("July", "Day2", "Day2", "Day3", "Day3", "Day4", "Day1", "Day1")
                    .addPeriod("Summer", "Day1", "Day2", "Day3", "Day3", "Day4", "Day1", "Day1")
                    .on(MonthDay.of(1, 1)).transitionTo("January")
                    .on(MonthDay.of(2, 1)).transitionTo("Winter")
                    .on(MonthDay.of(7, 1)).transitionTo("July")
                    .on(MonthDay.of(8, 1)).transitionTo("Summer")
                    .except("Day4")
                        .occursAlwaysOn(MonthDay.of(5, 1))
                        .occursAlwaysOn(MonthDay.of(11, 1))
                        .occursAlwaysOn(MonthDay.of(12, 25))
                        .add()
                    .except("Day3")
                        .occursOnceOn(LocalDate.of(2009, 4, 12))
                        .occursOnceOn(LocalDate.of(2010, 4, 4))
                        .add()
                    .add();
            context.commit();
        }
    }

}
