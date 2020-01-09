package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TokenRequest {

    private String grantType;

    private String scope;

    public TokenRequest() {
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
