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

import javax.inject.Inject;

/**
 * When a web service (EndPointProvider) is registered on the dashboard (WebServiceServiceImpl), the AppServer is notified
 */
public class WebServiceRegistrationTopicHandler implements TopicHandler {

    private final AppService appService;
    private final WebServicesService webServicesService;

    @Inject
    public WebServiceRegistrationTopicHandler(AppService appService, WebServicesService webServicesService) {
        this.appService = appService;
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
