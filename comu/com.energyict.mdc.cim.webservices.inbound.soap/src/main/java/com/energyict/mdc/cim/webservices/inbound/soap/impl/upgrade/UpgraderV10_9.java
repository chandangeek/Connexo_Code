/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterCustomPropertySet;

import javax.inject.Inject;
import java.sql.Statement;
import java.text.MessageFormat;

import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.DATA_LINKAGE_CONFIG;
import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG;

public class UpgraderV10_9 implements Upgrader {

    private final DataModel dataModel;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    UpgraderV10_9(DataModel dataModel, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        updateOldMasterDataLinkageConfigServiceCalls();
    }

    private void updateOldMasterDataLinkageConfigServiceCalls() {
        serviceCallService.findServiceCallType(MASTER_DATA_LINKAGE_CONFIG.getTypeName(), MASTER_DATA_LINKAGE_CONFIG.getTypeVersion())
                .ifPresent(serviceCallType -> {
                    serviceCallType.getCustomPropertySets().stream()
                            .forEach(cps -> serviceCallType.removeCustomPropertySet(cps));

                    RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(MasterDataLinkageConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_ID)
                            .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set by id {0}", MasterDataLinkageConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_ID)));
                    serviceCallType.addCustomPropertySet(registeredCustomPropertySet);
                    serviceCallType.save();
                });

        serviceCallService.findServiceCallType(DATA_LINKAGE_CONFIG.getTypeName(), DATA_LINKAGE_CONFIG.getTypeVersion())
                .ifPresent(serviceCallType -> {
                    serviceCallType.getCustomPropertySets().stream()
                            .forEach(cps -> serviceCallType.removeCustomPropertySet(cps));

                    RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(MasterDataLinkageConfigCustomPropertySet.CUSTOM_PROPERTY_SET_ID)
                            .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set by id {0}", MasterDataLinkageConfigCustomPropertySet.CUSTOM_PROPERTY_SET_ID)));
                    serviceCallType.addCustomPropertySet(registeredCustomPropertySet);
                    serviceCallType.save();
                });

        migrateSql();
    }

    private void migrateSql() {
        String sql1 =
                "BEGIN " +
                        "  DELETE FROM DLP_MSC_WS1; " +
                        "  DELETE FROM CPS_REGISTERED_CUSTOMPROPSET where LOGICALID = 'com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterDomainExtension'; " +
                        "END; ";

        String sql2 =
                "BEGIN " +
                        "  DELETE FROM DLP_CSC_WS1; " +
                        "  DELETE FROM CPS_REGISTERED_CUSTOMPROPSET where LOGICALID = 'com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigDomainExtension'; " +
                        "END; ";

        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                execute(statement, sql1);
                execute(statement, sql2);
            }
        });
    }
}

