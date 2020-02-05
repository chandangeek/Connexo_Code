package com.elster.jupiter.http.whiteboard.impl.saml.slo;

import com.elster.jupiter.http.whiteboard.SAMLSingleLogoutService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.http.whiteboard.impl.saml.SAMLUtilities;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.xmlsec.signature.support.SignatureException;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.elster.jupiter.http.whiteboard.impl.saml.SAMLUtilities.createLogoutResponse;

public class SAMLSingleLogoutServiceImpl implements SAMLSingleLogoutService {

    private static SAMLUtilities samlUtilities = SAMLUtilities.getInstance();

    @Inject
    private TokenService tokenService;

    @Inject
    private UserService userService;

    public SAMLSingleLogoutServiceImpl(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    public LogoutResponse initializeSingleLogout(LogoutRequest logoutRequest) {
        final NameID nameID = logoutRequest.getNameID();
        if (Objects.isNull(nameID)) {
            return createLogoutResponse(StatusCode.INVALID_ATTR_NAME_OR_VALUE);
        }

        final SAMLSignatureProfileValidator samlSignatureProfileValidator = new SAMLSignatureProfileValidator();
        try {
            samlSignatureProfileValidator.validate(Objects.requireNonNull(logoutRequest.getSignature()));
        } catch (SignatureException e) {
            return createLogoutResponse(StatusCode.REQUEST_DENIED);
        }

        final Optional<User> userByExternalId = userService.findUserByExternalId(nameID.getValue());
        if (!userByExternalId.isPresent()) {
            return createLogoutResponse(StatusCode.REQUEST_DENIED);
        }

        final User user = userByExternalId.get();

        try {
            tokenService.invalidateUserJWT(user);
        } catch (ExecutionException e) {
            return createLogoutResponse(StatusCode.REQUEST_DENIED);
        }

        return createLogoutResponse(StatusCode.SUCCESS);
    }
}
