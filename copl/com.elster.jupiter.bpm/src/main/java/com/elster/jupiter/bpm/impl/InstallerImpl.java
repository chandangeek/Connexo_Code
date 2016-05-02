package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

//import com.elster.jupiter.bpm.security.Privileges;

class InstallerImpl implements FullInstaller {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());
    private static final String BPM_DESIGNER_ROLE = "Business process designer";
    private static final String BPM_DESIGNER_ROLE_DESCRIPTION = "Business process designer privilege";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final UserService userService;

    @Inject
    InstallerImpl(DataModel dataModel, MessageService messageService, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        createBPMQueue(messageService);
        ExceptionCatcher.executing(
                () -> dataModelUpgrader.upgrade(dataModel, Version.latest()),
                this::createDefaultRoles
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void createBPMQueue(MessageService messageService) {
        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(BpmService.BPM_QUEUE_DEST, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
            destinationSpec.subscribe(BpmService.BPM_QUEUE_SUBSC);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void createDefaultRoles() {
        try {
            Group group = userService.createGroup(BPM_DESIGNER_ROLE, BPM_DESIGNER_ROLE_DESCRIPTION);
            userService.grantGroupWithPrivilege(group.getName(), BpmService.COMPONENTNAME, new String[] {"privilege.design.bpm"});
            //TODO: workaround: attached Bpm designer to user admin !!! to remove this line when the user can be created/added to system
            userService.getUser(1).ifPresent(u -> u.join(group));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}
