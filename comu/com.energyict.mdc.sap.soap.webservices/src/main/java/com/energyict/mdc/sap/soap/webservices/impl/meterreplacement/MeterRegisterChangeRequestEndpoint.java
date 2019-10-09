/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

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
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterRegisterChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementrequest.UtilitiesDeviceERPSmartMeterRegisterChangeRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementrequest.UtilsDvceERPSmrtMtrRegChgReqMsg;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MeterRegisterChangeRequestEndpoint extends AbstractInboundEndPoint implements UtilitiesDeviceERPSmartMeterRegisterChangeRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final OrmService ormService;

    @Inject
    MeterRegisterChangeRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
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
    public void utilitiesDeviceERPSmartMeterRegisterChangeRequestCIn(UtilsDvceERPSmrtMtrRegChgReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            if (!isAnyActiveEndpoint(MeterRegisterChangeConfirmation.NAME)) {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                        MeterRegisterChangeConfirmation.NAME);
            }

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> createServiceCallAndTransition(MeterRegisterChangeMessageBuilder.builder()
                            .from(requestMessage)
                            .build()));
            return null;
        });
    }

    private boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(name)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .findAny().isPresent();
    }

    private void createServiceCallAndTransition(MeterRegisterChangeMessage message) {
        if (message.isValid()) {
            if (hasMeterChangeRequestServiceCall(message.getId())) {
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_METER_REGISTER_CHANGE_REQUEST).ifPresent(serviceCallType -> {
                    createServiceCall(serviceCallType, message);
                });
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private void createServiceCall(ServiceCallType serviceCallType, MeterRegisterChangeMessage requestMessage) {
        MasterMeterRegisterChangeRequestDomainExtension masterMeterRegisterChangeRequestDomainExtension =
                new MasterMeterRegisterChangeRequestDomainExtension();
        masterMeterRegisterChangeRequestDomainExtension.setRequestId(requestMessage.getId());
        masterMeterRegisterChangeRequestDomainExtension.setBulk(false);

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterMeterRegisterChangeRequestDomainExtension)
                .create();

        if (requestMessage.isValid()) {
            createChildServiceCall(serviceCall, requestMessage);
        }
        if (!serviceCall.findChildren().paged(0, 0).find().isEmpty()) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private boolean hasMeterChangeRequestServiceCall(String id) {
        Optional<DataModel> dataModel = ormService.getDataModel(MasterMeterRegisterChangeRequestCustomPropertySet.MODEL_NAME);
        if (dataModel.isPresent()) {
            return dataModel.get().stream(MasterMeterRegisterChangeRequestDomainExtension.class)
                    .anyMatch(where(MasterMeterRegisterChangeRequestDomainExtension.FieldNames.REQUEST_ID.javaName()).isEqualTo(id));
        }
        return false;
    }

    private void sendProcessError(MeterRegisterChangeMessage message, MessageSeeds messageSeed) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format());
        MeterRegisterChangeConfirmationMessage confirmationMessage =
                MeterRegisterChangeConfirmationMessage.builder()
                        .from(message, messageSeed, clock.instant())
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendMessage(MeterRegisterChangeConfirmationMessage message) {
        WebServiceActivator.METER_REGISTER_CHANGE_CONFIRMATIONS
                .forEach(service -> service.call(message));
    }

    private void createChildServiceCall(ServiceCall parent, MeterRegisterChangeMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.METER_REGISTER_CHANGE_REQUEST);

        MeterRegisterChangeRequestDomainExtension childDomainExtension = new MeterRegisterChangeRequestDomainExtension();
        childDomainExtension.setRequestId(message.getId());
        childDomainExtension.setDeviceId(message.getDeviceId());
        childDomainExtension.setLrn(message.getLrn());
        childDomainExtension.setEndDate(message.getEndDate());
        childDomainExtension.setTimeZone(message.getTimeZone());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }
}
