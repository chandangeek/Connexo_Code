/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.database;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.upgrade.Upgrader;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_7 implements Upgrader {

    private final DataModel dataModel;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;


    @Inject
    UpgraderV10_7(DataModel dataModel, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
        createServiceCallTypes();
        try (Connection connection = this.dataModel.getConnection(true);
             Statement statement = connection.createStatement()){
            ImmutableList.<String>builder()
                    .addAll(updateColumnFromNumberToChar("SAP_CAS_DI1","DEVICE_IDENTIFIER", "DEVICE_ID"))
                    .addAll(updateColumnFromNumberToChar("SAP_CAS_DI1JRNL","DEVICE_IDENTIFIER", "DEVICE_ID"))
                    .addAll(updateColumnFromNumberToChar("SAP_CAS_DI2","LOGICAL_REGISTER_NUMBER", "LRN"))
                    .addAll(updateColumnFromNumberToChar("SAP_CAS_DI2JRNL","LOGICAL_REGISTER_NUMBER", "LRN"))
                    .addAll(updateColumnFromNumberToChar("SAP_CAS_DI3","LOGICAL_REGISTER_NUMBER", "LRN"))
                    .addAll(updateColumnFromNumberToChar("SAP_CAS_DI3JRNL","LOGICAL_REGISTER_NUMBER", "LRN"))
                    .addAll(updateColumnFromNumberToChar("SAP_CPS_MR1","DEVICEID", "DEVICEIDENTIFIER"))
                    .addAll(updateColumnFromNumberToChar("SAP_CPS_MR1JRNL","DEVICEID", "DEVICEIDENTIFIER"))
                    .addAll(updateColumnFromNumberToChar("SAP_CPS_MR1","LRN", "LRNID"))
                    .addAll(updateColumnFromNumberToChar("SAP_CPS_MR1JRNL","LRN", "LRNID"))
                    .addAll(updateColumnFromNumberToChar("SAP_CPS_MR4","DEVICEID", "DEVICEIDENTIFIER"))
                    .addAll(updateColumnFromNumberToChar("SAP_CPS_MR4JRNL","DEVICEID", "DEVICEIDENTIFIER"))
                    .addAll(updateColumnFromNumberToChar("SAP_CPS_MR4","LRN", "LRNID"))
                    .addAll(updateColumnFromNumberToChar("SAP_CPS_MR4JRNL","LRN", "LRNID"))
                    .build()
                    .forEach(command -> execute(statement, command));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private List<String> updateColumnFromNumberToChar(String tableName, String columnName, String newColumnName){
        return Arrays.asList("update " + tableName + " set " + newColumnName + " = to_char(" + columnName + ")",
                "alter table " + tableName + " drop column " + columnName + " cascade constraints");
    }

    private void createServiceCallTypes() {
        for (ServiceCallTypes serviceCallType : ServiceCallTypes.values()) {
            createServiceCallType(serviceCallType);
        }
    }

    private void createServiceCallType(ServiceCallTypes serviceCallTypeMapping) {
        Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion());
        if (!serviceCallType.isPresent()) {
            RegisteredCustomPropertySet customPropertySet = customPropertySetService
                    .findActiveCustomPropertySet(serviceCallTypeMapping.getPersistenceSupportClass())
                    .orElseThrow(() -> new IllegalStateException(
                            MessageFormat.format("Could not find active custom property set {0}",
                                    serviceCallTypeMapping.getCustomPropertySetClass())));

            serviceCallService.createServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion())
                    .handler(serviceCallTypeMapping.getTypeName())
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(customPropertySet)
                    .create();
        }
    }

}
