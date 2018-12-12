/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl.servicecall;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.prepayment.impl.BreakerStatus;
import com.elster.jupiter.prepayment.impl.ContactorInfo;
import com.elster.jupiter.prepayment.impl.MessageSeeds;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;

/**
 * @author sva
 * @since 31/03/2016 - 9:45
 */
public class ServiceCallCommands {

    public enum ServiceCallTypes {
        disconnect("redkneeDisconnectHandler", "v1.0"),
        connectWithLoadLimit("redkneeConnectWithLoadLimitHandler", "v1.0"),
        connectWithoutLoadLimit("redkneeConnectHandler", "v1.0"),
        armWithLoadLimit("redkneeArmWithLoadLimitHandler", "v1.0"),
        armWithoutLoadLimit("redkneeArmHandler", "v1.0"),;

        private final String typeName;
        private final String typeVersion;

        ServiceCallTypes(String typeName, String typeVersion) {
            this.typeName = typeName;
            this.typeVersion = typeVersion;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getTypeVersion() {
            return typeVersion;
        }
    }

    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;

    private ServiceCallType serviceCallType;

    @Inject
    public ServiceCallCommands(ServiceCallService serviceCallService, Thesaurus thesaurus) {
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
    }

    @TransactionRequired
    public ServiceCall createContactorOperationServiceCall(Optional<UsagePoint> usagePoint, ContactorInfo contactorInfo) {
        ServiceCallType serviceCallType = getServiceCallType(contactorInfo);

        ContactorOperationDomainExtension contactorOperationDomainExtension = new ContactorOperationDomainExtension();
        contactorOperationDomainExtension.setBreakerStatus(contactorInfo.status);
        contactorOperationDomainExtension.setCallback(contactorInfo.callback);
        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall().origin("Redknee").extendedWith(contactorOperationDomainExtension);
        if (usagePoint.isPresent()) {
            serviceCallBuilder.targetObject(usagePoint.get());
        }
        return serviceCallBuilder.create();
    }

    /**
     * Reject the given ServiceCall<br/>
     * Note: the ServiceCall should be in an appropriate state from which it can transit to either REJECTED or FAILED,
     * meaning it should be either in state CREATED or ONGOING.
     *
     * @param serviceCall
     * @param message
     */
    @TransactionRequired
    public void rejectServiceCall(ServiceCall serviceCall, String message) {
        serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Service call has failed: {0}", message));
        if (serviceCall.canTransitionTo(DefaultState.REJECTED)) {
            requestTransition(serviceCall, DefaultState.REJECTED);
        } else {
            requestTransition(serviceCall, DefaultState.FAILED);
        }
    }

    @TransactionRequired
    public void requestTransition(ServiceCall serviceCall, DefaultState newState) {
        serviceCall.requestTransition(newState);
    }

    private ServiceCallType getServiceCallType(ContactorInfo contactorInfo) {
        if (serviceCallType == null) {
            ServiceCallTypes serviceCallType = getServiceCallTypesFor(contactorInfo);
            this.serviceCallType = serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                    .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                            .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
        }
        return serviceCallType;
    }

    private ServiceCallTypes getServiceCallTypesFor(ContactorInfo contactorInfo) {
        if (BreakerStatus.connected.equals(contactorInfo.status)) {
            return (contactorInfo.loadLimit == null || contactorInfo.loadLimit.shouldDisableLoadLimit()) ? ServiceCallTypes.connectWithoutLoadLimit : ServiceCallTypes.connectWithLoadLimit;
        }
        if (BreakerStatus.armed.equals(contactorInfo.status)) {
            return (contactorInfo.loadLimit == null || contactorInfo.loadLimit.shouldDisableLoadLimit()) ? ServiceCallTypes.armWithoutLoadLimit : ServiceCallTypes.armWithLoadLimit;
        }
        return ServiceCallTypes.disconnect;
    }
}