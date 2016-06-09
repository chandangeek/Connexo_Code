package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Component(name = "com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=UsagePointCommandHandler")
public class UsagePointCommandHandler  implements ServiceCallHandler {

    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringService meteringService;

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
            case ONGOING:

                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
        }
    }

    public String sendResponce(String targetURL, String authorization) {
        HttpURLConnection httpConnection = null;
        String authorizationHeader = (basicAuthString != null) ? basicAuthString : authorization;
        try {
            URL targetUrl = new URL(url + targetURL);
            httpConnection = (HttpURLConnection) targetUrl.openConnection();
            httpConnection.setConnectTimeout(60000);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Authorization", authorizationHeader);
            httpConnection.setRequestProperty("Accept", "application/json");

            int responseCode = httpConnection.getResponseCode();
            if (responseCode != 200 && responseCode != 204) {
                throw new RuntimeException(Integer.toString(responseCode));
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (httpConnection.getInputStream())));

            String output;
            StringBuilder jsonContent = new StringBuilder();
            while ((output = br.readLine()) != null) {
                jsonContent.append(output);
            }
            return jsonContent.toString();

        } catch (IOException e) {
            return e.getMessage();
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }



}
