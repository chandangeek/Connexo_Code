/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskService;

import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.sap.SearchDataSourceHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + SearchDataSourceHandlerFactory.SEARCH_DATA_SOURCE_TASK_SUBSCRIBER,
                "destination=" + SearchDataSourceHandlerFactory.SEARCH_DATA_SOURCE_TASK_DESTINATION},
        immediate = true)
public class SearchDataSourceHandlerFactory implements MessageHandlerFactory {
    public static final String SEARCH_DATA_SOURCE_TASK_DESTINATION = "SearchDataSourceTopic";
    public static final String SEARCH_DATA_SOURCE_TASK_SUBSCRIBER = "SearchDataSourceSubscriber";
    public static final String SEARCH_DATA_SOURCE_TASK_DISPLAYNAME = "Handle search data sources by SAP id's";

    private volatile TaskService taskService;
    private volatile ServiceCallService serviceCallService;
    private volatile WebServiceActivator webServiceActivator;

    public SearchDataSourceHandlerFactory() {
    }

    @Inject
    public SearchDataSourceHandlerFactory(TaskService taskService,
                                          ServiceCallService serviceCallService) {
        setTaskService(taskService);
        setServiceCallService(serviceCallService);
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new SearchDataSourceHandler(serviceCallService, webServiceActivator));
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public final void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }
}
