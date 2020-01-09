package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TokenResponse {

    private String accessToken;

    private String tokenType;

    private String expirationDate;

    public TokenResponse() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public static final class TokenResponseBuilder {
        private String accessToken;
        private String tokenType;
        private String expirationDate;

        private TokenResponseBuilder() {
        }

        public static TokenResponseBuilder aTokenResponse() {
            return new TokenResponseBuilder();
        }

        public TokenResponseBuilder withAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public TokenResponseBuilder withTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public TokenResponseBuilder withExpirationDate(String expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }

        public TokenResponse build() {
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.expirationDate = this.expirationDate;
            tokenResponse.tokenType = this.tokenType;
            tokenResponse.accessToken = this.accessToken;
            return tokenResponse;
        }
    }
}
