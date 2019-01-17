package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;

import org.osgi.service.component.annotations.Component;


@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.servicecall.ParentGetMeterReadingsServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + ParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class ParentGetMeterReadingsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "ParentGetMeterReadingsServiceCallHandler";
    public static final String VERSION = "v1.0";

    private volatile EndPointConfigurationService endPointConfigurationService;
    private SendMeterReadingsProvider sendMeterReadingsProvider;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Parent service call is swithed to state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                serviceCall.findChildren().stream().forEach(child -> child.requestTransition(DefaultState.PENDING));
                break;
            case SUCCESSFUL:
                collectAndSentResult(serviceCall);
                break;
            case FAILED:
                collectAndSentResult(serviceCall);
                break;
            case PARTIAL_SUCCESS:
                collectAndSentResult(serviceCall);
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
        parentServiceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case SUCCESSFUL:
                doSomething(parentServiceCall, newState);
                break;
            case FAILED:
                doSomething(parentServiceCall, newState);
                break;
            case CANCELLED:
            case REJECTED:
            default:
                // No specific action required for these states
                break;
        }
    }

    private void doSomething(ServiceCall parentServiceCall, DefaultState newState) {
        /// TODO maybe to log here
        parentServiceCall.requestTransition(DefaultState.SUCCESSFUL);
    }

    private void collectAndSentResult(ServiceCall serviceCall) {
        serviceCall.log(LogLevel.FINE, "Result collection is started");
        /// TODO
    }

}
