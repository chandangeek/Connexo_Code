/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.FailedLinkageOperation;
import com.elster.jupiter.cim.webservices.inbound.soap.LinkageOperation;
import com.elster.jupiter.cim.webservices.inbound.soap.MasterDataLinkageAction;
import com.elster.jupiter.cim.webservices.inbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ObjectHolder;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.MeterInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.UsagePointInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MeterConfig
 */
public class MasterDataLinkageConfigMasterServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MasterDataLinkageConfigMasterServiceCallHandler";
    public static final String VERSION = "v1.0";

    private final EndPointConfigurationService endPointConfigurationService;
    private final ObjectHolder<ReplyMasterDataLinkageConfigWebService> replyMasterDataLinkageConfigWebServiceHolder;
    private final JsonService jsonService;

    @Inject
    public MasterDataLinkageConfigMasterServiceCallHandler(EndPointConfigurationService endPointConfigurationService,
            ObjectHolder<ReplyMasterDataLinkageConfigWebService> replyMasterDataLinkageConfigWebServiceHolder,
            JsonService jsonService) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.replyMasterDataLinkageConfigWebServiceHolder = replyMasterDataLinkageConfigWebServiceHolder;
        this.jsonService = jsonService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
        case ONGOING:
            serviceCall.findChildren().stream().forEach(child -> child.requestTransition(DefaultState.PENDING));
            break;
        case SUCCESSFUL:
            sendResponseToOutboundEndPoint(serviceCall);
            break;
        case FAILED:
            sendResponseToOutboundEndPoint(serviceCall);
            break;
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
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState,
            DefaultState newState) {
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

    private void updateCounter(ServiceCall serviceCall, DefaultState state) {
        MasterDataLinkageConfigMasterDomainExtension extension = serviceCall
                .getExtension(MasterDataLinkageConfigMasterDomainExtension.class)
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

    private void sendResponseToOutboundEndPoint(ServiceCall serviceCall) {
        if (replyMasterDataLinkageConfigWebServiceHolder.getObject() == null) {
            return;
        }
        MasterDataLinkageConfigMasterDomainExtension extension = serviceCall
                .getExtension(MasterDataLinkageConfigMasterDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        if (extension.getCallbackURL() == null) {
            return;
        }
        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService
                .findEndPointConfigurations().find().stream().filter(EndPointConfiguration::isActive)
                .filter(epc -> !epc.isInbound()).filter(epc -> epc.getUrl().equals(extension.getCallbackURL()))
                .findAny();
        if (!endPointConfiguration.isPresent()) {
            return;
        }

        ServiceCall child = serviceCall.findChildren().stream().findFirst().get();
        MasterDataLinkageConfigDomainExtension extensionForChild = child
                .getExtension(MasterDataLinkageConfigDomainExtension.class).get();
        MasterDataLinkageAction action = MasterDataLinkageAction.valueOf(extensionForChild.getOperation());
        replyMasterDataLinkageConfigWebServiceHolder.getObject().call(endPointConfiguration.get(), action,
                getSuccessfulLinkages(serviceCall), getFailedLinkages(serviceCall),
                extension.getExpectedNumberOfCalls());
    }

    private List<FailedLinkageOperation> getFailedLinkages(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.FAILED))
                .map(child -> {
                    MasterDataLinkageConfigDomainExtension extension = child
                            .getExtension(MasterDataLinkageConfigDomainExtension.class).get();
                    MeterInfo meter = jsonService.deserialize(extension.getMeter(), MeterInfo.class);
                    UsagePointInfo usagePoint = jsonService.deserialize(extension.getUsagePoint(),
                            UsagePointInfo.class);
                    FailedLinkageOperation failedLinkageOperation = new FailedLinkageOperation();
                    failedLinkageOperation.setErrorCode(extension.getErrorCode());
                    failedLinkageOperation.setErrorMessage(extension.getErrorMessage());
                    failedLinkageOperation.setMeterMrid(meter.getMrid());
                    failedLinkageOperation.setMeterName(meter.getName());
                    failedLinkageOperation.setUsagePointMrid(usagePoint.getMrid());
                    failedLinkageOperation.setUsagePointName(usagePoint.getName());
                    return failedLinkageOperation;
                }).collect(Collectors.toList());
    }

    private List<LinkageOperation> getSuccessfulLinkages(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.SUCCESSFUL))
                .map(child -> {
                    MasterDataLinkageConfigDomainExtension extension = child
                            .getExtension(MasterDataLinkageConfigDomainExtension.class).get();
                    MeterInfo meter = jsonService.deserialize(extension.getMeter(), MeterInfo.class);
                    UsagePointInfo usagePoint = jsonService.deserialize(extension.getUsagePoint(),
                            UsagePointInfo.class);
                    LinkageOperation linkageOperation = new FailedLinkageOperation();
                    linkageOperation.setMeterMrid(meter.getMrid());
                    linkageOperation.setMeterName(meter.getName());
                    linkageOperation.setUsagePointMrid(usagePoint.getMrid());
                    linkageOperation.setUsagePointName(usagePoint.getName());
                    return linkageOperation;
                }).collect(Collectors.toList());
    }

}
