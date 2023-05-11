/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.lang.reflect.Field;

public class AbstractEndPointInitializer {
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private final Thesaurus thesaurus;
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServicesService webServicesService;
    private final WebServiceCallOccurrenceService webServiceCallOccurrenceService;
    private final EventService eventService;

    @Inject
    public AbstractEndPointInitializer(TransactionService transactionService,
                                ThreadPrincipalService threadPrincipalService,
                                UserService userService,
                                Thesaurus thesaurus,
                                EndPointConfigurationService endPointConfigurationService,
                                WebServicesService webServicesService,
                                WebServiceCallOccurrenceService webServiceCallOccurrenceService,
                                EventService eventService) {
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.thesaurus = thesaurus;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
        this.webServiceCallOccurrenceService = webServiceCallOccurrenceService;
        this.eventService = eventService;
    }

    public <T> T initializeInboundEndPoint(T endPoint, InboundEndPointConfiguration endPointConfiguration) {
        if (endPoint instanceof AbstractInboundEndPoint) {
            inject(AbstractInboundEndPoint.class, endPoint, "transactionService", transactionService);
            inject(AbstractInboundEndPoint.class, endPoint, "threadPrincipalService", threadPrincipalService);
            inject(AbstractInboundEndPoint.class, endPoint, "userService", userService);
            inject(AbstractInboundEndPoint.class, endPoint, "endPointConfiguration", endPointConfiguration);
            inject(AbstractInboundEndPoint.class, endPoint, "webServicesService", webServicesService);
            inject(AbstractInboundEndPoint.class, endPoint, "webServiceCallOccurrenceService", webServiceCallOccurrenceService);
        }
        return endPoint;
    }

    public <T extends EndPointProvider> T initializeOutboundEndPointProvider(T endPointProvider) {
        if (endPointProvider instanceof AbstractOutboundEndPointProvider) {
            inject(AbstractOutboundEndPointProvider.class, endPointProvider, "thesaurus", thesaurus);
            inject(AbstractOutboundEndPointProvider.class, endPointProvider, "endPointConfigurationService", endPointConfigurationService);
            inject(AbstractOutboundEndPointProvider.class, endPointProvider, "webServicesService", webServicesService);
            inject(AbstractOutboundEndPointProvider.class, endPointProvider, "webServiceCallOccurrenceService", webServiceCallOccurrenceService);
            inject(AbstractOutboundEndPointProvider.class, endPointProvider, "eventService", eventService);
        }
        return endPointProvider;
    }

    private static void inject(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object get(Class<?> clazz, Object instance, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
