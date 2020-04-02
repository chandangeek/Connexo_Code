package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error;

public enum OAuthError {

    INVALID_REQUEST("invalid_request", "The request is missing a required parameter, includes an unsupported parameter value (other than grant type), repeats a parameter, includes multiple credentials, utilizes more than one mechanism for authenticating the client, or is otherwise malformed.", 400),
    INVALID_CLIENT("invalid_client", "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method).  The authorization server MAY return an HTTP 401 (Unauthorized) status code to indicate which HTTP authentication schemes are supported.  If the client attempted to authenticate via the \"Authorization\" request header field, the authorization server MUST respond with an HTTP 401 (Unauthorized) status code and include the \"WWW-Authenticate\" response header field matching the authentication scheme used by the client.", 401),
    INVALID_GRANT("invalid_grant", "The provided authorization grant (e.g., authorization code, resource owner credentials) or refresh token is invalid, expired, revoked, does not match the redirection URI used in the authorization request, or was issued to another client", 400),
    UNAUTHORIZED_CLIENT("unauthorized_client", "The authenticated client is not authorized to use this authorization grant type.", 400),
    UNSUPPORTED_GRANT_TYPE("unsupported_grant_type", "The authorization grant type is not supported by the authorization server.", 400);

    String errorCode;

    String description;

    int httpCode;

    OAuthError(String errorCode, String description, int httpCode) {
        this.errorCode = errorCode;
        this.description = description;
        this.httpCode = httpCode;
    }
}
