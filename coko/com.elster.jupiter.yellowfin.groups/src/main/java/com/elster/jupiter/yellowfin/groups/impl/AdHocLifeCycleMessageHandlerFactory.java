/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;


@Component(name = "com.elster.jupiter.yellowfin.groups.impl",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST, "destination="+ YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST},
        immediate = true)
public class AdHocLifeCycleMessageHandlerFactory implements MessageHandlerFactory {

    private volatile YellowfinGroupsService yellowfinGroupsService;

    @Override
    public MessageHandler newMessageHandler() {
        return new AdHocLifeCycleMessageHandler(yellowfinGroupsService);
    }

    @Reference
    public void setDataExportService(YellowfinGroupsService yellowfinGroupsService) {
        this.yellowfinGroupsService = (YellowfinGroupsService) yellowfinGroupsService;
    }

    @Activate
    public void activate(BundleContext context) {
    }

    @Deactivate
    public void deactivate() {

    }

}
