/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * When a web service (EndPointProvider) is registered on the dashboard (WebServiceServiceImpl), the AppServer is notified
 */
@Component(name = "com.elster.jupiter.webservices.registration.eventhandler", service = TopicHandler.class, immediate = true)
public class WebServiceRegistrationTopicHandler implements TopicHandler {

    private volatile AppService appService;
    private volatile WebServicesService webServicesService;

    public WebServiceRegistrationTopicHandler() {
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        String webServiceName = (String) localEvent.getSource();
        appService.getAppServer()
                .filter(AppServer::isActive)
                .ifPresent(as -> as.supportedEndPoints()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(epc -> epc.getWebServiceName().equals(webServiceName))
                        .filter(epc -> !webServicesService.isPublished(epc))
                .forEach(webServicesService::publishEndPoint));
    }

    @Override
    public String getTopicMatcher() {
        return EventType.WEBSERVICE_REGISTERED.topic();
    }
}
