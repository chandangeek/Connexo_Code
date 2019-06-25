
/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.tasks.TaskService;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.energyict.mdc.cim.webservices.inbound.soap.task.FutureComTaskExecutionHandlerFactory;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.List;


public class UpgraderV10_6 extends SqlExecuteUpgrader {

    @Inject
    UpgraderV10_6(OrmService ormService, BundleContext bundleContext, MessageService messageService, TaskService taskService) {
        super(ormService, bundleContext, messageService, taskService);
    }

    @Override
    protected List<String> getSQLStatementsToExecute() {
        return Arrays.asList(
                ignoreColumnExistsOrTableDoesNotExist("alter table MCP_SCS_CNT modify (CALLBACK_URL NULL)"),
                ignoreColumnExistsOrTableDoesNotExist("alter table MCP_SCS_CNTJRNL modify (CALLBACK_URL NULL)"));
    }

}
