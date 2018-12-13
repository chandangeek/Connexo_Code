/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides factory services for {@link CalendarTimeSeriesExtenderHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-24 (09:53)
 */
@Component(name="com.elster.jupiter.calendar.extender.factory", service = MessageHandlerFactory.class, property = {"subscriber=" + CalendarTimeSeriesExtenderHandlerFactory.TASK_SUBSCRIBER, "destination=" + CalendarTimeSeriesExtenderHandlerFactory.TASK_DESTINATION}, immediate = true)
public class CalendarTimeSeriesExtenderHandlerFactory implements MessageHandlerFactory {

    public static final String TASK_DESTINATION = "CALTimeSeriesExtQ";
    public static final String TASK_SUBSCRIBER = "CALTimeSeriesExtender";
    public static final String TASK_NAME = "Calendar timeseries extender";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Extend the definition of a calendar with one year";

    private volatile TransactionService transactionService;
    private volatile ServerCalendarService calendarService;
    private volatile TaskService taskService;

    // For OSGi framework only
    public CalendarTimeSeriesExtenderHandlerFactory() {
        super();
    }

    // For testing purposes
    @Inject
    public CalendarTimeSeriesExtenderHandlerFactory(TransactionService transactionService, ServerCalendarService calendarService, TaskService taskService) {
        this.setTransactionService(transactionService);
        this.setCalendarService(calendarService);
        this.setTaskService(taskService);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setCalendarService(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(new CalendarTimeSeriesExtenderHandler(transactionService, this.calendarService));
    }

}