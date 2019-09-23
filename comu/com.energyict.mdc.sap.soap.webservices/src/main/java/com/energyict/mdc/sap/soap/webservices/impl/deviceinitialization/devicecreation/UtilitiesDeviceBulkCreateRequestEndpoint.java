/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrBlkCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class UtilitiesDeviceBulkCreateRequestEndpoint extends AbstractInboundEndPoint implements UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final OrmService ormService;

    @Inject
    UtilitiesDeviceBulkCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                             Thesaurus thesaurus, Clock clock, SAPCustomPropertySets sapCustomPropertySets,
                                             OrmService ormService) {
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.ormService = ormService;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void utilitiesDeviceERPSmartMeterBulkCreateRequestCIn(UtilsDvceERPSmrtMtrBlkCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            SetMultimap<String, String> values = HashMultimap.create();

            request.getUtilitiesDeviceERPSmartMeterCreateRequestMessage().forEach(req->{
                values.put(WebServiceRequestAttributesNames.SAP_SERIAL_ID.getAttributeName(), req.getUtilitiesDevice().getSerialID());
                values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), req.getUtilitiesDevice().getID().getValue());
            });

            values.keys().forEach(key->{
                createRelatedObjects(key, values.get(key));
            });

            if (!isAnyActiveEndpoint(UtilitiesDeviceBulkCreateConfirmation.NAME)) {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                        UtilitiesDeviceBulkCreateConfirmation.NAME);
            }

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> createServiceCallAndTransition(UtilitiesDeviceCreateRequestMessage.builder()
                            .from(requestMessage)
                            .build()));
            return null;
        });

    }

    private void createServiceCallAndTransition(UtilitiesDeviceCreateRequestMessage message) {
        if (message.isValid()) {
            if (hasUtilDeviceRequestServiceCall(message.getRequestID())) {
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_UTILITIES_DEVICE_CREATE_REQUEST).ifPresent(serviceCallType -> {
                    createServiceCall(serviceCallType, message);
                });
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private void createServiceCall(ServiceCallType serviceCallType, UtilitiesDeviceCreateRequestMessage requestMessage) {
        MasterUtilitiesDeviceCreateRequestDomainExtension masterUtilitiesDeviceCreateRequestDomainExtension =
                new MasterUtilitiesDeviceCreateRequestDomainExtension();
        masterUtilitiesDeviceCreateRequestDomainExtension.setRequestID(requestMessage.getRequestID());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterUtilitiesDeviceCreateRequestDomainExtension)
                .create();

        requestMessage.getUtilitiesDeviceCreateMessages()
                .forEach(bodyMessage -> {
                    if (bodyMessage.isValid()) {
                        createChildServiceCall(serviceCall, bodyMessage);
                    }
                });
        if (!serviceCall.findChildren().paged(0, 0).find().isEmpty()) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private boolean hasUtilDeviceRequestServiceCall(String id) {
        Optional<DataModel> dataModel = ormService.getDataModel(MasterUtilitiesDeviceCreateRequestCustomPropertySet.MODEL_NAME);
        if (dataModel.isPresent()) {
            return dataModel.get().stream(MasterUtilitiesDeviceCreateRequestDomainExtension.class)
                    .anyMatch(where(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName()).isEqualTo(id));
        }
        return false;
    }

    private void sendProcessError(UtilitiesDeviceCreateRequestMessage message, MessageSeeds messageSeed) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format());
        UtilitiesDeviceCreateConfirmationMessage confirmationMessage =
                UtilitiesDeviceCreateConfirmationMessage.builder()
                        .from(message, messageSeed, clock.instant())
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendMessage(UtilitiesDeviceCreateConfirmationMessage confirmationMessage) {
        WebServiceActivator.UTILITIES_DEVICE_BULK_CREATE_CONFIRMATION
                .forEach(service -> service.call(confirmationMessage));
    }

    private void createChildServiceCall(ServiceCall parent, UtilitiesDeviceCreateMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.UTILITIES_DEVICE_CREATE_REQUEST);

        UtilitiesDeviceCreateRequestDomainExtension childDomainExtension = new UtilitiesDeviceCreateRequestDomainExtension();
        childDomainExtension.setSerialId(message.getSerialId());
        childDomainExtension.setDeviceId(message.getDeviceId());
        childDomainExtension.setDeviceType(message.getDeviceType());
        childDomainExtension.setShipmentDate(message.getShipmentDate());
        childDomainExtension.setManufacturer(message.getManufacturer());
        childDomainExtension.setModelNumber(message.getModelNumber());


        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }

    private boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(name)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .findAny().isPresent();
    }
}
