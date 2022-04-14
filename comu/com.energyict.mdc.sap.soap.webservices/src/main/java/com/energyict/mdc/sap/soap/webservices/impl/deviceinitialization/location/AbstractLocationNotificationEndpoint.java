/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.location;

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

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceLocationNotificationDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceLocationNotificationDomainExtension;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class AbstractLocationNotificationEndpoint extends AbstractInboundEndPoint implements ApplicationSpecific {
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final ServiceCallCommands serviceCallCommands;

    @Inject
    public AbstractLocationNotificationEndpoint(ServiceCallService serviceCallService, Thesaurus thesaurus, ServiceCallCommands serviceCallCommands) {
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

    void handleMessage(LocationNotificationMessage locationNotificationMessage) {
        SetMultimap<String, String> values = HashMultimap.create();
        locationNotificationMessage
                .getLocationMessages()
                .forEach(locationMessage -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), locationMessage.getDeviceId()));

        saveRelatedAttributes(values);
        createServiceCallAndTransition(locationNotificationMessage);
    }

    private void createServiceCallAndTransition(LocationNotificationMessage locationNotificationMessage) {
        if (locationNotificationMessage.isValid()) {
            if (hasOpenUtilDeviceRequestServiceCall(locationNotificationMessage.getRequestId(), locationNotificationMessage.getUuid())) {
                logWarning(MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_UTILITIES_DEVICE_LOCATION_NOTIFICATION).ifPresent(serviceCallType -> {
                    createServiceCall(serviceCallType, locationNotificationMessage);
                });
            }
        } else {
            logWarning(MessageSeeds.INVALID_MESSAGE_FORMAT, locationNotificationMessage.getMissingFields());
        }
    }

    private void createServiceCall(ServiceCallType serviceCallType, LocationNotificationMessage locationNotificationMessage) {
        MasterUtilitiesDeviceLocationNotificationDomainExtension masterDomainExtension =
                new MasterUtilitiesDeviceLocationNotificationDomainExtension();
        masterDomainExtension.setRequestID(locationNotificationMessage.getRequestId());
        masterDomainExtension.setUuid(locationNotificationMessage.getUuid());
        masterDomainExtension.setBulk(locationNotificationMessage.isBulk());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterDomainExtension)
                .create();

        locationNotificationMessage.getLocationMessages()
                .forEach(locationMessage -> {
                    if (locationMessage.isValid()) {
                        createChildServiceCall(serviceCall, locationMessage);
                    }
                });

        if (!serviceCall.findChildren().paged(0, 0).find().isEmpty()) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
            logWarning(MessageSeeds.INVALID_MESSAGE_FORMAT, locationNotificationMessage.getMissingFields());
        }
    }

    private void createChildServiceCall(ServiceCall parent, LocationMessage locationMessage) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.UTILITIES_DEVICE_LOCATION_NOTIFICATION);
        UtilitiesDeviceLocationNotificationDomainExtension childDomainExtension = new UtilitiesDeviceLocationNotificationDomainExtension();
        childDomainExtension.setDeviceId(locationMessage.getDeviceId());
        childDomainExtension.setLocationId(locationMessage.getLocationId());
        childDomainExtension.setInstallationNumber(locationMessage.getInstallationNumber());
        childDomainExtension.setPod(locationMessage.getPod());
        childDomainExtension.setDivisionCategoryCode(locationMessage.getDivisionCategoryCode());
        childDomainExtension.setLocationInformation(locationMessage.getLocationIdInformation());
        childDomainExtension.setModificationInformationInformation(locationMessage.getModificationInformation());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);

        serviceCallBuilder.create();
    }

    private void logWarning(MessageSeeds messageSeed, Object... messageSeedArgs) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format(messageSeedArgs));
    }

    private boolean hasOpenUtilDeviceRequestServiceCall(String id, String uuid) {
        return findAvailableOpenServiceCalls(ServiceCallTypes.MASTER_UTILITIES_DEVICE_LOCATION_NOTIFICATION)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterUtilitiesDeviceLocationNotificationDomainExtension.class))
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
