/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.exception;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ExceptionCatcher {

    private final List<Runnable> runnables;
    private Consumer<Exception> exceptionHandler;

    public ExceptionCatcher(List<Runnable> runnables) {
        this.runnables = ImmutableList.copyOf(runnables);
    }

    public static <F> ExceptionCatcher.Intermediate executing(Runnable... runnables) {
        return new ExceptionCatcher(Arrays.asList(runnables)).intermediate();
    }

    private Intermediate intermediate() {
        return new Intermediate();
    }

    public void execute() {
        runnables.stream()
                .forEach(c -> {
                    try {
                        c.run();
                    } catch (Exception e) {
                        exceptionHandler.accept(e);
                    }
                });
    }

    public class Intermediate {

        public ExceptionCatcher andHandleExceptionsWith(Consumer<Exception> handler) {
            exceptionHandler = handler;
            return ExceptionCatcher.this;
        }
    }

}
