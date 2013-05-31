package com.elster.jupiter.messaging.impl;

import java.sql.SQLException;

import com.elster.jupiter.messaging.consumer.MessageHandler;
import com.elster.jupiter.transaction.Transaction;

public class MessageHandlerTask implements Runnable , Transaction<Void> {
	
	private final ConsumerSpecImpl consumerSpec;
	private final MessageHandler handler;
	
	MessageHandlerTask(ConsumerSpecImpl consumerSpec,MessageHandler handler) {
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
