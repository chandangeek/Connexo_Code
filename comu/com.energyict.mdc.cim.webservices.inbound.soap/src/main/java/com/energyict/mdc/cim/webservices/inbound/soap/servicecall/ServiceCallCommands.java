/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterInfo;

import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

public class ServiceCallCommands {

    public enum ServiceCallTypes {
        MASTER_METER_CONFIG("MeterConfigMasterServiceCallHandler", "v1.0"),
        METER_CONFIG("MeterConfigServiceCallHandler", "v1.0");

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
    private final JsonService jsonService;

    @Inject
    public ServiceCallCommands(ServiceCallService serviceCallService, JsonService jsonService, Thesaurus thesaurus) {
        this.serviceCallService = serviceCallService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
    }

    @TransactionRequired
    public ServiceCall createMeterConfigMasterServiceCall(MeterConfig meterConfig, EndPointConfiguration outboundEndPointConfiguration) {
        ServiceCallType serviceCallType = getServiceCallType(true);

        MeterConfigMasterDomainExtension meterConfigMasterDomainExtension = new MeterConfigMasterDomainExtension();
        meterConfigMasterDomainExtension.setActualNumberOfSuccessfulCalls(new BigDecimal(0));
        meterConfigMasterDomainExtension.setActualNumberOfFailedCalls(new BigDecimal(0));
        meterConfigMasterDomainExtension.setExpectedNumberOfCalls(BigDecimal.valueOf(meterConfig.getMeter().size()));
        meterConfigMasterDomainExtension.setCallbackURL(outboundEndPointConfiguration.getUrl());

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall().origin("MultiSense").extendedWith(meterConfigMasterDomainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();

        meterConfig.getMeter().forEach(meter -> createMeterConfigChildCall(parentServiceCall, meter, meterConfig.getSimpleEndDeviceFunction()));

        return parentServiceCall;
    }

    private ServiceCall createMeterConfigChildCall(ServiceCall parent, Meter meter,  List<SimpleEndDeviceFunction> simpleEndDeviceFunction) {
        ServiceCallType serviceCallType = getServiceCallType(false);

        MeterConfigDomainExtension meterConfigDomainExtension = new MeterConfigDomainExtension();
        meterConfigDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
        MeterInfo meterInfo = new MeterInfo(meter, simpleEndDeviceFunction);
        meterConfigDomainExtension.setMeter(jsonService.serialize(meterInfo));
        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType).extendedWith(meterConfigDomainExtension);
//        todo mlevan take into account update meter
//        if (device.isPresent()) {
//            serviceCallBuilder.targetObject(device.get());
//        }
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

    private ServiceCallType getServiceCallType(boolean master) {
        ServiceCallTypes serviceCallType = master ? ServiceCallTypes.MASTER_METER_CONFIG : ServiceCallTypes.METER_CONFIG;
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
    }
}
