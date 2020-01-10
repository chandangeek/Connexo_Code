package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;
import io.jsonwebtoken.Jwt;

public interface TokenService {

    TokenResponse createToken();

    Jwt<?, ?> verifyToken(String jws);

}
