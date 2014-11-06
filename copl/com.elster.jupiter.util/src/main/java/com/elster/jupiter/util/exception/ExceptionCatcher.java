package com.elster.jupiter.util.exception;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ExceptionCatcher {

    private final List<Runnable> runnables;
    private Consumer<Exception> exceptionHandler;

    public ExceptionCatcher(List<Runnable> runnables) {
        this.runnables = runnables;
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
