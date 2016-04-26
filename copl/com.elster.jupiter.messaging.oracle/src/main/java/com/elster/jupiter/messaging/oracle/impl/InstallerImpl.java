package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;

public class InstallerImpl implements FullInstaller {

    private final DataModel dataModel;
	private final MessageService messageService;

	@Inject
    public InstallerImpl(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
		this.messageService = messageService;
	}

    public void install(DataModelUpgrader dataModelUpgrader) {
		dataModelUpgrader.upgrade(dataModel, Version.latest());
		createQueueTables();
	}
	
	private void createQueueTables() {
		messageService.createQueueTableSpec("MSG_RAWQUEUETABLE", "RAW", false);
		messageService.createQueueTableSpec("MSG_RAWTOPICTABLE" , "RAW", true);
	}
	
}
