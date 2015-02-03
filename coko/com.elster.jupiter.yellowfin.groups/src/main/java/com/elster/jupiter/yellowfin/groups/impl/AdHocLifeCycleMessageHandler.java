package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;


public class AdHocLifeCycleMessageHandler implements MessageHandler{

    private final YellowfinGroupsService yellowfinGroupsService;

    public AdHocLifeCycleMessageHandler(YellowfinGroupsService yellowfinGroupsService) {
        this.yellowfinGroupsService = (YellowfinGroupsService) yellowfinGroupsService;
    }

    @Override
    public void process(Message message) {
        yellowfinGroupsService.purgeAdHocSearch();
    }
}

