package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OAuthErrorResponse {

    private String error;

    private String errorDescription;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
