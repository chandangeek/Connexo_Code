package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private static final int RETRY_DELAY = 60;
    private final DataModel dataModel;
    private final MessageService messageService;
    private final Thesaurus thesaurus;

    @Inject
    public Installer(DataModel dataModel, MessageService messageService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create itemizer Queue and subscriber.",
                () -> {
                    DestinationSpec itemizerDestination = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")
                            .orElseThrow(() -> new IllegalStateException("Queue table spec MSG_RAWQUEUETABLE does not exist."))
                            .createDestinationSpec(UsagePointDataService.BULK_ITEMIZER_QUEUE_DESTINATION, RETRY_DELAY);
                    itemizerDestination.activate();
                    itemizerDestination.subscribe(Subscribers.BULK_ITEMIZER, UsagePointDataService.COMPONENT_NAME, Layer.DOMAIN);
                },
                logger
        );
        doTry(
                "Create bulk handling Queue and subscriber.",
                () -> {
                    DestinationSpec handlingDestination = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")
                            .orElseThrow(() -> new IllegalStateException("Queue table spec MSG_RAWQUEUETABLE does not exist."))
                            .createDestinationSpec(UsagePointDataService.BULK_HANDLING_QUEUE_DESTINATION, RETRY_DELAY);
                    handlingDestination.activate();
                    handlingDestination.subscribe(Subscribers.BULK_HANDLER, UsagePointDataService.COMPONENT_NAME, Layer.DOMAIN);
                },
                logger
        );
    }
}
