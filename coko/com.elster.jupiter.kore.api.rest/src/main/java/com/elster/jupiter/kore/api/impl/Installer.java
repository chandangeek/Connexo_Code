package com.elster.jupiter.kore.api.impl;


import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandCustomPropertySet;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandDomainExtension;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandHandler;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

public class Installer implements FullInstaller {

    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final MessageService messageService;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public Installer(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService, PropertySpecService propertySpecService, MessageService messageService, Thesaurus thesaurus) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.messageService = messageService;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        createUsagePointCommandServiceCallType();
        createUsagePointCommandDestinationSpec();
    }

    private void createUsagePointCommandServiceCallType() {
        Optional<RegisteredCustomPropertySet> customPropertySet = findUsagePointCommandCustomPropertySet();
        if (customPropertySet.isPresent()) {
            createUsagePointCommandServiceCallType(customPropertySet.get());
        } else {
            customPropertySetService.addCustomPropertySet(new UsagePointCommandCustomPropertySet(propertySpecService, thesaurus));
            findUsagePointCommandCustomPropertySet()
                    .ifPresent(this::createUsagePointCommandServiceCallType);
        }
    }

    private Optional<RegisteredCustomPropertySet> findUsagePointCommandCustomPropertySet() {
        return customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                .stream()
                .filter(cps -> cps.getCustomPropertySet()
                        .getId()
                        .equals(UsagePointCommandDomainExtension.class.getName()))
                .findFirst();
    }

    private void createUsagePointCommandServiceCallType(RegisteredCustomPropertySet customPropertySet) {
        serviceCallService.findServiceCallType(
                UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_NAME,
                UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_VERSION)
                .orElseGet(() -> serviceCallService.createServiceCallType(
                        UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_NAME,
                        UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_VERSION)
                        .handler(UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_NAME)
                        .customPropertySet(customPropertySet)
                        .create());
    }

    private void createUsagePointCommandDestinationSpec() {
        if (!messageService.getDestinationSpec("CommandCallback").isPresent()) {
            QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec("CommandCallback", 60);
            destinationSpec.save();
            destinationSpec.activate();
            destinationSpec.subscribe(TranslationKeys.USAGE_POINT_COMMAND_MESSAGE_HANDLER_DISPLAYNAME, PublicRestApplication.COMPONENT_NAME, Layer.REST);
        }
    }

}