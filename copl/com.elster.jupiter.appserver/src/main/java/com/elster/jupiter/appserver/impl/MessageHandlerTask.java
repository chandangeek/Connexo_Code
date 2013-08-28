package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.VoidTransaction;

import java.sql.SQLException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class MessageHandlerTask implements ProvidesCancellableFuture {

    private final SubscriberSpec subscriberSpec;
    private final MessageHandler handler;
    private final MessageHandlerTask.ProcessTransaction processTransaction = new ProcessTransaction();

    MessageHandlerTask(SubscriberSpec subscriberSpec, MessageHandler handler) {
        this.subscriberSpec = subscriberSpec;
        this.handler = handler;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Bus.getTransactionService().execute(processTransaction);
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.out.println("Message handler stopped");
                throw e;
            }
        }
    }

    public void cancel() throws SQLException {
        subscriberSpec.cancel();
    }

    @Override
    public <T> RunnableFuture<T> newTask(T result) {
        return new FutureTask<T>(this, result) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                try {
                    MessageHandlerTask.this.cancel();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return super.cancel(mayInterruptIfRunning);
            }
        };
    }

    private class ProcessTransaction extends VoidTransaction {

        @Override
        protected void doPerform() {
                Message message = subscriberSpec.receive();
                if (message == null) { // receive() got cancelled, by a shut down request
                    Thread.currentThread().interrupt();
                    return;
                }
            try {
                handler.process(message);
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }
}
