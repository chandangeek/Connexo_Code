/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;

import java.util.logging.Logger;

public class Installer extends AbstractInstaller implements FullInstaller {
	private final Logger logger = Logger.getLogger("upgrade");

	@Inject
	public Installer(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
			MessageService messageService) {
		super(serviceCallService, customPropertySetService, messageService);
	}

	@Override
	public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
		doTry("Create service call types", this::createServiceCallTypes, logger);
		doTry("Create destination specs", this::createDestinationSpecs, logger);
	}
}
