package com.elster.jupiter.events.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;

import static com.elster.jupiter.events.EventService.JUPITER_EVENTS;

public class InstallerImpl implements FullInstaller {

    private static final int RETRY_DELAY = 60;

    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    public InstallerImpl(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        DestinationSpec destinationSpec = getRawTopicTableSpec().createDestinationSpec(JUPITER_EVENTS, RETRY_DELAY);
        destinationSpec.activate();
    }

    private QueueTableSpec getRawTopicTableSpec() {
        return messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get();
    }
}
