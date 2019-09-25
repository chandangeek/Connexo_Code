/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.tasks.TaskService;

import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.energyict.mdc.sap.UpdateSapExportTaskHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + UpdateSapExportTaskHandlerFactory.UPDATE_SAP_EXPORT_TASK_SUBSCRIBER,
                "destination=" + UpdateSapExportTaskHandlerFactory.UPDATE_SAP_EXPORT_TASK_DESTINATION},
        immediate = true)
public class UpdateSapExportTaskHandlerFactory implements MessageHandlerFactory {
    public static final String UPDATE_SAP_EXPORT_TASK_DESTINATION = "UpdateSapExportTaskTopic";
    public static final String UPDATE_SAP_EXPORT_TASK_SUBSCRIBER = "UpdateSapExportTaskSubscriber";
    public static final String UPDATE_SAP_EXPORT_TASK_DISPLAYNAME = "Update SAP export group task";

    private volatile TaskService taskService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile DataExportService dataExportService;

    public UpdateSapExportTaskHandlerFactory() {
        //for OSGi purpose
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new UpdateSapExportTaskHandler(meteringGroupsService, sapCustomPropertySets, clock, dataExportService));
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        //for OSGi purpose
    }
}
