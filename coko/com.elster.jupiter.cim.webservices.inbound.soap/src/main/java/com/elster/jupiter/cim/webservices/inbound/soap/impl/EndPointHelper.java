/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.security.Principal;

public class EndPointHelper {
    private static final String BATCH_EXECUTOR_USER_NAME = "batch executor";

    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;

    @Inject
    public EndPointHelper(ThreadPrincipalService threadPrincipalService,
                          UserService userService) {
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
    }

    public void setSecurityContext() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (principal == null) {
            userService.findUser(BATCH_EXECUTOR_USER_NAME, userService.getRealm()).ifPresent(user -> {
                threadPrincipalService.set(user);
            });
        }
    }
}
