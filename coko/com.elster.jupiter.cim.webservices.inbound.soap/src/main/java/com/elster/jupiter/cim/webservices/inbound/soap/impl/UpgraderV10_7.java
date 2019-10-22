/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Inject;

public class UpgraderV10_7 extends AbstractInstaller implements com.elster.jupiter.upgrade.Upgrader {

    private DataModel dataModel;

    @Inject
    public UpgraderV10_7(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
                         MessageService messageService, DataModel dataModel) {
        super(serviceCallService, customPropertySetService, messageService);
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        updateServiceCallTypes();
        execute(dataModel, "DELETE FROM APS_SUBSCRIBEREXECUTIONSPEC WHERE destinationspec = 'ReadMeterStatusChgTopic'");
        removeObsoleteDestinationSpecIfPresent();
    }

    private void updateServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypes type : ServiceCallCommands.ServiceCallTypes.values()) {
            type.getApplication().ifPresent(
                    application ->
                            serviceCallService
                                    .findServiceCallType(type.getTypeName(), type.getTypeVersion()).ifPresent(
                                    serviceCallType -> {
                                        serviceCallType.setApplication(application);
                                        serviceCallType.save();
                                    }
                            ));
        }
    }

    private void removeObsoleteDestinationSpecIfPresent() {
        String oldDestinationSpec = "ReadMeterStatusChgTopic";
        messageService.getDestinationSpec(oldDestinationSpec).ifPresent(destinationSpec -> {
            destinationSpec.unSubscribe(oldDestinationSpec);
            destinationSpec.delete();
        });
    }
}
