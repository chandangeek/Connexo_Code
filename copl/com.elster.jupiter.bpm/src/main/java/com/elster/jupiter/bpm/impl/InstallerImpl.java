package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.users.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerImpl {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    public void install(MessageService messageService, AppService appService, UserService userService) {
        createPrivileges(userService);
        assignPrivilegesToDefaultRoles(userService);
        createBPMQueue(messageService);
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

    private void createPrivileges(UserService userService) {
        userService.createResourceWithPrivileges("SYS", "bpm.businessProcesses", "bpm.businessProcesses.description", new String[] {Privileges.VIEW_BPM});
    }

    private void assignPrivilegesToDefaultRoles(UserService userService) {
        userService.grantGroupWithPrivilege(userService.DEFAULT_ADMIN_ROLE, new String[] {Privileges.VIEW_BPM});
        userService.grantGroupWithPrivilege(userService.DEFAULT_METER_EXPERT_ROLE, new String[] {Privileges.VIEW_BPM});
        userService.grantGroupWithPrivilege(userService.DEFAULT_METER_OPERATOR_ROLE, new String[] {Privileges.VIEW_BPM});
    }

}
