package com.elster.jupiter.prepayment.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.prepayment.impl.BreakerStatus;
import com.elster.jupiter.prepayment.impl.ContactorInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

/**
 * @author sva
 * @since 31/03/2016 - 9:45
 */
public class ServiceCallCommands {

    private enum ServiceCallTypes {
        disconnect("redkneeDisconnectHandler", "v1.0"),
        connectWithLoadLimit("redkneeConnectWithLoadLimitHandler", "v1.0"),
        connectWithoutLoadLimit("redkneeConnectWithoutLoadLimitHandler", "v1.0"),
        armWithLoadLimit("redkneeArmWithLoadLimitHandler", "v1.0"),
        armWithoutLoadLimit("redkneeArmWithoutLoadLimitHandler", "v1.0"),;

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

    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    private ServiceCallType serviceCallType;

    @Inject
    public ServiceCallCommands(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    @TransactionRequired
    public ServiceCall createContactorOperationServiceCall(Optional<UsagePoint> usagePoint, Optional<Device> device, ContactorInfo contactorInfo) {
        ServiceCallType serviceCallType = getServiceCallType(contactorInfo);

        ContactorOperationDomainExtension contactorOperationDomainExtension = new ContactorOperationDomainExtension();
        contactorOperationDomainExtension.setmRIDUsagePoint(usagePoint.isPresent() ? usagePoint.get().getMRID() : "unknown");
        contactorOperationDomainExtension.setmRIDDevice(device.isPresent() ? device.get().getmRID() : "unknown");
        contactorOperationDomainExtension.setActivationDate(contactorInfo.activationDate != null ? contactorInfo.activationDate : Instant.now());
        contactorOperationDomainExtension.setBreakerStatus(contactorInfo.status);
        contactorOperationDomainExtension.setCallback(contactorInfo.callback);

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall().origin("Redknee").extendedWith(contactorOperationDomainExtension);
        if (device.isPresent()) {
            serviceCallBuilder.targetObject(device.get());
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
        serviceCall.log(LogLevel.SEVERE, message);
        if (serviceCall.canTransitionTo(DefaultState.REJECTED)) {
            requestTransition(serviceCall, DefaultState.REJECTED);
        } else {
            requestTransition(serviceCall, DefaultState.FAILED);
        }
    }

    @TransactionRequired
    public void requestTransition(ServiceCall serviceCall, DefaultState newState) {
        serviceCall.log(LogLevel.INFO, "Now entering state " + newState.getDefaultFormat());
        serviceCall.requestTransition(newState);
    }

    private ServiceCallType getServiceCallType(ContactorInfo contactorInfo) {
        if (serviceCallType == null) {
            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                    .stream()
                    .filter(cps -> cps.getCustomPropertySet()
                            .getName()
                            .equals(ContactorOperationCustomPropertySet.class.getSimpleName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find the Redknee contactor operation custom property set"));
            ServiceCallTypes serviceCallType = getServiceCallTypesFor(contactorInfo);
            this.serviceCallType = serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                    .orElseGet(() -> serviceCallService.createServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                            .handler("RedkneeNoOperationHandler")
                            .logLevel(LogLevel.FINEST)
                            .customPropertySet(customPropertySet)
                            .create());
        }
        return serviceCallType;
    }

    private ServiceCallTypes getServiceCallTypesFor(ContactorInfo contactorInfo) {
        if (contactorInfo.status.equals(BreakerStatus.connected)) {
            return (contactorInfo.loadLimit == null || contactorInfo.loadLimit.shouldDisableLoadLimit()) ? ServiceCallTypes.connectWithoutLoadLimit : ServiceCallTypes.connectWithLoadLimit;
        }
        if (contactorInfo.status.equals(BreakerStatus.armed)) {
            return (contactorInfo.loadLimit == null || contactorInfo.loadLimit.shouldDisableLoadLimit()) ? ServiceCallTypes.armWithoutLoadLimit : ServiceCallTypes.armWithLoadLimit;
        }
        return ServiceCallTypes.disconnect;
    }
}