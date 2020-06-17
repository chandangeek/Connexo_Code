/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.ObjectHolder;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.EndDeviceInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.parent.AbstractMasterServiceCallHandler;
import com.energyict.mdc.cim.webservices.outbound.soap.FailedLinkageOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.LinkageOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MasterDataLinkageConfig
 */
public class MasterDataLinkageConfigMasterServiceCallHandler extends
        AbstractMasterServiceCallHandler<MasterDataLinkageConfigMasterDomainExtension, ReplyMasterDataLinkageConfigWebService> {
    public static final String SERVICE_CALL_HANDLER_NAME = "MasterDataLinkageConfigMasterServiceCallHandler";
    public static final String APPLICATION = "MDC";
    public static final String VERSION = "v1.0";

    private final JsonService jsonService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;

    @Inject
    public MasterDataLinkageConfigMasterServiceCallHandler(EndPointConfigurationService endPointConfigurationService,
            ObjectHolder<ReplyMasterDataLinkageConfigWebService> replyMasterDataLinkageConfigWebServiceHolder,
            JsonService jsonService, MeteringService meteringService, DeviceService deviceService, Thesaurus thesaurus,
            WebServicesService webServicesService) {
        super(MasterDataLinkageConfigMasterDomainExtension.class, replyMasterDataLinkageConfigWebServiceHolder,
                endPointConfigurationService, thesaurus, webServicesService);
        this.jsonService = jsonService;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
    }

    @Override
    protected void sendReply(ReplyMasterDataLinkageConfigWebService replyWebService,
            EndPointConfiguration endPointConfiguration, ServiceCall serviceCall,
            MasterDataLinkageConfigMasterDomainExtension extension) {
        ServiceCall child = serviceCall.findChildren().stream().findFirst().get();
        MasterDataLinkageConfigDomainExtension extensionForChild = child
                .getExtension(MasterDataLinkageConfigDomainExtension.class).get();
        replyWebService.call(endPointConfiguration, extensionForChild.getOperation(),
                getSuccessfulLinkages(serviceCall), getFailedLinkages(serviceCall),
                extension.getExpectedNumberOfCalls(), extension.getCorrelationId());
    }

    private List<FailedLinkageOperation> getFailedLinkages(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.FAILED))
                .map(child -> createLinkageOperation(child, DefaultState.FAILED)).collect(Collectors.toList());
    }

    private List<LinkageOperation> getSuccessfulLinkages(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.SUCCESSFUL))
                .map(child -> createLinkageOperation(child, DefaultState.SUCCESSFUL)).collect(Collectors.toList());
    }

    private Optional<Meter> findMeter(MasterDataLinkageConfigDomainExtension extension) {
        MeterInfo meterInfo = jsonService.deserialize(extension.getMeter(), MeterInfo.class);
        if (meterInfo.getMrid() != null) {
            return meteringService.findMeterByMRID(meterInfo.getMrid());
        }
        return meteringService.findMeterByName(meterInfo.getName());
    }

    private Optional<UsagePoint> findUsagePoint(MasterDataLinkageConfigDomainExtension extension) {
        UsagePointInfo usagePointInfo = (extension.getUsagePoint() == null) ? null : jsonService.deserialize(extension.getUsagePoint(), UsagePointInfo.class);
        if (usagePointInfo == null) {
            return Optional.empty();
        }
        if (usagePointInfo.getMrid() != null) {
            return meteringService.findUsagePointByMRID(usagePointInfo.getMrid());
        }
        return meteringService.findUsagePointByName(usagePointInfo.getName());
    }

    private Optional<Device> findEndDevice(MasterDataLinkageConfigDomainExtension extension) {
        EndDeviceInfo endDeviceInfo = (extension.getEndDevice() == null) ? null : jsonService.deserialize(extension.getEndDevice(), EndDeviceInfo.class);
        if (endDeviceInfo == null) {
            return Optional.empty();
        }
        if (endDeviceInfo.getMrid() != null) {
            return deviceService.findDeviceByMrid(endDeviceInfo.getMrid());
        }
        return deviceService.findDeviceByName(endDeviceInfo.getName());
    }

    private FailedLinkageOperation createLinkageOperation(ServiceCall child, DefaultState state){

        MasterDataLinkageConfigDomainExtension extension = child.getExtension(MasterDataLinkageConfigDomainExtension.class).get();
        Optional<Meter> meter = findMeter(extension);
        Optional<UsagePoint> usagePoint = findUsagePoint(extension);
        Optional<Device> endDevice = findEndDevice(extension);
        FailedLinkageOperation linkageOperation = new FailedLinkageOperation();

        if(state.equals(DefaultState.FAILED)){
            linkageOperation.setErrorMessage(extension.getErrorMessage());
            linkageOperation.setErrorCode(extension.getErrorCode());
        }
        if(meter.isPresent()){
            linkageOperation.setMeterMrid(meter.get().getMRID());
            linkageOperation.setMeterName(meter.get().getName());
        }
        if(usagePoint.isPresent()){
            linkageOperation.setUsagePointMrid(usagePoint.get().getMRID());
            linkageOperation.setUsagePointName(usagePoint.get().getName());
        }
        if(endDevice.isPresent()){
            linkageOperation.setEndDeviceMrid(endDevice.get().getmRID());
            linkageOperation.setEndDeviceName(endDevice.get().getName());
        }

        return linkageOperation;
    }

}
