package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.MessageService;

public class InstallerImpl {	
	
	public void install(MessageService service) {
		Bus.getOrmClient().install();
		createQueueTables(service);
	}
	
	private void createQueueTables(MessageService service) {
		service.createQueueTableSpec("MSG_RAWQUEUETABLE", "RAW", false);
		service.createQueueTableSpec("MSG_RAWTOPICTABLE" , "RAW", true);
	}
	
}
