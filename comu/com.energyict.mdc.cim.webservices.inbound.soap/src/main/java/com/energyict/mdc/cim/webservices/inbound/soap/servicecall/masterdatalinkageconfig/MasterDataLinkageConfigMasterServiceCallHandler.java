/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.EndDeviceInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.elster.jupiter.cim.webservices.outbound.soap.FailedLinkageOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.LinkageOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MasterDataLinkageConfig
 */
public class MasterDataLinkageConfigMasterServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MasterDataLinkageConfigMasterServiceCallHandler";
    public static final String APPLICATION = "MDC";
    public static final String VERSION = "v1.0";

    private final ReplyMasterDataLinkageConfigWebService replyWebService;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;
    private final WebServicesService webServicesService;
    private final JsonService jsonService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;

    @Inject
    public MasterDataLinkageConfigMasterServiceCallHandler(EndPointConfigurationService endPointConfigurationService,
            ReplyMasterDataLinkageConfigWebService replyMasterDataLinkageConfigWebService,
            JsonService jsonService, MeteringService meteringService, DeviceService deviceService, Thesaurus thesaurus,
            WebServicesService webServicesService) {
        this.replyWebService = replyMasterDataLinkageConfigWebService;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
        this.webServicesService = webServicesService;
        this.jsonService = jsonService;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
    }

    @Override
    public final void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case ONGOING:
                serviceCall.findChildren().stream().forEach(child -> child.requestTransition(DefaultState.PENDING));
                break;
            case SUCCESSFUL:
            case FAILED:
            case PARTIAL_SUCCESS:
                sendResponseToOutboundEndPoint(serviceCall);
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public final void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall,
                                         DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case SUCCESSFUL:
            case FAILED:
                updateCounter(parentServiceCall, newState);
                break;
            case CANCELLED:
            case REJECTED:
            default:
                // No specific action required for these states
                break;
        }
    }

    private void sendResponseToOutboundEndPoint(ServiceCall serviceCall) {
        MasterDataLinkageConfigMasterDomainExtension extension = serviceCall.getExtension(MasterDataLinkageConfigMasterDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        if (extension.getCallbackURL() == null) {
            return;
        }
        if (replyWebService == null) {
            logErrorAboutMissingEndpoint(serviceCall, extension);
            return;
        }
        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService
                .findEndPointConfigurations().find().stream().filter(EndPointConfiguration::isActive)
                .filter(epc -> !epc.isInbound()).filter(epc -> epc.getUrl().equals(extension.getCallbackURL()))
                .findAny();
        if (!endPointConfiguration.isPresent()) {
            logErrorAboutMissingEndpoint(serviceCall, extension);
            return;
        }
        if (!webServicesService.isPublished(endPointConfiguration.get())) {
            webServicesService.publishEndPoint(endPointConfiguration.get());
        }
        if (!webServicesService.isPublished(endPointConfiguration.get())) {
            serviceCall.log(LogLevel.SEVERE,
                    MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL.translate(thesaurus, extension.getCallbackURL()));
            return;
        }

        sendReply(replyWebService, endPointConfiguration.get(), serviceCall, extension);
    }

    private void logErrorAboutMissingEndpoint(ServiceCall serviceCall, MasterDataLinkageConfigMasterDomainExtension extension) {
        serviceCall.log(LogLevel.SEVERE,
                MessageSeeds.NO_END_POINT_WITH_URL.translate(thesaurus, extension.getCallbackURL()));
    }

    private void updateCounter(ServiceCall serviceCall, DefaultState state) {
        MasterDataLinkageConfigMasterDomainExtension extension = serviceCall.getExtension(MasterDataLinkageConfigMasterDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

        BigDecimal successfulCalls = extension.getActualNumberOfSuccessfulCalls();
        BigDecimal failedCalls = extension.getActualNumberOfFailedCalls();
        BigDecimal expectedCalls = extension.getExpectedNumberOfCalls();

        if (DefaultState.SUCCESSFUL.equals(state)) {
            successfulCalls = successfulCalls.add(BigDecimal.ONE);
            extension.setActualNumberOfSuccessfulCalls(successfulCalls);
        } else {
            failedCalls = failedCalls.add(BigDecimal.ONE);
            extension.setActualNumberOfFailedCalls(failedCalls);
        }
        serviceCall.update(extension);

        if (extension.getExpectedNumberOfCalls().compareTo(successfulCalls.add(failedCalls)) <= 0) {
            if (successfulCalls.compareTo(expectedCalls) >= 0 && serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (failedCalls.compareTo(expectedCalls) >= 0 && serviceCall.canTransitionTo(DefaultState.FAILED)) {
                serviceCall.requestTransition(DefaultState.FAILED);
            } else if (serviceCall.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
            }
        }
    }

    private void sendReply(ReplyMasterDataLinkageConfigWebService replyWebService,
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
        return Optional.ofNullable(extension.getUsagePoint()).flatMap(usagePoint -> {
            UsagePointInfo usagePointInfo = jsonService.deserialize(usagePoint, UsagePointInfo.class);
            if (usagePointInfo.getMrid() != null) {
                return meteringService.findUsagePointByMRID(usagePointInfo.getMrid());
            }
            return meteringService.findUsagePointByName(usagePointInfo.getName());
        });
    }

    private Optional<Device> findEndDevice(MasterDataLinkageConfigDomainExtension extension) {
        return Optional.ofNullable(extension.getEndDevice()).flatMap(endDevice -> {
            EndDeviceInfo endDeviceInfo = jsonService.deserialize(endDevice, EndDeviceInfo.class);
            if (endDeviceInfo.getMrid() != null) {
                return deviceService.findDeviceByMrid(endDeviceInfo.getMrid());
            }
            return deviceService.findDeviceByName(endDeviceInfo.getName());
        });
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
