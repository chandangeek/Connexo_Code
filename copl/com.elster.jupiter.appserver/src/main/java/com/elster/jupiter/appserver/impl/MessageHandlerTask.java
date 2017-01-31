/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.MessageSeeds;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;

import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Logger;

public class MessageHandlerTask implements ProvidesCancellableFuture {

    private static final Logger LOGGER = Logger.getLogger(MessageHandlerTask.class.getName());

    private final SubscriberSpec subscriberSpec;
    private final MessageHandler handler;
    private final Transaction<Message> processTransaction = new ProcessTransaction();
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
                Message message = transactionService.execute(processTransaction);
                if (message != null) {
                    handler.onMessageDelete(message);
                }
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

    private class ProcessTransaction implements Transaction<Message> {
        @Override
        public Message perform() {
            Message message = subscriberSpec.receive();
            if (message == null) { // receive() got cancelled, by a shut down request
                Thread.currentThread().interrupt();
                return null;
            }
            handler.process(message);
            return message;
        }
    }
}
