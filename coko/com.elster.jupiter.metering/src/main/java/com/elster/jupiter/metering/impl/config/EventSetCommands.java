/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.stream.Stream;

@Component(
        name = "com.elster.jupiter.usagepoint.calendar",
        service = {EventSetCommands.class},
        property = {
                "osgi.command.scope=mce",   // mce is short for metering.config.eventsets
                "osgi.command.function=createEventSet",
                "osgi.command.function=linkEventSet",
                "osgi.command.function=listLinkEventSets",
                "osgi.command.function=listAvailableEventSets",
                "osgi.command.function=unlinkEventSet"},
        immediate = true)
public class EventSetCommands {

    private volatile CalendarService calendarService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile TransactionService transactionService;

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @SuppressWarnings("unused")
    public void createEventSet(String name) {
        System.out.println("Usage: createEventSet <name> [<event name> <event code>]{1,5}");
    }

    @SuppressWarnings("unused")
    public void createEventSet(String name, String event1Name, long event1Code) {
        this.createEventSet(name, new EventSpecification(event1Name, event1Code));
    }

    @SuppressWarnings("unused")
    public void createEventSet(String name, String event1Name, long event1Code, String event2Name, long event2Code) {
        this.createEventSet(
                name,
                new EventSpecification(event1Name, event1Code),
                new EventSpecification(event2Name, event2Code));
    }

    @SuppressWarnings("unused")
    public void createEventSet(String name, String event1Name, long event1Code, String event2Name, long event2Code, String event3Name, long event3Code) {
        this.createEventSet(
                name,
                new EventSpecification(event1Name, event1Code),
                new EventSpecification(event2Name, event2Code),
                new EventSpecification(event3Name, event3Code));
    }

    @SuppressWarnings("unused")
    public void createEventSet(String name, String event1Name, long event1Code, String event2Name, long event2Code, String event3Name, long event3Code, String event4Name, long event4Code) {
        this.createEventSet(
                name,
                new EventSpecification(event1Name, event1Code),
                new EventSpecification(event2Name, event2Code),
                new EventSpecification(event3Name, event3Code),
                new EventSpecification(event4Name, event4Code));
    }

    @SuppressWarnings("unused")
    public void createEventSet(String name, String event1Name, long event1Code, String event2Name, long event2Code, String event3Name, long event3Code, String event4Name, long event4Code, String event5Name, long event5Code) {
        this.createEventSet(
                name,
                new EventSpecification(event1Name, event1Code),
                new EventSpecification(event2Name, event2Code),
                new EventSpecification(event3Name, event3Code),
                new EventSpecification(event4Name, event4Code),
                new EventSpecification(event5Name, event5Code));
    }

    private void createEventSet(String name, EventSpecification... eventSpecifications) {
        transactionService.builder()
                .principal(() -> "Console")
                .run(() -> this.doCreateEventSet(name, eventSpecifications));
    }

    private void doCreateEventSet(String name, EventSpecification... eventSpecifications) {
        CalendarService.EventSetBuilder builder = calendarService.newEventSet(name);
        Stream.of(eventSpecifications).forEach(each -> each.addTo(builder));
        EventSet eventSet = builder.add();
        System.out.println("EventSet created with id " + eventSet.getId());
    }

    @SuppressWarnings("unused")
    public void linkEventSet() {
        System.out.println("Usage: linkEventSet <metrology configuration id> <event set id>");
    }

    @SuppressWarnings("unused")
    public void linkEventSet(long metrologyConfigurationId, long eventSetId) {
        transactionService.builder()
                .principal(() -> "Console")
                .run(() -> {
                    MetrologyConfiguration metrologyConfiguration = getMetrologyConfig(metrologyConfigurationId);
                    EventSet eventSet = getEventSet(eventSetId);
                    metrologyConfiguration.addEventSet(eventSet);
                });
    }

    EventSet getEventSet(long eventSetId) {
        return calendarService.findEventSet(eventSetId)
                .orElseThrow(() -> new IllegalArgumentException("No event set with id " + eventSetId));
    }

    MetrologyConfiguration getMetrologyConfig(long metrologyConfigurationId) {
        return metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigurationId)
                .orElseThrow(() -> new IllegalArgumentException("No metrology configuration with id " + metrologyConfigurationId));
    }

    @SuppressWarnings("unused")
    public void listLinkEventSets(long metrologyConfigurationId) {
        MetrologyConfiguration metrologyConfig = getMetrologyConfig(metrologyConfigurationId);
        metrologyConfig
                .getEventSets()
                .stream()
                .map(this::toPrintString)
                .forEach(System.out::println);
    }

    String toPrintString(EventSet eventSet) {
        return eventSet.getId() + " " + eventSet.getName();
    }

    @SuppressWarnings("unused")
    public void listAvailableEventSets() {
        calendarService.findEventSets()
                .stream()
                .map(this::toPrintString)
                .forEach(System.out::println);
    }

    @SuppressWarnings("unused")
    public void unlinkEventSet(long metrologyConfigurationId, long eventSetId) {
        transactionService.builder()
                .principal(() -> "Console")
                .run(() -> {
                    MetrologyConfiguration metrologyConfiguration = getMetrologyConfig(metrologyConfigurationId);
                    EventSet eventSet = getEventSet(eventSetId);
                    metrologyConfiguration.removeEventSet(eventSet);
                });
    }

    private static class EventSpecification {
        private final String name;
        private final long code;

        EventSpecification(String name, long code) {
            this.name = name;
            this.code = code;
        }

        void addTo(CalendarService.EventSetBuilder eventSetBuilder) {
            eventSetBuilder.addEvent(this.name).withCode(this.code);
        }

    }
}