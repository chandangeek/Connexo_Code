package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.MessagingService;

public class InstallerImpl {	
	
	public void install(MessagingService service) {
		Bus.getOrmClient().install();
		createQueueTables(service);
	}
	
	private void createQueueTables(MessagingService service) {
		service.createQueueTableSpec("MSG_RAWQUEUETABLE","RAW", false);
		service.createQueueTableSpec("MSg_RAWTOPICTABLE" , "RAW", true);
	}
	
}
