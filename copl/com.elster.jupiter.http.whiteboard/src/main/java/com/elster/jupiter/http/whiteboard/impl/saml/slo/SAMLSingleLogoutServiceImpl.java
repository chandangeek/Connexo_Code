package com.elster.jupiter.http.whiteboard.impl.saml.slo;

import com.elster.jupiter.http.whiteboard.SAMLSingleLogoutService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.http.whiteboard.impl.saml.SAMLUtilities;
import com.elster.jupiter.http.whiteboard.UserJWT;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Objects;
import java.util.Optional;

@Component(
        name = "com.elster.jupiter.http.whiteboard.SAMLSingleLogoutService",
        property = {
                "name=" + SAMLSingleLogoutServiceImpl.SERVICE_NAME
        },
        immediate = true,
        service = {
                SAMLSingleLogoutService.class
        }
)
public class SAMLSingleLogoutServiceImpl implements SAMLSingleLogoutService {

    public static final String SERVICE_NAME = "SLO";

    private static SAMLUtilities samlUtilities = SAMLUtilities.getInstance();

    private volatile TokenService<UserJWT> tokenService;

    private volatile UserService userService;

    public SAMLSingleLogoutServiceImpl() {
    }

    public SAMLSingleLogoutServiceImpl(TokenService<UserJWT> tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    public LogoutResponse initializeSingleLogout(LogoutRequest logoutRequest) {
        final NameID nameID = logoutRequest.getNameID();
        if (Objects.isNull(nameID)) {
            return samlUtilities.createLogoutResponse(StatusCode.INVALID_ATTR_NAME_OR_VALUE);
        }

        final SAMLSignatureProfileValidator samlSignatureProfileValidator = new SAMLSignatureProfileValidator();
        try {
            samlSignatureProfileValidator.validate(Objects.requireNonNull(logoutRequest.getSignature()));
        } catch (SignatureException e) {
            return samlUtilities.createLogoutResponse(StatusCode.REQUEST_DENIED);
        }

        final Optional<User> userByExternalId = userService.findUserByExternalId(nameID.getValue());
        if (!userByExternalId.isPresent()) {
            return samlUtilities.createLogoutResponse(StatusCode.REQUEST_DENIED);
        }

        final User user = userByExternalId.get();

        tokenService.invalidateAllUserJWTsForUser(user);

        return samlUtilities.createLogoutResponse(StatusCode.SUCCESS);
    }

    @Reference
    public void setTokenService(TokenService<UserJWT> tokenService) {
        this.tokenService = tokenService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
