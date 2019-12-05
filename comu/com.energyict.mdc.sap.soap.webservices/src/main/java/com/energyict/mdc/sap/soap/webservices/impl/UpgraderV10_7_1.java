/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

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
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
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
        createNewlyAddedServiceCallTypes();
        updateEndpointNames();
        dropOldUrlColumns();
    }

    private void dropOldUrlColumns() {
        List<String> oldColumnsSql = ImmutableList.<String>builder()
                .add("ALTER TABLE SAP_CS1_CR_SC_CPS DROP COLUMN CONFIRMATION_URL")
                .add("ALTER TABLE SAP_CS1_CR_SC_CPSJRNL DROP COLUMN CONFIRMATION_URL")
                .add("ALTER TABLE SAP_CPS_MR2 DROP COLUMN CONFIRMATION_URL")
                .add("ALTER TABLE SAP_CPS_MR2 DROP COLUMN RESULT_URL")
                .add("ALTER TABLE SAP_CPS_MR2JRNL DROP COLUMN CONFIRMATION_URL")
                .add("ALTER TABLE SAP_CPS_MR2JRNL DROP COLUMN RESULT_URL")
                .add("ALTER TABLE SAP_CPS_MR3 DROP COLUMN RESULT_URL")
                .add("ALTER TABLE SAP_CPS_MR3JRNL DROP COLUMN RESULT_URL")
                .build();

        try (Connection connection = this.dataModel.getConnection(true);
            Statement statement = connection.createStatement()) {
            oldColumnsSql.forEach(oldColumn -> {
                try {
                    execute(statement, oldColumn);
                } catch (Exception e) {
                    // no action if column already not exists
                }
            });
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void updateEndpointNames() {
        Map<String, String> endpointNames = ImmutableMap.<String, String>builder()
                .put("SapStatusChangeRequestCreate", "SAP ConnectionStatusChangeRequest")
                .put("SapMeterReadingRequest", "SAP MeterReadingRequest")
                .put("SapMeterReadingBulkRequest", "SAP MeterReadingBulkRequest")
                .put("SapMeterReadingResultConfirmation", "SAP MeterReadingResultConfirmation")
                .put("SapMeterReadingBulkResultConfirmation", "SAP MeterReadingBulkResultConfirmation")
                .put("SAP UtilitiesDeviceERPSmartMeterCreateRequest_C_In", "SAP SmartMeterCreateRequest")
                .put("SAP UtilitiesDeviceERPSmartMeterBulkCreateRequest_C_In", "SAP SmartMeterBulkCreateRequest")
                .put("SAP UtilitiesDeviceERPSmartMeterRegisterCreateRequest_C_In", "SAP SmartMeterRegisterCreateRequest")
                .put("SAP UtilitiesDeviceERPSmartMeterRegisterBulkCreateRequest_C_In", "SAP SmartMeterRegisterBulkCreateRequest")
                .put("SAP UtilitiesDeviceERPSmartMeterLocationNotification_C_In", "SAP SmartMeterLocationNotification")
                .put("SAP UtilitiesDeviceERPSmartMeterLocationBulkNotification_C_In", "SAP SmartMeterLocationBulkNotification")
                .put("SAP PointOfDeliveryAssignedNotification_C_In", "SAP PointOfDeliveryAssignedNotification")
                .put("SAP PointOfDeliveryBulkAssignedNotification_C_In", "SAP PointOfDeliveryBulkAssignedNotification")
                .put("SAP UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeRequest_C_In", "SAP MeasurementTaskAssignmentChangeRequest")
                .put("SAP StatusChangeRequestCancellation", "SAP ConnectionStatusChangeCancellationRequest")
                .put("SAP StatusChangeRequestBulkCreate", "SAP ConnectionStatusChangeBulkRequest")
                .put("SAP SmartMeterEventCreateConfirmation", "SAP SmartMeterEventBulkCreateConfirmation")
                .put("SAP UtilitiesTimeSeriesERPItemBulkChangeConfirmation_C_In", "SAP TimeSeriesBulkChangeConfirmation")
                .put("SAP UtilitiesTimeSeriesERPItemBulkCreateConfirmation_C_In", "SAP TimeSeriesBulkCreateConfirmation")
                .put("SAP UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmation_C_Out", "SAP MeasurementTaskAssignmentChangeConfirmation")
                .put("SapMeterReadingBulkRequestConfirmation", "SAP MeterReadingBulkRequestConfirmation")
                .put("SapMeterReadingBulkResult", "SAP MeterReadingBulkResultRequest")
                .put("SapMeterReadingRequestConfirmation", "SAP MeterReadingRequestConfirmation")
                .put("SapMeterReadingResult", "SAP MeterReadingResultRequest")
                .put("SAP StatusChangeRequestBulkCreateConfirmation", "SAP ConnectionStatusChangeBulkConfirmation")
                .put("SAP StatusChangeRequestCancellationConfirmation", "SAP ConnectionStatusChangeCancellationConfirmation")
                .put("SapStatusChangeRequestCreateConfirmation", "SAP ConnectionStatusChangeСonfirmation")
                .put("SAP UtilitiesDeviceERPSmartMeterBulkCreateConfirmation_C_Out", "SAP SmartMeterBulkCreateConfirmation")
                .put("SAP UtilitiesDeviceERPSmartMeterCreateConfirmation_C_Out", "SAP SmartMeterCreateConfirmation")
                .put("SAP UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmation_C_Out", "SAP SmartMeterRegisterBulkCreateConfirmation")
                .put("SAP UtilitiesDeviceERPSmartMeterRegisterCreateConfirmation_C_Out", "SAP SmartMeterRegisterCreateConfirmation")
                .put("SAP UtilitiesDeviceERPSmartMeterRegisteredBulkNotification_C_Out", "SAP SmartMeterRegisteredBulkNotification")
                .put("SAP UtilitiesDeviceERPSmartMeterRegisteredNotification_C_Out", "SAP SmartMeterRegisteredNotification")
                .put("CreateUtilitiesSmartMeterEvent", "SAP SmartMeterEventBulkCreateRequest")
                .put("SAP UtilitiesTimeSeriesERPItemBulkChangeRequest_C_Out", "SAP TimeSeriesBulkChangeRequest")
                .put("SAP UtilitiesTimeSeriesERPItemBulkCreateRequest_C_Out", "SAP TimeSeriesBulkCreateRequest")
                .build();
        try (Connection connection = this.dataModel.getConnection(true);
            Statement statement = connection.createStatement()) {
            endpointNames.entrySet().forEach(oldName -> execute(statement, updateEndpointNameSql(oldName.getKey(), oldName.getValue())));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private String updateEndpointNameSql(String oldName, String newName) {
        return "UPDATE WS_ENDPOINTCFG SET WEBSERVICENAME = '" + newName + "' WHERE WEBSERVICENAME = '" + oldName + "'";
    }

    private void createNewlyAddedServiceCallTypes() {
        for (ServiceCallTypes serviceCallType : ServiceCallTypes.values()) {
            createNewlyAddedServiceCallType(serviceCallType);
        }
    }

    private void createNewlyAddedServiceCallType(ServiceCallTypes serviceCallTypeMapping) {
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
        }
    }
}
