/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;

import java.security.Principal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        return new MyRunnableFuture<>(task.newTask(result));
    }

    @Override
    public void run() {
        threadPrincipalService.set(principal);
        try {
            task.run();
        } finally {
            threadPrincipalService.clear();
        }
    }

    private final class MyRunnableFuture<T> implements RunnableFuture<T> {

        private final RunnableFuture<T> runnableFuture;

        private MyRunnableFuture(RunnableFuture<T> runnableFuture) {
            this.runnableFuture = runnableFuture;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return runnableFuture.cancel(mayInterruptIfRunning);
        }

        @Override
        public void run() {
            threadPrincipalService.set(principal);
            try {
                runnableFuture.run();
            } finally {
                threadPrincipalService.clear();
            }
        }

        @Override
        public boolean isCancelled() {
            return runnableFuture.isCancelled();
        }

        @Override
        public boolean isDone() {
            return runnableFuture.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return runnableFuture.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return runnableFuture.get(timeout, unit);
        }
    }
}
