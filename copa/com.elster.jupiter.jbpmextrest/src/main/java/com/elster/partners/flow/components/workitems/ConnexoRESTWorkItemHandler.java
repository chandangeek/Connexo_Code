/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.flow.components.workitems;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.jbpm.process.workitem.rest.RESTWorkItemHandler;
import org.uberfire.commons.services.cdi.Veto;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;

@Veto
public class ConnexoRESTWorkItemHandler extends RESTWorkItemHandler {
    private String token;
    private String user;
    private String password;
    private URI connexoURI;
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String FLOW_CSRF_HEADER= "BFlowProcess";
    private static final String FLOW_PROCESS_KEY = "BPMconnexoflowProcess";
    @Override
    protected HttpResponse doRequestWithAuthorization(HttpClient httpclient, RequestBuilder requestBuilder, Map<String, Object> params, AuthenticationType type) {
        loadConfiguration();

        URI configuredURI = requestBuilder.getUri();
        try {
            requestBuilder.setUri(new URI(connexoURI.toString() + "/" + configuredURI.toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        HttpUriRequest request = requestBuilder.build();
        if (this.token != null && !this.token.isEmpty()) {
            request.addHeader("Authorization", "Bearer " + this.token);
        } else {
            if ((this.user != null && !this.user.isEmpty()) &&
                    (this.password != null && !this.password.isEmpty())) {
                request.addHeader("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString((this.user + ":" + this.password).getBytes()));
            }
        }
        try {
            if(isFormSubmitRequest(request)) {
                request.addHeader(FLOW_CSRF_HEADER, Base64.getEncoder().encodeToString(FLOW_PROCESS_KEY.getBytes()));
            }
            return httpclient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException("Could not execute request on Connexo REST API!", e);
        }
    }

    private void loadConfiguration() {
        if (token == null) {
            token = System.getProperty("com.elster.jupiter.token");
        }

        if (connexoURI == null) {
            try {
                connexoURI = new URI(System.getProperty("com.elster.jupiter.url"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (user == null) {
            this.user = System.getProperty("com.elster.jupiter.user");
        }

        if (password == null) {
            this.password = System.getProperty("com.elster.jupiter.password");
        }
    }

    private boolean isFormSubmitRequest(HttpUriRequest request){
        return Stream.of(POST, PUT, DELETE).anyMatch(request.getMethod()::equalsIgnoreCase);
    }
}