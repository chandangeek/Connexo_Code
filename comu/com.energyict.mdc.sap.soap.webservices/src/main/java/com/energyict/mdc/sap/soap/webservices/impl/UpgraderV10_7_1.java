/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

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

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;

import java.text.MessageFormat;
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
        for(String oldName: endpointNames.keySet()) {
            updateEndpointName(oldName, endpointNames.get(oldName));
        }
    }

    private void updateEndpointName(String oldName, String newName) {
        execute(dataModel, "UPDATE WS_ENDPOINTCFG SET WEBSERVICENAME = '" + newName + "' WHERE WEBSERVICENAME = '" + oldName + "'");
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
