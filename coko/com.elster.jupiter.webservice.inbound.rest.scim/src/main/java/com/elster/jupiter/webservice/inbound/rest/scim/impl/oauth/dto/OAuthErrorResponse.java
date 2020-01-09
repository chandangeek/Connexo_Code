package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OAuthErrorResponse {

    @XmlElement(name = "error")
    private String error;

    @XmlElement(name = "error_description")
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
