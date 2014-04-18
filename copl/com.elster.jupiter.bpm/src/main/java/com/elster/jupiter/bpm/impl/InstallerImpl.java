package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.google.common.base.Optional;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerImpl {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    private final DataModel dataModel;


    public InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install(MessageService messageService) {
        createBPMQueue(messageService);
	}

    private void createBPMQueue(MessageService messageService) {
        //TODO: implement the queue
    }

}
