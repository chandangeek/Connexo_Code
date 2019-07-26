/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl.servicecall;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.processes.keyrenewal.api.impl.Command;
import com.energyict.mdc.processes.keyrenewal.api.impl.DeviceCommandInfo;
import com.energyict.mdc.processes.keyrenewal.api.impl.MessageSeeds;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;

public class ServiceCallCommands {

    public enum ServiceCallTypes {
        RENEW_KEY("FlowRenewDeviceKeyHandler", "v1.0"),
        TEST_COMMUNICATION("FlowTestDeviceCommunication", "v1.0");

        private final String typeName;
        private final String typeVersion;
        private static final String APPLICATION = "MDC";

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

        public String getApplication() {
            return APPLICATION;
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
    public ServiceCall createRenewKeyServiceCall(Optional<Device> device, DeviceCommandInfo deviceCommandInfo) {
        ServiceCallType serviceCallType = getServiceCallType(deviceCommandInfo);

        KeyRenewalDomainExtension keyRenewalDomainExtension = new KeyRenewalDomainExtension();
        keyRenewalDomainExtension.setCallbackSuccess(deviceCommandInfo.callbackSuccess);
        keyRenewalDomainExtension.setCallbackError(deviceCommandInfo.callbackError);
        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall().origin("Flow").extendedWith(keyRenewalDomainExtension);
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

    private ServiceCallType getServiceCallType(DeviceCommandInfo deviceCommandInfo) {
        if (serviceCallType == null) {
            ServiceCallTypes serviceCallType = getServiceCallTypesFor(deviceCommandInfo);
            this.serviceCallType = serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                    .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                            .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
        }
        return serviceCallType;
    }

    private ServiceCallTypes getServiceCallTypesFor(DeviceCommandInfo deviceCommandInfo) {
        if (deviceCommandInfo.command == Command.RENEW_KEY) {
            return ServiceCallTypes.RENEW_KEY;
        }
        return ServiceCallTypes.TEST_COMMUNICATION;
    }
}