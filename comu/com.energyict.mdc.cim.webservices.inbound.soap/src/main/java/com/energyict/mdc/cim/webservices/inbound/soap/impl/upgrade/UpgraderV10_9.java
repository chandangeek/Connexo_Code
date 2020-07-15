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
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InstallerV1;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterCustomPropertySet;

import javax.inject.Inject;
import java.text.MessageFormat;

import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.DATA_LINKAGE_CONFIG;
import static com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands.ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG;

public class UpgraderV10_9 implements Upgrader {

    private final DataModel dataModel;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final InstallerV1 installer;

    @Inject
    UpgraderV10_9(DataModel dataModel, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService,
            InstallerV1 installer) {
        this.dataModel = dataModel;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.installer = installer;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        updateOldMasterDataLinkageConfigServiceCalls();
        installer.createServiceCallTypes();
    }

    private void updateOldMasterDataLinkageConfigServiceCalls() {
        updateServiceCall(MASTER_DATA_LINKAGE_CONFIG, MasterDataLinkageConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_ID);
        updateServiceCall(DATA_LINKAGE_CONFIG, MasterDataLinkageConfigCustomPropertySet.CUSTOM_PROPERTY_SET_ID);
        migrateSql();
    }

    private void updateServiceCall(ServiceCallCommands.ServiceCallTypes type, String customPropertySetId) {
        serviceCallService.findServiceCallType(type.getTypeName(), type.getTypeVersion())
                .ifPresent(serviceCallType -> {
                    serviceCallType.getCustomPropertySets().forEach(serviceCallType::removeCustomPropertySet);

                    RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySetId)
                            .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find active custom property set by id {0}", customPropertySetId)));
                    serviceCallType.addCustomPropertySet(registeredCustomPropertySet);
                    serviceCallType.save();
                });
    }

    private void migrateSql() {
        String sql1 =
                "BEGIN " +
                        "  UPDATE DLP_MSC_WS1 SET CPS = (SELECT ID FROM CPS_REGISTERED_CUSTOMPROPSET where LOGICALID = '" + MasterDataLinkageConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_ID +  "'); " +
                        "  DELETE FROM CPS_REGISTERED_CUSTOMPROPSET where LOGICALID = 'com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterDomainExtension'; " +
                        "END; ";

        String sql2 =
                "BEGIN " +
                        "  UPDATE DLP_CSC_WS1 SET CPS = (SELECT ID FROM CPS_REGISTERED_CUSTOMPROPSET where LOGICALID = '" + MasterDataLinkageConfigCustomPropertySet.CUSTOM_PROPERTY_SET_ID +  "'); " +
                        "  DELETE FROM CPS_REGISTERED_CUSTOMPROPSET where LOGICALID = 'com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigDomainExtension'; " +
                        "END; ";

        execute(dataModel, sql1, sql2);
    }
}

