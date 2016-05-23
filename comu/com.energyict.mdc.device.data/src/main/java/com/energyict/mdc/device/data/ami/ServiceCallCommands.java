package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;

public class ServiceCallCommands {

    protected enum ServiceCallTypes {
        arm("multisenseArmHandler", "v1.0"),
        disconnect("multiSenseDisconnectHandler", "v1.0"),
        connect("multiSenseConnectHandler", "v1.0"),
        enableLoadLimit("multisenseEnableLoadLimitHandler", "v1.0"),
        disableLoadLimit("multisenseDisableLoadLimitHandler", "v1.0");


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

   /* @TransactionRequired
    public ServiceCall createContactorOperationServiceCall(Optional<EndDevice> endDevice, ContactorInfo contactorInfo) {
        ServiceCallType serviceCallType = getServiceCallType(contactorInfo);

        ContactorOperationDomainExtension contactorOperationDomainExtension = new ContactorOperationDomainExtension();
        contactorOperationDomainExtension.setmRIDDevice(endDevice.isPresent() ? endDevice.get().getMRID() : "unknown");
        contactorOperationDomainExtension.setActivationDate(contactorInfo.activationDate != null ? contactorInfo.activationDate : Instant.now());
        contactorOperationDomainExtension.setBreakerStatus(contactorInfo.status);
        contactorOperationDomainExtension.setCallback(contactorInfo.callback);

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall().origin("MultiSense").extendedWith(contactorOperationDomainExtension);
        return serviceCallBuilder.create();
    }
*/
    /**
     * Reject the given ServiceCall<br/>
     * Note: the ServiceCall should be in an appropriate state from which it can transit to either REJECTED or FAILED,
     * meaning it should be either in state CREATED or ONGOING.
     *
     * @param serviceCall
     * @param message
     */
  /*  @TransactionRequired
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
                    .orElseThrow(() -> new IllegalStateException("Could not find the MultiSense contactor operation custom property set"));
            ServiceCallTypes serviceCallType = getServiceCallTypesFor(contactorInfo);
            this.serviceCallType = serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                    .orElseGet(() -> serviceCallService.createServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                            .handler("MultisenseNoOperationHandler")
                            .logLevel(LogLevel.FINEST)
                            .customPropertySet(customPropertySet)
                            .create());
        }
        return serviceCallType;
    }

    private ServiceCallTypes getServiceCallTypesFor(ContactorInfo contactorInfo) {
        if (BreakerStatus.CONNECTED.equals(contactorInfo.status)) {
            return contactorInfo.loadLimit == null ? ServiceCallTypes.connectWithoutLoadLimit : ServiceCallTypes.connectWithLoadLimit;
        }
        if (BreakerStatus.ARMED.equals(contactorInfo.status)) {
            return contactorInfo.loadLimit == null ? ServiceCallTypes.armWithoutLoadLimit : ServiceCallTypes.armWithLoadLimit;
        }
        return ServiceCallTypes.disconnect;
    } */
}