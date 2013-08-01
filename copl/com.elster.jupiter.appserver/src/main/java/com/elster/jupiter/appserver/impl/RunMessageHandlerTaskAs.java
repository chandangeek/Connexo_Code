package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

import java.security.Principal;
import java.util.concurrent.RunnableFuture;

public class RunMessageHandlerTaskAs implements ProvidesCancellableFuture {

    private final MessageHandlerTask task;
    private final ThreadPrincipalService threadPrincipalService;
    private final Principal principal;

    public RunMessageHandlerTaskAs(MessageHandlerTask task, ThreadPrincipalService threadPrincipalService, Principal principal) {
        this.principal = principal;
        this.task = task;
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public <T> RunnableFuture<T> newTask(T result) {
        return task.newTask(result);
    }

    @Override
    public void run() {
        task.run();
    }
}
