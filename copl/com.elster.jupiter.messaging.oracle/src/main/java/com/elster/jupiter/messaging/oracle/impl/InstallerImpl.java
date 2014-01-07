package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;

public class InstallerImpl {

    private final DataModel dataModel;

    public InstallerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void install(MessageService service) {
		dataModel.install(true, true);
		createQueueTables(service);
	}
	
	private void createQueueTables(MessageService service) {
		service.createQueueTableSpec("MSG_RAWQUEUETABLE", "RAW", false);
		service.createQueueTableSpec("MSG_RAWTOPICTABLE" , "RAW", true);
	}
	
}
