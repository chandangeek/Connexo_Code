package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private static final int RETRY_DELAY = 60;
    private final DataModel dataModel;
    private final MessageService messageService;
    private final Thesaurus thesaurus;
    private final UserService userService;
    private final UsagePointGroupPrivilegesProvider usagePointGroupPrivilegesProvider;

    @Inject
    public Installer(DataModel dataModel, UserService userService, MessageService messageService,
                     Thesaurus thesaurus, UsagePointGroupPrivilegesProvider usagePointGroupPrivilegesProvider) {
        this.dataModel = dataModel;
        this.userService = userService;
        this.usagePointGroupPrivilegesProvider = usagePointGroupPrivilegesProvider;
        this.messageService = messageService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Add module privileges",
                () -> userService.addModulePrivileges(usagePointGroupPrivilegesProvider),
                logger
        );
        doTry(
                "Create itemizer Queue and subscriber.",
                () -> {
                    DestinationSpec itemizerDestination = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")
                            .orElseThrow(() -> new IllegalStateException("Queue table spec MSG_RAWQUEUETABLE does not exist."))
                            .createDestinationSpec(UsagePointDataModelService.BULK_ITEMIZER_QUEUE_DESTINATION, RETRY_DELAY);
                    itemizerDestination.activate();
                    itemizerDestination.subscribe(Subscribers.BULK_ITEMIZER, UsagePointDataModelService.COMPONENT_NAME, Layer.DOMAIN);
                },
                logger
        );
        doTry(
                "Create bulk handling Queue and subscriber.",
                () -> {
                    DestinationSpec handlingDestination = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE")
                            .orElseThrow(() -> new IllegalStateException("Queue table spec MSG_RAWQUEUETABLE does not exist."))
                            .createDestinationSpec(UsagePointDataModelService.BULK_HANDLING_QUEUE_DESTINATION, RETRY_DELAY);
                    handlingDestination.activate();
                    handlingDestination.subscribe(Subscribers.BULK_HANDLER, UsagePointDataModelService.COMPONENT_NAME, Layer.DOMAIN);
                },
                logger
        );
    }
}
