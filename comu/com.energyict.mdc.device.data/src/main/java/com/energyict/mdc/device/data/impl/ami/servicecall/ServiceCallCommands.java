/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall;

import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.ArmServiceCallHandler;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.ConnectServiceCallHandler;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.DisableLoadLimitServiceCallHandler;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.DisconnectServiceCallHandler;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.EnableLoadLimitServiceCallHandler;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author sva
 * @since 31/03/2016 - 9:45
 */
public class ServiceCallCommands {

    public enum ServiceCallTypeMapping {
        connectBreaker(ConnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ConnectServiceCallHandler.VERSION, EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH),
        disconnectBreaker(DisconnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME, DisconnectServiceCallHandler.VERSION, EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH),
        armBreaker(ArmServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ArmServiceCallHandler.VERSION, EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_CLOSURE),

        loadControlInitiate(EnableLoadLimitServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ArmServiceCallHandler.VERSION, EndDeviceControlTypeMapping.LOAD_CONTROL_INITIATE),
        loadControlTerminate(DisableLoadLimitServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ArmServiceCallHandler.VERSION, EndDeviceControlTypeMapping.LOAD_CONTROL_TERMINATE),;

        private final String typeName;
        private final String typeVersion;
        private final EndDeviceControlTypeMapping endDeviceControlTypeMapping;

        ServiceCallTypeMapping(String typeName, String typeVersion, EndDeviceControlTypeMapping endDeviceControlTypeMapping) {
            this.typeName = typeName;
            this.typeVersion = typeVersion;
            this.endDeviceControlTypeMapping = endDeviceControlTypeMapping;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getTypeVersion() {
            return typeVersion;
        }

        public EndDeviceControlTypeMapping getEndDeviceControlTypeMapping() {
            return endDeviceControlTypeMapping;
        }

        static Optional<ServiceCallTypeMapping> getServiceCallTypeMappingFor(EndDeviceControlType endDeviceControlType) {
            return Arrays.asList(values()).stream()
                    .filter(mapping -> mapping.getEndDeviceControlTypeMapping().getEndDeviceControlTypeMRID().equals(endDeviceControlType.getMRID()))
                    .findFirst();
        }
    }

    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;

    private Map<EndDeviceControlType, ServiceCallType> serviceCallTypes;

    public ServiceCallCommands(ServiceCallService serviceCallService, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.serviceCallService = serviceCallService;
    }

    @TransactionRequired
    public ServiceCall createOperationServiceCall(Optional<ServiceCall> parentServiceCall, Device device, EndDeviceControlType endDeviceControlType, Instant releaseDate) {
        CompletionOptionsServiceCallDomainExtension completionOptionsServiceCallDomainExtension = new CompletionOptionsServiceCallDomainExtension();
        CommandServiceCallDomainExtension commandServiceCallDomainExtension = new CommandServiceCallDomainExtension();
        commandServiceCallDomainExtension.setCommandOperationStatus(CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES);
        commandServiceCallDomainExtension.setReleaseDate(releaseDate);

        ServiceCallType serviceCallType = getServiceCallType(endDeviceControlType);
        ServiceCallBuilder serviceCallBuilder = parentServiceCall.isPresent() ? parentServiceCall.get().newChildCall(serviceCallType) : serviceCallType.newServiceCall();
        serviceCallBuilder.targetObject(device);
        serviceCallBuilder.extendedWith(completionOptionsServiceCallDomainExtension);
        serviceCallBuilder.extendedWith(commandServiceCallDomainExtension);
        return serviceCallBuilder.create();
    }

    private ServiceCallType getServiceCallType(EndDeviceControlType endDeviceControlType) {
        if (!getServiceCallTypesMapping().containsKey(endDeviceControlType)) {
            ServiceCallTypeMapping serviceCallTypeMapping = ServiceCallTypeMapping.getServiceCallTypeMappingFor(endDeviceControlType).orElseThrow(IllegalStateException::new);
            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion())
                    .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                            .format(serviceCallTypeMapping.getTypeName(), serviceCallTypeMapping.getTypeVersion())));
            getServiceCallTypesMapping().put(endDeviceControlType, serviceCallType);
        }
        return getServiceCallTypesMapping().get(endDeviceControlType);
    }

    public Map<EndDeviceControlType, ServiceCallType> getServiceCallTypesMapping() {
        if (serviceCallTypes == null) {
            serviceCallTypes = new HashMap<>();
        }
        return serviceCallTypes;
    }
}