/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import javax.inject.Inject;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents.EndDeviceEventsBuilder;
import com.energyict.mdc.cim.webservices.inbound.soap.getmeterconfig.GetMeterConfigFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.getmeterconfig.GetMeterConfigParser;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigParser;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents.GetEndDeviceEventsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents.GetEndDeviceEventsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents.GetEndDeviceEventsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig.GetMeterConfigItemCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig.GetMeterConfigItemDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig.GetMeterConfigItemServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig.GetMeterConfigMasterCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig.GetMeterConfigMasterDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig.GetMeterConfigMasterServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigServiceCallHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.google.common.collect.Range;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;

public class ServiceCallCommands {

    public enum ServiceCallTypes {
        MASTER_METER_CONFIG(MeterConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MeterConfigMasterServiceCallHandler.VERSION, MeterConfigMasterCustomPropertySet.class.getName()),
        METER_CONFIG(MeterConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MeterConfigServiceCallHandler.VERSION, MeterConfigCustomPropertySet.class.getName()),
        GET_END_DEVICE_EVENTS(GetEndDeviceEventsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, GetEndDeviceEventsServiceCallHandler.VERSION, GetEndDeviceEventsCustomPropertySet.class.getName()),
        MASTER_GET_METER_CONFIG(GetMeterConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME, GetMeterConfigMasterServiceCallHandler.VERSION, GetMeterConfigMasterCustomPropertySet.class.getName()),
        GET_METER_CONFIG(GetMeterConfigItemServiceCallHandler.SERVICE_CALL_HANDLER_NAME, GetMeterConfigItemServiceCallHandler.VERSION, GetMeterConfigItemCustomPropertySet.class.getName());

        private final String typeName;
        private final String typeVersion;
        private final String customPropertySetClass;

        ServiceCallTypes(String typeName, String typeVersion, String customPropertySetClass) {
            this.typeName = typeName;
            this.typeVersion = typeVersion;
            this.customPropertySetClass = customPropertySetClass;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getTypeVersion() {
            return typeVersion;
        }

        public String getCustomPropertySetClass() {
            return customPropertySetClass;
        }
    }

    private final DeviceService deviceService;
    private final JsonService jsonService;
    private final EndDeviceEventsBuilder endDeviceEventsBuilder;
    private final MeterConfigParser meterConfigParser;
    private final GetMeterConfigParser getMeterConfigParser;
    private final MeterConfigFaultMessageFactory meterConfigFaultMessageFactory;
    private final GetMeterConfigFaultMessageFactory getMeterConfigFaultMessageFactory;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;

    @Inject
    public ServiceCallCommands(DeviceService deviceService, JsonService jsonService,
                               MeterConfigParser meterConfigParser, MeterConfigFaultMessageFactory meterConfigFaultMessageFactory,
                               ServiceCallService serviceCallService, EndDeviceEventsBuilder endDeviceEventsBuilder,
                               Thesaurus thesaurus, GetMeterConfigParser getMeterConfigParser, GetMeterConfigFaultMessageFactory getMeterConfigFaultMessageFactory) {
        this.deviceService = deviceService;
        this.jsonService = jsonService;
        this.endDeviceEventsBuilder = endDeviceEventsBuilder;
        this.meterConfigParser = meterConfigParser;
        this.meterConfigFaultMessageFactory = meterConfigFaultMessageFactory;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.getMeterConfigParser = getMeterConfigParser;
        this.getMeterConfigFaultMessageFactory = getMeterConfigFaultMessageFactory;
    }

    @TransactionRequired
    public ServiceCall createMeterConfigMasterServiceCall(MeterConfig meterConfig, EndPointConfiguration outboundEndPointConfiguration,
                                                          OperationEnum operation) throws FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.MASTER_METER_CONFIG);

        MeterConfigMasterDomainExtension meterConfigMasterDomainExtension = new MeterConfigMasterDomainExtension();
        meterConfigMasterDomainExtension.setActualNumberOfSuccessfulCalls(new BigDecimal(0));
        meterConfigMasterDomainExtension.setActualNumberOfFailedCalls(new BigDecimal(0));
        meterConfigMasterDomainExtension.setExpectedNumberOfCalls(BigDecimal.valueOf(meterConfig.getMeter().size()));
        setCallBackUrl(meterConfigMasterDomainExtension, outboundEndPointConfiguration);

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(meterConfigMasterDomainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();

        for (Meter meter : meterConfig.getMeter()) {
            createMeterConfigChildCall(parentServiceCall, operation, meter, meterConfig.getSimpleEndDeviceFunction());
        }

        return parentServiceCall;
    }

    private void setCallBackUrl(MeterConfigMasterDomainExtension meterConfigMasterDomainExtension,
            EndPointConfiguration outboundEndPointConfiguration) {
        if (outboundEndPointConfiguration != null) {
            meterConfigMasterDomainExtension.setCallbackURL(outboundEndPointConfiguration.getUrl());
        }
    }

    private ServiceCall createMeterConfigChildCall(ServiceCall parent, OperationEnum operation,
                                                   Meter meter, List<SimpleEndDeviceFunction> simpleEndDeviceFunction) throws FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.METER_CONFIG);

        MeterConfigDomainExtension meterConfigDomainExtension = new MeterConfigDomainExtension();
        meterConfigDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
        MeterInfo meterInfo = meterConfigParser.asMeterInfo(meter, simpleEndDeviceFunction, operation);
        meterConfigDomainExtension.setMeter(jsonService.serialize(meterInfo));
        meterConfigDomainExtension.setOperation(operation.getOperation());
        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(meterConfigDomainExtension);
        if (operation == OperationEnum.UPDATE) {
            serviceCallBuilder.targetObject(findDevice(meterInfo));
        }
        return serviceCallBuilder.create();
    }

    @TransactionRequired
    public ServiceCall createGetEndDeviceEventsMasterServiceCall(List<ch.iec.tc57._2011.getenddeviceevents.Meter> meters,
                                                                 Range<Instant> interval, EndPointConfiguration outboundEndPointConfiguration)
            throws ch.iec.tc57._2011.getenddeviceevents.FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.GET_END_DEVICE_EVENTS);

        GetEndDeviceEventsDomainExtension domainExtension = new GetEndDeviceEventsDomainExtension();

        String meterIdentifiers = "";
        for (String identifier : endDeviceEventsBuilder.getMeterIdentifiers(meters)) {
            meterIdentifiers = identifier + "," + meterIdentifiers;
        }

        domainExtension.setMeter(meterIdentifiers);
        domainExtension.setFromDate(interval.lowerEndpoint());
        domainExtension.setToDate(interval.upperEndpoint());
        domainExtension.setCallbackURL(outboundEndPointConfiguration.getUrl());

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(domainExtension);
        return serviceCallBuilder.create();
    }

    @TransactionRequired
    public ServiceCall createGetMeterConfigMasterServiceCall(List<ch.iec.tc57._2011.getmeterconfig.Meter> meters,
                                                             EndPointConfiguration outboundEndPointConfiguration,
                                                             OperationEnum operation) throws ch.iec.tc57._2011.getmeterconfig.FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.MASTER_GET_METER_CONFIG);

        GetMeterConfigMasterDomainExtension domainExtension = new GetMeterConfigMasterDomainExtension();
        domainExtension.setActualNumberOfSuccessfulCalls(0l);
        domainExtension.setActualNumberOfFailedCalls(0l);
        domainExtension.setExpectedNumberOfCalls(new Long(meters.size()));
        domainExtension.setCallbackURL(outboundEndPointConfiguration.getUrl());

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(domainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();

        for (ch.iec.tc57._2011.getmeterconfig.Meter meter: meters) {
            createGetMeterConfigChildCall(parentServiceCall, operation, meter);
        }

        return parentServiceCall;
    }

    private ServiceCall createGetMeterConfigChildCall(ServiceCall parent, OperationEnum operation,
                                                      ch.iec.tc57._2011.getmeterconfig.Meter meter) throws ch.iec.tc57._2011.getmeterconfig.FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.GET_METER_CONFIG);

        GetMeterConfigItemDomainExtension domainExtension = new GetMeterConfigItemDomainExtension();
        domainExtension.setMeterMrid(meter.getMRID());
        String deviceName = getMeterConfigParser.extractName(meter.getNames()).orElse(null);
        domainExtension.setMeterName(deviceName);
        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(domainExtension);
        if (operation == OperationEnum.UPDATE) {
            serviceCallBuilder.targetObject(findDevice(meter.getMRID(), deviceName));
        }
        return serviceCallBuilder.create();
    }

    private Object findDevice(String mrid, String deviceName) throws ch.iec.tc57._2011.getmeterconfig.FaultMessage {
        if (mrid != null) {
            return deviceService.findDeviceByMrid(mrid)
                    .orElseThrow(getMeterConfigFaultMessageFactory.meterConfigFaultMessageSupplier(deviceName, MessageSeeds.NO_DEVICE_WITH_MRID, mrid));
        } else {
            return deviceService.findDeviceByName(deviceName)
                    .orElseThrow(getMeterConfigFaultMessageFactory.meterConfigFaultMessageSupplier(deviceName, MessageSeeds.NO_DEVICE_WITH_NAME, deviceName));
        }
    }

    @TransactionRequired
    public void requestTransition(ServiceCall serviceCall, DefaultState newState) {
        serviceCall.requestTransition(newState);
    }

    private ServiceCallType getServiceCallType(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
    }

    private Device findDevice(MeterInfo meterInfo) throws FaultMessage {
        if (meterInfo.getmRID() != null) {
            return deviceService.findDeviceByMrid(meterInfo.getmRID())
                    .orElseThrow(meterConfigFaultMessageFactory.meterConfigFaultMessageSupplier(meterInfo.getDeviceName(), MessageSeeds.NO_DEVICE_WITH_MRID, meterInfo.getmRID()));
        } else {
            return deviceService.findDeviceByName(meterInfo.getDeviceName())
                    .orElseThrow(meterConfigFaultMessageFactory.meterConfigFaultMessageSupplier(meterInfo.getDeviceName(), MessageSeeds.NO_DEVICE_WITH_NAME, meterInfo.getDeviceName()));
        }
    }
}
