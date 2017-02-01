package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceType;

import com.google.common.base.MoreObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.stream.Collectors;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-13 (12:59)
 */
@Component(name = "com.energyict.mdc.device.type.calendars",
        service = CalendarCommands.class,
        property = {
                "osgi.command.scope=mdc.device.calendar",
                "osgi.command.function=listCalendars",
                "osgi.command.function=addCalendar"},
        immediate = true)
@SuppressWarnings("unused")
public class CalendarCommands {
    private volatile ServerDeviceConfigurationService deviceConfigurationService;
    private volatile CalendarService calendarService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setDeviceConfigurationService(ServerDeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

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

    @SuppressWarnings("unused")
    public void listCalendars() {
        System.out.println("listCalendars <device type id>");
    }

    @SuppressWarnings("unused")
    public void listCalendars(long deviceTypeId) {
        DeviceType deviceType = this.findDeviceTypeOrThrowException(deviceTypeId);
        System.out.println(
                deviceType
                        .getAllowedCalendars()
                        .stream()
                        .map(this::toString)
                        .collect(Collectors.joining("\n")));
    }

    private DeviceType findDeviceTypeOrThrowException(long deviceTypeId) {
        return this.deviceConfigurationService
                    .findDeviceType(deviceTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Devicetype with id " + deviceTypeId + " does not exist"));
    }

    private String toString(AllowedCalendar allowedCalendar) {
        return MoreObjects.toStringHelper(allowedCalendar)
                .add("id", allowedCalendar.getId())
                .add("isGhost", allowedCalendar.isGhost())
                .add("name", allowedCalendar.getName())
                .toString();
    }

    @SuppressWarnings("unused")
    public void addCalendar() {
        System.out.println("addCalendar <device type id> <calendar id>");
    }

    @SuppressWarnings("unused")
    public void addCalendar(long deviceTypeId, long calendarId) {
        this.threadPrincipalService.set(() -> "CalendarCommands");
        DeviceType deviceType = this.findDeviceTypeOrThrowException(deviceTypeId);
        Calendar calendar = this.findCalendarOrThrowException(calendarId);
        try (TransactionContext context = this.transactionService.getContext()) {
            deviceType.addCalendar(calendar);
            context.commit();
        } finally {
            this.threadPrincipalService.clear();
        }
    }

    private Calendar findCalendarOrThrowException(long id) {
        return this.calendarService
                .findCalendar(id)
                .orElseThrow(() -> new IllegalArgumentException("Calendar with id " + id + " does not exist"));
    }

}