/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.ObjectHolder;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.Action;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.UsagePointBuilder;
import com.elster.jupiter.cim.webservices.outbound.soap.FailedUsagePointOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.ReplyUsagePointConfigWebService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.usagepointconfig.Name;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS UsagePointConfig
 */
public class UsagePointConfigMasterServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "UsagePointConfigMasterServiceCallHandler";
    public static final String VERSION = "v1.0";

    private final EndPointConfigurationService endPointConfigurationService;
    private final ObjectHolder<ReplyUsagePointConfigWebService> replyUsagePointConfigWebServiceHolder;
    private final JsonService jsonService;
    private final MeteringService meteringService;

    @Inject
    public UsagePointConfigMasterServiceCallHandler(EndPointConfigurationService endPointConfigurationService,
            ObjectHolder<ReplyUsagePointConfigWebService> replyUsagePointConfigWebServiceHolder,
            JsonService jsonService, MeteringService meteringService) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.replyUsagePointConfigWebServiceHolder = replyUsagePointConfigWebServiceHolder;
        this.jsonService = jsonService;
        this.meteringService = meteringService;
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
        UsagePointConfigMasterDomainExtension extension = serviceCall
                .getExtension(UsagePointConfigMasterDomainExtension.class)
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
        if (replyUsagePointConfigWebServiceHolder.getObject() == null) {
            return;
        }
        UsagePointConfigMasterDomainExtension extension = serviceCall
                .getExtension(UsagePointConfigMasterDomainExtension.class)
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
        UsagePointConfigDomainExtension extensionForChild = child.getExtension(UsagePointConfigDomainExtension.class)
                .get();
        replyUsagePointConfigWebServiceHolder.getObject().call(endPointConfiguration.get(),
                extensionForChild.getOperation(), getSuccessfulUsagePoints(serviceCall),
                getFailedUsagePoints(serviceCall), extension.getExpectedNumberOfCalls());
    }

    private List<FailedUsagePointOperation> getFailedUsagePoints(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.FAILED))
                .map(child -> {
                    UsagePointConfigDomainExtension extension = child
                            .getExtension(UsagePointConfigDomainExtension.class).get();
                    FailedUsagePointOperation failedUsagePointOperation = new FailedUsagePointOperation();
                    failedUsagePointOperation.setErrorCode(extension.getErrorCode());
                    failedUsagePointOperation.setErrorMessage(extension.getErrorMessage());
                    ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint = jsonService.deserialize(
                            extension.getUsagePoint(), ch.iec.tc57._2011.usagepointconfig.UsagePoint.class);
                    failedUsagePointOperation.setUsagePointMrid(usagePoint.getMRID());
                    failedUsagePointOperation.setUsagePointName(retrieveName(usagePoint));
                    return failedUsagePointOperation;
                }).collect(Collectors.toList());
    }

    private String retrieveName(ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePoint) {
        List<Name> names = usagePoint.getNames();
        switch (names.size()) {
        case 0:
            return null;
        case 1:
            return names.get(0).getName();
        default:
            return names.stream()
                    .filter(name -> UsagePointBuilder.USAGE_POINT_NAME.equals(name.getNameType().getName())).findFirst()
                    .map(Name::getName).orElse(null);
        }
    }

    private List<UsagePoint> getSuccessfulUsagePoints(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().filter(child -> child.getState().equals(DefaultState.SUCCESSFUL))
                .map(child -> {
                    UsagePointConfigDomainExtension extension = child
                            .getExtension(UsagePointConfigDomainExtension.class).get();
                    return findUsagePoint(extension);
                }).collect(Collectors.toList());
    }

    private UsagePoint findUsagePoint(UsagePointConfigDomainExtension extension) {
        ch.iec.tc57._2011.usagepointconfig.UsagePoint usagePointInfo = jsonService
                .deserialize(extension.getUsagePoint(), ch.iec.tc57._2011.usagepointconfig.UsagePoint.class);
        Action action = Action.valueOf(extension.getOperation());
        // MRID is ignored for CREATE so it can have invalid value which references another existing usage point
        if (action != Action.CREATE && usagePointInfo.getMRID() != null) {
            return meteringService.findUsagePointByMRID(usagePointInfo.getMRID()).get(); // NOSONAR operation was successful so usagePoint should be found
        }
        return meteringService.findUsagePointByName(retrieveName(usagePointInfo)).get(); // NOSONAR operation was successful so usagePoint should be found
    }

}
