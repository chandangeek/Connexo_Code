/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.tasks.TaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides factory services for {@link CalendarTimeSeriesCacheHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-13 (13:21)
 */
@Component(name="com.elster.jupiter.metering.calendar.cache.factory", service = MessageHandlerFactory.class, property = {"subscriber=" + CalendarTimeSeriesCacheHandlerFactory.TASK_SUBSCRIBER, "destination=" + CalendarTimeSeriesCacheHandlerFactory.TASK_DESTINATION}, immediate = true)
public class CalendarTimeSeriesCacheHandlerFactory implements MessageHandlerFactory {

    public static final String TASK_DESTINATION = "CALTimeSeriesCacheQ";
    public static final String TASK_SUBSCRIBER = "CALTimeSeriesCacheHandler";
    public static final String TASK_NAME = "Calendar timeseries cache handler";
    public static final String TASK_SUBSCRIBER_DISPLAYNAME = "Caches the definition of a calendar for every interval that is likely to be used when applied to a usagepoint";

    private volatile TaskService taskService;
    private volatile MeteringService meteringService;
    private volatile ServerDataAggregationService dataAggregationService;

    // For OSGi purposes
    public CalendarTimeSeriesCacheHandlerFactory() {
        super();
    }

    // For testing purposes
    @Inject
    public CalendarTimeSeriesCacheHandlerFactory(TaskService taskService, MeteringService meteringService, ServerDataAggregationService dataAggregationService) {
        this();
        this.setTaskService(taskService);
        this.setMeteringService(meteringService);
        this.setDataAggregationService(dataAggregationService);
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setDataAggregationService(ServerDataAggregationService dataAggregationService) {
        this.dataAggregationService = dataAggregationService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return this.taskService.createMessageHandler(new CalendarTimeSeriesCacheHandler(meteringService, this.dataAggregationService));
    }

}