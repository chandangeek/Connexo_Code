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
import java.util.Map;

@Veto
public class ConnexoRESTWorkItemHandler extends RESTWorkItemHandler {
    private String token;

    @Override
    protected HttpResponse doRequestWithAuthorization(HttpClient httpclient, RequestBuilder requestBuilder, Map<String, Object> params, AuthenticationType type) {
        if (token == null) {
            token = System.getProperty("com.elster.jupiter.token");
        }
        HttpUriRequest request = requestBuilder.build();
        request.addHeader("Authorization", "Bearer " + this.token);
        try {
            return httpclient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException("Could not execute request on Connexo REST API!", e);
        }
    }
}