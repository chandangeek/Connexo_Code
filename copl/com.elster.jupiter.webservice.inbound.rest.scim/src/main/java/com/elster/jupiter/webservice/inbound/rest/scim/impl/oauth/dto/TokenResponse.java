package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TokenResponse {

    @XmlElement(name = "access_token")
    private String accessToken;

    @XmlElement(name = "token_type")
    private String tokenType;

    @XmlElement(name = "expires_in")
    private long expiresIn;

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

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }


    public static final class TokenResponseBuilder {
        private String accessToken;
        private String tokenType;
        private long expiresIn;

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

        public TokenResponseBuilder withExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public TokenResponse build() {
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken(accessToken);
            tokenResponse.setTokenType(tokenType);
            tokenResponse.setExpiresIn(expiresIn);
            return tokenResponse;
        }
    }
}
