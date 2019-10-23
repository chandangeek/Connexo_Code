package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.Upgrader;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;

import javax.inject.Inject;

import java.text.MessageFormat;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_7_1 implements Upgrader {
    private final DataModel dataModel;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public UpgraderV10_7_1(DataModel dataModel, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7, 1));
        createOrUpdateServiceCallTypes();
    }

    private void createOrUpdateServiceCallTypes() {
        for (ServiceCallTypes serviceCallType : ServiceCallTypes.values()) {
            createOrUpdateServiceCallType(serviceCallType);
        }
    }

    private void createOrUpdateServiceCallType(ServiceCallTypes serviceCallTypeMapping) {
        Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion());
        if (!serviceCallType.isPresent()) {
            RegisteredCustomPropertySet customPropertySet = customPropertySetService
                    .findActiveCustomPropertySet(serviceCallTypeMapping.getPersistenceSupportClass())
                    .orElseThrow(() -> new IllegalStateException(
                            MessageFormat.format("Could not find active custom property set {0}",
                                    serviceCallTypeMapping.getCustomPropertySetClass())));

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion(),serviceCallTypeMapping.getApplication().orElse(null))
                    .handler(serviceCallTypeMapping.getTypeName())
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(customPropertySet)
                    .create();
        }else{
            serviceCallTypeMapping.getApplication().ifPresent(
                    application ->
                            serviceCallService
                                    .findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion()).ifPresent(
                                    type -> {
                                        type.setApplication(application);
                                        type.save();
                                    }
                            ));
        }
    }
}
