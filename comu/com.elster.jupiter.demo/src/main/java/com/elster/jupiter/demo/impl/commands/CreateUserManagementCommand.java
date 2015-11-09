package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.UserTpl;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public class CreateUserManagementCommand {
    private final UserService userService;

    @Inject
    public CreateUserManagementCommand(UserService userService) {
        this.userService = userService;
    }

    public void run(){
        createUsers();
        updateDefaultUsers();
    }

    private void createUsers(){
        for (UserTpl userTpl : UserTpl.values()) {
            Builders.from(userTpl).get();
        }
    }

    private void updateDefaultUsers(){
        User admin = userService.findUser("admin").get();
        admin.setPassword("D3moAdmin");
        admin.update();
    }
}
