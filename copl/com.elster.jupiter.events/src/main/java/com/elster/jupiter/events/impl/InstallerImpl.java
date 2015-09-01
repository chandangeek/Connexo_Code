package com.elster.jupiter.events.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;

import static com.elster.jupiter.events.EventService.JUPITER_EVENTS;

public class InstallerImpl {

    private static final int RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final MessageService messageService;

    public InstallerImpl(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    public void install() {
        try {
            dataModel.install(true, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DestinationSpec destinationSpec = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get().createDestinationSpec(JUPITER_EVENTS, RETRY_DELAY);
        destinationSpec.activate();
    }
}
