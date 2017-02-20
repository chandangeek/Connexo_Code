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

@Component(
        name = "com.elster.jupiter.usagepoint.calendar",
        service = {EventSetCommands.class},
        property = {
                "osgi.command.scope=mce",   // mce is short for metering.config.eventsets
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

}