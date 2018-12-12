/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.security.thread.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

import java.security.Principal;
import java.util.Locale;

public class RunnableWithContext implements Runnable {

    private final Runnable decorated;
    private final ThreadPrincipalService threadPrincipalService;
    private final Principal principal;
    private final String module;
    private final String action;
    private final Locale locale;

    public RunnableWithContext(Runnable runnable, ThreadPrincipalService threadPrincipalService, Principal principal) {
        this(runnable, threadPrincipalService, principal, null, null, null);
    }

    public RunnableWithContext(Runnable runnable, ThreadPrincipalService threadPrincipalService, Principal principal, String module, String action, Locale locale) {
        this.decorated = runnable;
        this.threadPrincipalService = threadPrincipalService;
        this.principal = principal;
        this.module = module;
        this.action = action;
        this.locale = locale;
    }

    @Override
    public void run() {
        threadPrincipalService.set(principal, module, action, locale);
        try {
            decorated.run();
        } finally {
            threadPrincipalService.clear();
        }

    }
}
