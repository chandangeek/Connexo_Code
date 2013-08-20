package com.elster.jupiter.security.thread;

import java.security.Principal;

/**
 * Runnable decorator that sets up, and clears a Principal around the decorated Runnable.
 */
public final class RunAs implements Runnable {

    private final Principal principal;
    private String module;
    private String action;
    private final Runnable runnable;
    private final ThreadPrincipalService threadPrincipalService;

    /**
     * @param threadPrincipalService the threadPrincipalService to register the Principal
     * @param principal the principal
     * @param runnable the runnable to decorate
     */
    public RunAs(ThreadPrincipalService threadPrincipalService, Principal principal, Runnable runnable) {
        this.threadPrincipalService = threadPrincipalService;
        this.principal = principal;
        this.runnable = runnable;
    }

    /**
     * @param module the module String to set
     * @return this
     */
    public RunAs module(String module) {
        this.module = module;
        return this;
    }

    /**
     * @param action the action to set
     * @return this
     */
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
