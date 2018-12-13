/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.gogo;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Hashtable;

@Component(service = TopicHandlerCommands.class, property = {"osgi.command.scope=topic", "osgi.command.function=listenTo"}, immediate = true)
public class TopicHandlerCommands {

    private BundleContext context;
    private ServiceRegistration<TopicHandler> topicHandlerServiceRegistration;

    @Activate
    public void activate(BundleContext bundleContext) {
       this.context = bundleContext;
    }

    public void listenTo(String topic) {
        ListenToTopicHandler listenToTopicHandler = new ListenToTopicHandler(topic);
        if(topicHandlerServiceRegistration != null) {
            topicHandlerServiceRegistration.unregister();
        }
        topicHandlerServiceRegistration = context.registerService(TopicHandler.class, listenToTopicHandler, new Hashtable<>());
        System.out.println("listening to topic " + topic);
    }

}