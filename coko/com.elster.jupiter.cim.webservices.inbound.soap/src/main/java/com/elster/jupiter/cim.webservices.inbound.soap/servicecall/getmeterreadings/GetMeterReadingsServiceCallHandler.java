package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import org.osgi.service.component.annotations.Component;


@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.servicecall.GetMeterReadingsServiceCallHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + GetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class GetMeterReadingsServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "GetMeterReadingsServiceCallHandler";
    public static final String VERSION = "v1.0";

    private volatile EndPointConfigurationService endPointConfigurationService;
    private SendMeterReadingsProvider sendMeterReadingsProvider;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                doSomething(serviceCall);
                break;
            case SUCCESSFUL:
                break;
            case FAILED:
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void doSomething(ServiceCall serviceCall) {
        /// TODO maybe to log here
        serviceCall.log(LogLevel.FINE, "Request Transition to Successful");
        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
    }
}
