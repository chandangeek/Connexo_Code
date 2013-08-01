package com.elster.jupiter.appserver.impl;

import java.util.concurrent.RunnableFuture;

public interface ProvidesCancellableFuture extends Runnable {

    <T> RunnableFuture<T> newTask(T result);
}
