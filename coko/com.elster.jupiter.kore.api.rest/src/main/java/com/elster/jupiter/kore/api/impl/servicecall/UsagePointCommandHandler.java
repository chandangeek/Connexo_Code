package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.kore.api.impl.utils.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Component(name = "com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=UsagePointCommandHandler")
public class UsagePointCommandHandler  implements ServiceCallHandler {

    private static final String BPM_USER = "com.elster.jupiter.bpm.user";
    private static final String BPM_PASSWORD = "com.elster.jupiter.bpm.password";

    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringService meteringService;
    private volatile EndPointConfigurationService endPointConfigurationService;

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case SUCCESSFUL:
                sendSuccessResponce(serviceCall);
                break;
            case PARTIAL_SUCCESS:
                getPartiallySuccessResponce(serviceCall);
                break;
            case FAILED:
                getFailureResponce(serviceCall);
                break;
        }
    }

    public void sendSuccessResponce(ServiceCall serviceCall){
        UsagePointCommandDomainExtension extension = serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException(MessageSeeds.NO_SUCH_DOMAIN_EXTENSION.getDefaultFormat()));
        sendResponse(extension.getCallbackSuccessURL(), extension.getCallbackHttpMethod());
    }

    public void getPartiallySuccessResponce(ServiceCall serviceCall){
        UsagePointCommandDomainExtension extension = serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException(MessageSeeds.NO_SUCH_DOMAIN_EXTENSION.getDefaultFormat()));
        sendResponse(extension.getCallbackPartialSuccessURL(), extension.getCallbackHttpMethod());
    }

    public void getFailureResponce(ServiceCall serviceCall){
        UsagePointCommandDomainExtension extension = serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException(MessageSeeds.NO_SUCH_DOMAIN_EXTENSION.getDefaultFormat()));
        sendResponse(extension.getCallbackFailureURL(), extension.getCallbackHttpMethod());
    }


    public void sendResponse(String targetURL, String method) {

        EndPointConfiguration endPointConfiguration = endPointConfigurationService.findEndPointConfigurations().stream()
                .filter(epc -> targetURL.startsWith(epc.getUrl()))
                .findFirst().orElseThrow(() -> new IllegalStateException(MessageSeeds.NO_SUCH_ENDPOINT.getDefaultFormat()));

        HttpURLConnection httpConnection = null;
        try {
            URL targetUrl = new URL(targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setConnectTimeout(60000);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod(method);
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("Accept", "application/json");
            if(endPointConfiguration.getAuthenticationMethod().equals(EndPointAuthentication.BASIC_AUTHENTICATION)){
                httpConnection.setRequestProperty("Authorization",
                        "Basic " + new String(Base64.getEncoder().encode((
                                ((OutboundEndPointConfiguration)endPointConfiguration).getUsername()
                                + ":"
                                + ((OutboundEndPointConfiguration)endPointConfiguration).getPassword()).getBytes())));
            }
            int responseCode = httpConnection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException(Integer.toString(responseCode));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(MessageSeeds.HTTP_CONNECTION_FAILED.getDefaultFormat());
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}
