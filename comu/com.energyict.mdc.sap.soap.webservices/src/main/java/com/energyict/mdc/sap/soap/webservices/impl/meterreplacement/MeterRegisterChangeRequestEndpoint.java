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
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterRegisterChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.RegisterCreateRequestHandler;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementrequest.UtilitiesDeviceERPSmartMeterRegisterChangeRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementrequest.UtilsDvceERPSmrtMtrRegChgReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

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
    private final WebServiceActivator webServiceActivator;
    private final RegisterCreateRequestHandler registerCreateRequestHandler;

    @Inject
    MeterRegisterChangeRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                       Thesaurus thesaurus, Clock clock, SAPCustomPropertySets sapCustomPropertySets,
                                       OrmService ormService, WebServiceActivator webServiceActivator, RegisterCreateRequestHandler registerCreateRequestHandler) {
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.ormService = ormService;
        this.webServiceActivator = webServiceActivator;
        this.registerCreateRequestHandler = registerCreateRequestHandler;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void utilitiesDeviceERPSmartMeterRegisterChangeRequestCIn(UtilsDvceERPSmrtMtrRegChgReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        MeterRegisterChangeMessage message = MeterRegisterChangeMessageBuilder
                                .builder(webServiceActivator.getSapProperty(AdditionalProperties.METER_REPLACEMENT_ADD_INTERVAL))
                                .from(requestMessage)
                                .build();
                        SetMultimap<String, String> values = HashMultimap.create();
                        message.getRegisters().forEach(register -> {
                            Optional.ofNullable(register.getLrn())
                                    .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), value));
                        });
                        Optional.ofNullable(message.getDeviceId())
                                .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                        saveRelatedAttributes(values);
                        if (message.getRegisters().size() == 1) {
                            if (!isAnyActiveEndpoint(MeterRegisterChangeConfirmation.NAME)) {
                                throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                        MeterRegisterChangeConfirmation.NAME);
                            }
                            createServiceCallAndTransition(message);
                        } else if (message.getRegisters().size() > 1) {
                            registerCreateRequestHandler.handleRequestMessage(message);
                        }
                    });
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
            if (hasMeterChangeRequestServiceCall(message.getId(), message.getUuid())) {
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
        masterMeterRegisterChangeRequestDomainExtension.setUuid(requestMessage.getUuid());
        masterMeterRegisterChangeRequestDomainExtension.setBulk(false);

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterMeterRegisterChangeRequestDomainExtension)
                .create();

        if (requestMessage.isValid()) {
            createSubParentServiceCall(serviceCall, requestMessage);
        }
        if (!serviceCall.findChildren().paged(0, 0).find().isEmpty()) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private boolean hasMeterChangeRequestServiceCall(String id, String uuid) {
        Optional<DataModel> dataModel = ormService.getDataModel(MasterMeterRegisterChangeRequestCustomPropertySet.MODEL_NAME);
        if (dataModel.isPresent()) {
            if (id != null) {
                return dataModel.get().stream(MasterMeterRegisterChangeRequestDomainExtension.class)
                        .anyMatch(where(MasterMeterRegisterChangeRequestDomainExtension.FieldNames.REQUEST_ID.javaName()).isEqualTo(id));
            } else {
                return dataModel.get().stream(MasterMeterRegisterChangeRequestDomainExtension.class)
                        .anyMatch(where(MasterMeterRegisterChangeRequestDomainExtension.FieldNames.UUID.javaName()).isEqualTo(uuid));
            }
        }
        return false;
    }

    private void sendProcessError(MeterRegisterChangeMessage message, MessageSeeds messageSeed) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format());
        MeterRegisterChangeConfirmationMessage confirmationMessage =
                MeterRegisterChangeConfirmationMessage.builder()
                        .from(message, messageSeed, webServiceActivator.getMeteringSystemId(), clock.instant())
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendMessage(MeterRegisterChangeConfirmationMessage message) {
        WebServiceActivator.METER_REGISTER_CHANGE_CONFIRMATIONS
                .forEach(service -> service.call(message));
    }

    private void createSubParentServiceCall(ServiceCall parent, MeterRegisterChangeMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.SUB_MASTER_METER_REGISTER_CHANGE_REQUEST);

        SubMasterMeterRegisterChangeRequestDomainExtension subParentDomainExtension = new SubMasterMeterRegisterChangeRequestDomainExtension();
        subParentDomainExtension.setRequestId(message.getId());
        subParentDomainExtension.setUuid(message.getUuid());
        subParentDomainExtension.setDeviceId(message.getDeviceId());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(subParentDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        ServiceCall subParent = serviceCallBuilder.create();

        if (message.getRegisters().size() == 1) {
            RegisterChangeMessage register = message.getRegisters().get(0);
            if (register.isValid()) {
                createChildServiceCall(subParent, register);
            } else {
                sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
            }
        }
        if (!ServiceCallHelper.findChildren(subParent).isEmpty()) {
            subParent.requestTransition(DefaultState.PENDING);
        } else {
            subParent.requestTransition(DefaultState.REJECTED);
        }
    }

    private void createChildServiceCall(ServiceCall subParent, RegisterChangeMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.METER_REGISTER_CHANGE_REQUEST);

        MeterRegisterChangeRequestDomainExtension childDomainExtension = new MeterRegisterChangeRequestDomainExtension();
        childDomainExtension.setLrn(message.getLrn());
        childDomainExtension.setEndDate(message.getCalculatedEndDate());
        childDomainExtension.setTimeZone(message.getTimeZone());

        ServiceCallBuilder serviceCallBuilder = subParent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        serviceCallBuilder.create();
    }
}
