/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InstallerV1;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;

import javax.inject.Inject;
import java.sql.Statement;
import java.text.MessageFormat;

import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.PARENT_GET_METER_READINGS;
import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_ID;

public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final InstallerV1 installer;

    @Inject
    UpgraderV10_7(DataModel dataModel, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
                  InstallerV1 installer) {
        this.dataModel = dataModel;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.installer = installer;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        updateOldGetMeterReadingsServiceCalls();
        updateServiceCallTypes();
        installer.createServiceCallTypes();
    }

    private void updateOldGetMeterReadingsServiceCalls() {
        serviceCallService.findServiceCallType(PARENT_GET_METER_READINGS.getTypeName(), PARENT_GET_METER_READINGS.getTypeVersion())
                .ifPresent(serviceCallType -> {
                    serviceCallType.getCustomPropertySets().stream()
                            .forEach(cps -> serviceCallType.removeCustomPropertySet(cps));

                    RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(CUSTOM_PROPERTY_SET_ID)
                            .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set by id {0}", CUSTOM_PROPERTY_SET_ID)));
                    serviceCallType.addCustomPropertySet(registeredCustomPropertySet);
                    serviceCallType.save();
                });

        migrateSql();
    }

    private void migrateSql() {
        String sql = "DELETE FROM CPS_REGISTERED_CUSTOMPROPSET where LOGICALID = 'com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsDomainExtension'";
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                execute(statement, sql);
            }
        });
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
}

