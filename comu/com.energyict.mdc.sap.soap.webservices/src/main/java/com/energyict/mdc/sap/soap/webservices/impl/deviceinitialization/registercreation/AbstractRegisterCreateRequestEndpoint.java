package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

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
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestDomainExtension;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public abstract class AbstractRegisterCreateRequestEndpoint extends AbstractInboundEndPoint implements ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final Thesaurus thesaurus;
    private final OrmService ormService;

    @Inject
    AbstractRegisterCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                          Clock clock, SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus,
                                          OrmService ormService) {
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.clock = clock;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.thesaurus = thesaurus;
        this.ormService = ormService;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    void createServiceCallAndTransition(UtilitiesDeviceRegisterCreateRequestMessage message) {
        if (message.isValid()) {
            if(hasUtilDeviceRegisterRequestServiceCall(message.getRequestID())){
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            }else {
                serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST).ifPresent(serviceCallType -> {
                    createServiceCall(serviceCallType, message);
                });
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(name)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .findAny().isPresent();
    }

    private void createServiceCall(ServiceCallType serviceCallType, UtilitiesDeviceRegisterCreateRequestMessage requestMessage) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension masterUtilitiesDeviceRegisterCreateRequestDomainExtension =
                new MasterUtilitiesDeviceRegisterCreateRequestDomainExtension();
        masterUtilitiesDeviceRegisterCreateRequestDomainExtension.setRequestID(requestMessage.getRequestID());
        masterUtilitiesDeviceRegisterCreateRequestDomainExtension.setBulk(requestMessage.isBulk());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterUtilitiesDeviceRegisterCreateRequestDomainExtension)
                .create();

        requestMessage.getUtilitiesDeviceRegisterCreateMessages()
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

    private void createChildServiceCall(ServiceCall parent, UtilitiesDeviceRegisterCreateMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.SUB_MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST);

        SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension childDomainExtension = new SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension();
        childDomainExtension.setDeviceId(message.getDeviceId());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        ServiceCall serviceCall = serviceCallBuilder.create();

        message.getUtilitiesDeviceRegisterMessages()
                .forEach(bodyMessage -> {
                    if (bodyMessage.isValid()) {
                        createSecondChildServiceCall(serviceCall, bodyMessage, message.getDeviceId());
                    }
                });
    }

    private void createSecondChildServiceCall(ServiceCall parent, UtilitiesDeviceRegisterMessage bodyMessage, String deviceId) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.UTILITIES_DEVICE_REGISTER_CREATE_REQUEST);

        UtilitiesDeviceRegisterCreateRequestDomainExtension childDomainExtension = new UtilitiesDeviceRegisterCreateRequestDomainExtension();
        childDomainExtension.setDeviceId(deviceId);
        childDomainExtension.setLrn(bodyMessage.getLrn());
        childDomainExtension.setObis(bodyMessage.getObis());
        childDomainExtension.setInterval(bodyMessage.getInterval());
        childDomainExtension.setStartDate(bodyMessage.getStartDate());
        childDomainExtension.setEndDate(bodyMessage.getEndDate());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(deviceId).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }

    private void sendProcessError(UtilitiesDeviceRegisterCreateRequestMessage message, MessageSeeds messageSeed) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format());
        UtilitiesDeviceRegisterCreateConfirmationMessage confirmationMessage = null;
        confirmationMessage =
                UtilitiesDeviceRegisterCreateConfirmationMessage.builder()
                        .from(message, messageSeed, clock.instant())
                        .build();
        sendMessage(confirmationMessage, message.isBulk());
    }

    private void sendMessage(UtilitiesDeviceRegisterCreateConfirmationMessage confirmationMessage, boolean bulk) {
        if (bulk) {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_BULK_CREATE_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        } else {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_CREATE_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        }
    }

    private boolean hasUtilDeviceRegisterRequestServiceCall(String id) {
        Optional<DataModel> dataModel = ormService.getDataModel(MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.MODEL_NAME);
        if (dataModel.isPresent()) {
            return dataModel.get().stream(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class)
                    .anyMatch(where(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName()).isEqualTo(id));
        }
        return false;
    }


}
