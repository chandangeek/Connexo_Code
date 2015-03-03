package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.Privileges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes the necessary steps to install the technical components of the finate state machine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:57)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final UserService userService;

    public Installer(DataModel dataModel, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.createPrivileges();
    }

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges(
                "FSM",
                "finateStateMachineAdministration.finateStateMachineAdministrations",
                "finateStateMachineAdministration.finateStateMachineAdministrations.description",
                new String[]{
                        Privileges.CONFIGURE_FINATE_STATE_MACHINES,
                        Privileges.VIEW_FINATE_STATE_MACHINES});
    }

}