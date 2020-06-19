/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.parent;

import com.elster.jupiter.cim.webservices.outbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implementation of {@link ServiceCallHandler} interface which counts successful child service calls and sends response to outbound web service endpoint when done
 *
 * @param <T>
 *            extension for the master service call CPS
 */
public abstract class AbstractMasterServiceCallHandler<T extends AbstractMasterDomainExtension>
        implements ServiceCallHandler {

    private Class<T> extensionClass;
    private final ReplyMasterDataLinkageConfigWebService replyWebService;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;
    private final WebServicesService webServicesService;

    protected AbstractMasterServiceCallHandler(Class<T> extensionClass, ReplyMasterDataLinkageConfigWebService replyWebService,
            EndPointConfigurationService endPointConfigurationService, Thesaurus thesaurus,
            WebServicesService webServicesService) {
        this.extensionClass = extensionClass;
        this.replyWebService = replyWebService;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
        this.webServicesService = webServicesService;
    }

    /**
     *
     * @param replyWebService
     *            cannot be null
     * @param endPointConfiguration
     *            cannot be null
     * @param serviceCall
     *            cannot be null
     * @param extension
     *            cannot be null
     */
    protected abstract void sendReply(ReplyMasterDataLinkageConfigWebService replyWebService, EndPointConfiguration endPointConfiguration,
            ServiceCall serviceCall, T extension);

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
        T extension = serviceCall.getExtension(extensionClass)
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

    private void logErrorAboutMissingEndpoint(ServiceCall serviceCall, T extension) {
        serviceCall.log(LogLevel.SEVERE,
                MessageSeeds.NO_END_POINT_WITH_URL.translate(thesaurus, extension.getCallbackURL()));
    }

    private void updateCounter(ServiceCall serviceCall, DefaultState state) {
        T extension = serviceCall.getExtension(extensionClass)
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

}
