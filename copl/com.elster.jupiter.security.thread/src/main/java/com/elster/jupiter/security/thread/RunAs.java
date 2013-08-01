package com.elster.jupiter.security.thread;

import java.security.Principal;

/**
 * Runnable wrapper that sets up a Principal
 */
public final class RunAs implements Runnable {

    private final Principal principal;
    private String module;
    private String action;
    private final Runnable runnable;
    private final ThreadPrincipalService threadPrincipalService;

    public RunAs(ThreadPrincipalService threadPrincipalService, Principal principal, Runnable runnable) {
        this.threadPrincipalService = threadPrincipalService;
        this.principal = principal;
        this.runnable = runnable;
    }

    public RunAs module(String module) {
        this.module = module;
        return this;
    }

    public RunAs action(String action) {
        this.action = action;
        return this;
    }

    @Override
    public void run() {
        threadPrincipalService.set(principal);
        threadPrincipalService.set(module, action);
        try {
            runnable.run();
        } finally {
            threadPrincipalService.clear();
        }
    }
}
