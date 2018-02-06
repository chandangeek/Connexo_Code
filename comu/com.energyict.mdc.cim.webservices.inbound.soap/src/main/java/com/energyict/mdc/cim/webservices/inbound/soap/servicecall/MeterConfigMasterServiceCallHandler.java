/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.ReplyMeterConfigWebService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MeterConfig
 */
@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.servicecall.MeterConfigMasterServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + MeterConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class MeterConfigMasterServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MeterConfigMasterServiceCallHandler";

    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile PropertySpecService propertySpecService;
    private volatile ServiceCallService serviceCallService;
    private volatile JsonService jsonService;

    private final List<ReplyMeterConfigWebService> webServiceList = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addReplyMeterConfigWebServiceClient(ReplyMeterConfigWebService webService) {
        webServiceList.add(webService);
    }

    public void removeReplyMeterConfigWebServiceClient(ReplyMeterConfigWebService webService) {
        webServiceList.remove(webService);
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINEST, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                serviceCall.findChildren().stream().forEach(child ->
                        child.requestTransition(DefaultState.PENDING));
                break;
            case SUCCESSFUL:
                sendResponseToOutbound(serviceCall);
                break;
            case FAILED:
                sendResponseToOutbound(serviceCall);
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
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case SUCCESSFUL:
                updateCounter(parentServiceCall, newState);
                break;
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
        MeterConfigMasterDomainExtension extension = serviceCall.getExtension(MeterConfigMasterDomainExtension.class)
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

    private void sendResponseToOutbound(ServiceCall serviceCall) {
        MeterConfigMasterDomainExtension extensionFor = serviceCall.getExtensionFor(new MeterConfigMasterCustomPropertySet()).get();
        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService.findEndPointConfigurations().find()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(epc -> !epc.isInbound())
                .filter(epc -> epc.getUrl().equals(extensionFor.getCallbackURL()))
                .findAny();
        serviceCall.findChildren();
    }
}
