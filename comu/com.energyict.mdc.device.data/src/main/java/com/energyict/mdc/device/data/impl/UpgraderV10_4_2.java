package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;

public class UpgraderV10_4_2 implements Upgrader {

    private final DataModel dataModel;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public UpgraderV10_4_2(DataModel dataModel, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 2));
        createServiceCallTypes();
    }


    private void createServiceCallTypes() {
        for (ServiceCallCommands.ServiceCallTypeMapping serviceCallTypeMapping : ServiceCallCommands.ServiceCallTypeMapping.values()) {
                createServiceCallType(serviceCallTypeMapping);
        }
    }

    private void createServiceCallType(ServiceCallCommands.ServiceCallTypeMapping serviceCallTypeMapping) {
        Optional<ServiceCallType> serviceCallTypeOptional = serviceCallService.findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion());
        if (!serviceCallTypeOptional.isPresent()) {
            RegisteredCustomPropertySet commandCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(new CommandCustomPropertySet().getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", CommandCustomPropertySet.class.getSimpleName())));
            RegisteredCustomPropertySet completionOptionsCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(new CompletionOptionsCustomPropertySet().getId())
                    .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set {0}", CommandCustomPropertySet.class.getSimpleName())));

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion())
                    .handler(serviceCallTypeMapping.getTypeName())
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(commandCustomPropertySet)
                    .customPropertySet(completionOptionsCustomPropertySet)
                    .create();
        }
    }
}