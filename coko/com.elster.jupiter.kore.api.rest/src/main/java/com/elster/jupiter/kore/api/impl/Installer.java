package com.elster.jupiter.kore.api.impl;


import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandDomainExtension;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandHandler;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final MessageService messageService;

    @Inject
    public Installer(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService, MessageService messageService) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        createUsagePointCommandServiceCallType();
        createUsagePointCommandDestinationSpec();
    }

    private void createUsagePointCommandServiceCallType() {
        customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                .stream()
                .filter(cps -> cps.getCustomPropertySet()
                        .getId()
                        .equals(UsagePointCommandDomainExtension.class.getName()))
                .findFirst()
                .ifPresent(customPropertySet ->
                        serviceCallService.findServiceCallType(
                                UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_NAME,
                                UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_VERSION)
                                .orElseGet(() -> serviceCallService.createServiceCallType(
                                        UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_NAME,
                                        UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_VERSION)
                                        .handler(UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_NAME)
                                        .customPropertySet(customPropertySet)
                                        .create()));
    }

    private void createUsagePointCommandDestinationSpec() {
        if (!messageService.getDestinationSpec("CommandCallback").isPresent()) {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec("CommandCallback", 60);
            destinationSpec.save();
            destinationSpec.activate();
            destinationSpec.subscribe("CommandCallback");
        }
    }

}
