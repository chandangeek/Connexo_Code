/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitesdevicebulkcreaterequest.UtilsDvceERPSmrtMtrBlkCrteReqMsg;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class UtilitiesDeviceBulkCreateRequestEndpoint extends AbstractInboundEndPoint implements UtilitiesDeviceERPSmartMeterBulkCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;

    @Inject
    UtilitiesDeviceBulkCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                             Thesaurus thesaurus, Clock clock, SAPCustomPropertySets sapCustomPropertySets) {
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void utilitiesDeviceERPSmartMeterBulkCreateRequestCIn(UtilsDvceERPSmrtMtrBlkCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
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
        if (serviceCall.findChildren().stream().count() > 0) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private boolean hasUtilDeviceRequestServiceCall(String id) {
        return serviceCallCommands.findAvailableServiceCalls(ServiceCallTypes.MASTER_UTILITIES_DEVICE_CREATE_REQUEST)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterUtilitiesDeviceCreateRequestDomainExtension.class))
                .filter(Objects::nonNull)
                .map(Optional::get)
                .anyMatch(domainExtension -> domainExtension.getRequestID().equals(id));
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


        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }

    private boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .findEndPointConfigurations().find().stream()
                .filter(epc -> epc.getWebServiceName().equals(name))
                .filter(EndPointConfiguration::isActive)
                .findAny().isPresent();
    }
}
