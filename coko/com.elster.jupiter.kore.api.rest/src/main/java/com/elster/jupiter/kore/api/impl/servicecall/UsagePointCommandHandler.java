package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

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

    private String basicAuthString;

    @Activate
    public void activate(BundleContext context){
        String user = context.getProperty(BPM_USER);
        String password = context.getProperty(BPM_PASSWORD);
        if(user != null && password != null) {
            this.basicAuthString = "Basic " + new String(Base64.getEncoder().encode((user + ":" + password).getBytes()));
        }
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
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
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        sendResponse(extension.getCallbackSuccessURL(), extension.getCallbackHttpMethod());
    }

    public void getPartiallySuccessResponce(ServiceCall serviceCall){
        UsagePointCommandDomainExtension extension = serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        sendResponse(extension.getCallbackPartialSuccessURL(), extension.getCallbackHttpMethod());
    }

    public void getFailureResponce(ServiceCall serviceCall){
        UsagePointCommandDomainExtension extension = serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        sendResponse(extension.getCallbackFailureURL(), extension.getCallbackHttpMethod());
    }


    public void sendResponse(String targetURL, String method) {
        HttpURLConnection httpConnection = null;
        try {
            URL targetUrl = new URL(targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setConnectTimeout(60000);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod(method);
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setRequestProperty("Authorization", basicAuthString);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException(Integer.toString(responseCode));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to send responce.");
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}
