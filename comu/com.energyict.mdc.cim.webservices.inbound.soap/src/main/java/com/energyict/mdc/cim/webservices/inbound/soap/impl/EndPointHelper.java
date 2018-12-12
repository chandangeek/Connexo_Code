/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.security.Principal;

public class EndPointHelper {

    private static final String DEFAULT_USER_NAME = "MDC inbound webservice";

    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private final TransactionService transactionService;

    @Inject
    public EndPointHelper(ThreadPrincipalService threadPrincipalService,
                          UserService userService,
                          TransactionService transactionService) {
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    public void setSecurityContext() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (principal == null) {
            threadPrincipalService.set(() -> DEFAULT_USER_NAME);
        }
        if (!(principal instanceof User)) {
            try (TransactionContext context = transactionService.getContext()) {
                User user = userService.findUser(DEFAULT_USER_NAME, userService.getRealm())
                        .orElseGet(() -> userService.createUser(DEFAULT_USER_NAME, DEFAULT_USER_NAME));
                threadPrincipalService.set(user);
                context.commit();
            }
        }
    }
}