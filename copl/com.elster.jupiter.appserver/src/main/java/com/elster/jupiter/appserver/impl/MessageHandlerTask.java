package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.consumer.MessageHandler;
import com.elster.jupiter.transaction.VoidTransaction;

import java.sql.SQLException;

public class MessageHandlerTask implements Runnable {

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


    private class ProcessTransaction extends VoidTransaction {

        @Override
        protected void doPerform() {
            try {
                handler.process(subscriberSpec.receive());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
