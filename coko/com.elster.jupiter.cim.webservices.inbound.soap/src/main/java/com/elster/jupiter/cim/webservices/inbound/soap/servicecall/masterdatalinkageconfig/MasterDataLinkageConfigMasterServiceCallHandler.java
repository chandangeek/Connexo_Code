/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.ObjectHolder;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.parent.AbstractMasterServiceCallHandler;
import com.elster.jupiter.cim.webservices.outbound.soap.FailedLinkageOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.LinkageOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MasterDataLinkageConfig
 */
public class MasterDataLinkageConfigMasterServiceCallHandler extends
        AbstractMasterServiceCallHandler<MasterDataLinkageConfigMasterDomainExtension, ReplyMasterDataLinkageConfigWebService> {
    public static final String SERVICE_CALL_HANDLER_NAME = "MasterDataLinkageConfigMasterServiceCallHandler";
    public static final String VERSION = "v1.0";

    private final JsonService jsonService;
    private final MeteringService meteringService;

    @Inject
    public MasterDataLinkageConfigMasterServiceCallHandler(EndPointConfigurationService endPointConfigurationService,
            ObjectHolder<ReplyMasterDataLinkageConfigWebService> replyMasterDataLinkageConfigWebServiceHolder,
            JsonService jsonService, MeteringService meteringService) {
        super(MasterDataLinkageConfigMasterDomainExtension.class, replyMasterDataLinkageConfigWebServiceHolder,
                endPointConfigurationService);
        this.jsonService = jsonService;
        this.meteringService = meteringService;
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
                extension.getExpectedNumberOfCalls());
    }

    private List<FailedLinkageOperation> getFailedLinkages(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.FAILED))
                .map(child -> {
                    MasterDataLinkageConfigDomainExtension extension = child
                            .getExtension(MasterDataLinkageConfigDomainExtension.class).get();
                    Meter meter = findMeter(extension);
                    UsagePoint usagePoint = findUsagePoint(extension);
                    FailedLinkageOperation failedLinkageOperation = new FailedLinkageOperation();
                    failedLinkageOperation.setErrorCode(extension.getErrorCode());
                    failedLinkageOperation.setErrorMessage(extension.getErrorMessage());
                    failedLinkageOperation.setMeterMrid(meter.getMRID());
                    failedLinkageOperation.setMeterName(meter.getName());
                    failedLinkageOperation.setUsagePointMrid(usagePoint.getMRID());
                    failedLinkageOperation.setUsagePointName(usagePoint.getName());
                    return failedLinkageOperation;
                }).collect(Collectors.toList());
    }

    private List<LinkageOperation> getSuccessfulLinkages(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.SUCCESSFUL))
                .map(child -> {
                    MasterDataLinkageConfigDomainExtension extension = child
                            .getExtension(MasterDataLinkageConfigDomainExtension.class).get();
                    Meter meter = findMeter(extension);
                    UsagePoint usagePoint = findUsagePoint(extension);
                    LinkageOperation linkageOperation = new FailedLinkageOperation();
                    linkageOperation.setMeterMrid(meter.getMRID());
                    linkageOperation.setMeterName(meter.getName());
                    linkageOperation.setUsagePointMrid(usagePoint.getMRID());
                    linkageOperation.setUsagePointName(usagePoint.getName());
                    return linkageOperation;
                }).collect(Collectors.toList());
    }

    private Meter findMeter(MasterDataLinkageConfigDomainExtension extension) {
        MeterInfo meterInfo = jsonService.deserialize(extension.getMeter(), MeterInfo.class);
        if (meterInfo.getMrid() != null) {
            return meteringService.findMeterByMRID(meterInfo.getMrid()).get();
        }
        return meteringService.findMeterByName(meterInfo.getName()).get();
    }

    private UsagePoint findUsagePoint(MasterDataLinkageConfigDomainExtension extension) {
        UsagePointInfo usagePointInfo = jsonService.deserialize(extension.getUsagePoint(), UsagePointInfo.class);
        if (usagePointInfo.getMrid() != null) {
            return meteringService.findUsagePointByMRID(usagePointInfo.getMrid()).get();
        }
        return meteringService.findUsagePointByName(usagePointInfo.getName()).get();
    }

}
