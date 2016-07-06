package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.prepayment.impl.servicecall.ContactorOperationCustomPropertySet;
import com.elster.jupiter.prepayment.impl.servicecall.OperationHandler;
import com.elster.jupiter.prepayment.impl.servicecall.ServiceCallCommands;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author sva
 * @since 4/07/2016 - 10:28
 */
public class Installer implements FullInstaller {

    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public Installer(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create service call types",
                this::createServiceCallTypes,
                logger
        );
    }

    private void createServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypes serviceCallType : com.elster.jupiter.prepayment.impl.servicecall.ServiceCallCommands.ServiceCallTypes.values()) {
            createServiceCallType(serviceCallType);
        }
    }

    private void createServiceCallType(ServiceCallCommands.ServiceCallTypes serviceCallTypeMapping) {
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService.findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion());
        if (!serviceCallTypeOptional.isPresent()) {
            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySet(new ContactorOperationCustomPropertySet().getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", ContactorOperationCustomPropertySet.class.getSimpleName())));

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion())
                    .handler(OperationHandler.HANDLER_NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(customPropertySet)
                    .create();
        }
    }
}