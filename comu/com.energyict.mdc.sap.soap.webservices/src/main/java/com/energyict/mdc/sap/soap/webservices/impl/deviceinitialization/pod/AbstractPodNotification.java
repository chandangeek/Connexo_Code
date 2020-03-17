/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.pod;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterPodNotificationDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.PodNotificationDomainExtension;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class AbstractPodNotification extends AbstractInboundEndPoint implements ApplicationSpecific {
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final ServiceCallCommands serviceCallCommands;

    @Inject
    public AbstractPodNotification(ServiceCallService serviceCallService, Thesaurus thesaurus, ServiceCallCommands serviceCallCommands) {
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    void handleMessage(PodNotificationMessage podNotificationMessage) {
        SetMultimap<String, String> values = HashMultimap.create();
        podNotificationMessage
                .getPodMessages()
                .forEach(podMessage -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), podMessage.getDeviceId()));

        saveRelatedAttributes(values);
        createServiceCallAndTransition(podNotificationMessage);
    }

    private void createServiceCallAndTransition(PodNotificationMessage podNotificationMessage) {
        if (podNotificationMessage.isValid()) {
            if (hasOpenUtilDeviceRequestServiceCall(podNotificationMessage.getRequestId(), podNotificationMessage.getUuid())) {
                logWarning(MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_POD_NOTIFICATION).ifPresent(serviceCallType -> {
                    createServiceCall(serviceCallType, podNotificationMessage);
                });
            }
        } else {
            logWarning(MessageSeeds.INVALID_MESSAGE_FORMAT, podNotificationMessage.getMissingFields());
        }
    }

    private void createServiceCall(ServiceCallType serviceCallType, PodNotificationMessage podNotificationMessage) {
        MasterPodNotificationDomainExtension masterDomainExtension =
                new MasterPodNotificationDomainExtension();
        masterDomainExtension.setRequestID(podNotificationMessage.getRequestId());
        masterDomainExtension.setUuid(podNotificationMessage.getUuid());
        masterDomainExtension.setBulk(podNotificationMessage.isBulk());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterDomainExtension)
                .create();

        podNotificationMessage.getPodMessages()
                .forEach(podMessage -> {
                    if (podMessage.isValid()) {
                        createChildServiceCall(serviceCall, podMessage);
                    }
                });

        if (!serviceCall.findChildren().paged(0, 0).find().isEmpty()) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
            logWarning(MessageSeeds.INVALID_MESSAGE_FORMAT, podNotificationMessage.getMissingFields());
        }
    }

    private void createChildServiceCall(ServiceCall parent, PodMessage podMessage) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.POD_NOTIFICATION);

        PodNotificationDomainExtension childDomainExtension = new PodNotificationDomainExtension();
        childDomainExtension.setDeviceId(podMessage.getDeviceId());
        childDomainExtension.setPodId(podMessage.getPodId());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);

        serviceCallBuilder.create();
    }

    private void logWarning(MessageSeeds messageSeed, Object... messageSeedArgs) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format(messageSeedArgs));
    }

    private boolean hasOpenUtilDeviceRequestServiceCall(String id, String uuid) {
        return findAvailableOpenServiceCalls(ServiceCallTypes.MASTER_POD_NOTIFICATION)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterPodNotificationDomainExtension.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(domainExtension -> {
                    if (id != null) {
                        return domainExtension.getRequestID() != null && domainExtension.getRequestID().equals(id);
                    } else {
                        return domainExtension.getUuid() != null && domainExtension.getUuid().equals(uuid);
                    }
                });
    }

    private Finder<ServiceCall> findAvailableOpenServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        Arrays.stream(DefaultState.values()).filter(DefaultState::isOpen).map(DefaultState::name).forEach(filter.states::add);
        return serviceCallService.getServiceCallFinder(filter);
    }
}
