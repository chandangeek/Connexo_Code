/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.users.UserService;

import com.google.inject.Inject;


public class CreatePowerUserCommand  extends CommandWithTransaction{
    private final UserService userService;

    @Inject
    public CreatePowerUserCommand(UserService userService) {
        this.userService = userService;
    }

    public void run(){
        getPrincipal();
    }
}
