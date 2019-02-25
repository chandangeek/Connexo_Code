/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MeterConfig
 */
@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.servicecall.MeterConfigMasterServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + MasterDataLinkageConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class MasterDataLinkageConfigMasterServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MasterDataLinkageConfigMasterServiceCallHandler";
    public static final String VERSION = "v1.0";

   // private volatile DeviceService deviceService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile JsonService jsonService;

    private ReplyMasterDataLinkageConfigWebService replyMasterDataLinkageConfigWebService;

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

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void addReplyMeterConfigWebServiceClient(ReplyMasterDataLinkageConfigWebService webService) {
        replyMasterDataLinkageConfigWebService = webService;
    }

    public void removeReplyMeterConfigWebServiceClient(ReplyMasterDataLinkageConfigWebService webService) {
        replyMasterDataLinkageConfigWebService = null;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    private void updateCounter(ServiceCall serviceCall, DefaultState state) {
//        MeterConfigMasterDomainExtension extension = serviceCall.getExtension(MeterConfigMasterDomainExtension.class)
//                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
//
//        BigDecimal successfulCalls = extension.getActualNumberOfSuccessfulCalls();
//        BigDecimal failedCalls = extension.getActualNumberOfFailedCalls();
//        BigDecimal expectedCalls = extension.getExpectedNumberOfCalls();
//
//        if (DefaultState.SUCCESSFUL.equals(state)) {
//            successfulCalls = successfulCalls.add(BigDecimal.ONE);
//            extension.setActualNumberOfSuccessfulCalls(successfulCalls);
//        } else {
//            failedCalls = failedCalls.add(BigDecimal.ONE);
//            extension.setActualNumberOfFailedCalls(failedCalls);
//        }
//        serviceCall.update(extension);
//
//        if (extension.getExpectedNumberOfCalls().compareTo(successfulCalls.add(failedCalls)) <= 0) {
//            if (successfulCalls.compareTo(expectedCalls) >= 0 && serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)) {
//                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
//            } else if (failedCalls.compareTo(expectedCalls) >= 0 && serviceCall.canTransitionTo(DefaultState.FAILED)) {
//                serviceCall.requestTransition(DefaultState.FAILED);
//            } else if (serviceCall.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
//                serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
//            }
//        }
    }

    private void sendResponseToOutboundEndPoint(ServiceCall serviceCall) {
//        MeterConfigMasterDomainExtension extensionFor = serviceCall.getExtensionFor(new MeterConfigMasterCustomPropertySet()).get();
//        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService.findEndPointConfigurations().find()
//                .stream()
//                .filter(EndPointConfiguration::isActive)
//                .filter(epc -> !epc.isInbound())
//                .filter(epc -> epc.getUrl().equals(extensionFor.getCallbackURL()))
//                .findAny();
//
//        ServiceCall child = serviceCall.findChildren().stream().findFirst().get();
//        MeterConfigDomainExtension extensionForChild = child.getExtensionFor(new MeterConfigCustomPropertySet()).get();
//        OperationEnum operation = OperationEnum.getFromString(extensionForChild.getOperation());
//
//        replyMeterConfigWebService.call(endPointConfiguration.get(), operation,
//                getSuccessfullyProceededDevices(serviceCall),
//                getUnsuccessfullyProceededDevices(serviceCall),
//                extensionFor.getExpectedNumberOfCalls());
    }

  }
