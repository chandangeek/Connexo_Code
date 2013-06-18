package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.consumer.MessageHandler;
import com.elster.jupiter.transaction.Transaction;

import java.sql.SQLException;

public class MessageHandlerTask implements Runnable , Transaction<Void> {
	
	private final SubscriberSpecImpl consumerSpec;
	private final MessageHandler handler;
	
	MessageHandlerTask(SubscriberSpecImpl consumerSpec,MessageHandler handler) {
		this.consumerSpec = consumerSpec;
		this.handler = handler;
	}

	@Override
	public void run() {
		for (;;) {
			try {
				Bus.getTransactionService().execute(this);
			} catch (Throwable ex) {
				ex.printStackTrace();
				System.out.println("Message handler stopped");
				throw ex;							
			}
		}
	}

	@Override
	public Void perform() {
		try {
			handler.process(consumerSpec.receive());
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
}
