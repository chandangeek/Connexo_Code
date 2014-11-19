package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.MessageSeeds;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;

import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Logger;

public class MessageHandlerTask implements ProvidesCancellableFuture {

    private static final Logger LOGGER = Logger.getLogger(MessageHandlerTask.class.getName());

    private final SubscriberSpec subscriberSpec;
    private final MessageHandler handler;
    private final MessageHandlerTask.ProcessTransaction processTransaction = new ProcessTransaction();
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    MessageHandlerTask(SubscriberSpec subscriberSpec, MessageHandler handler, TransactionService transactionService, Thesaurus thesaurus) {
        this.subscriberSpec = subscriberSpec;
        this.handler = handler;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                transactionService.execute(processTransaction);
                handler.onMessageDelete();
            } catch (RuntimeException e) {
                MessageSeeds.MESSAGEHANDLER_FAILED.log(LOGGER, thesaurus, e);
                // transaction has been rolled back, message will be reoffered after a delay or moved to dead letter queue as configured, we can just continue with the next message
            }
        }
    }

    public void cancel() {
        subscriberSpec.cancel();
    }

    @Override
    public <T> RunnableFuture<T> newTask(T result) {
        return new FutureTask<T>(this, result) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                MessageHandlerTask.this.cancel();
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
            handler.process(message);
        }
    }
}
