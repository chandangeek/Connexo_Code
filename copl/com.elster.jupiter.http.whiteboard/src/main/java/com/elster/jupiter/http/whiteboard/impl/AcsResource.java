package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.Assertion;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("security")
public class AcsResource {

    private static final Logger LOGGER = Logger.getLogger(AcsResource.class.getName());

    @Inject
    private HttpAuthenticationService authenticationService;

    @Inject
    private SamlResponseService samlResponseService;

    @Inject
    private UserService userService;

    @POST
    @Path("acs")
    public void handleSAMLResponse(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) throws IOException, ServletException {

        String token = null;
        org.opensaml.saml.saml2.core.Response response = samlResponseService.createSamlResponse(httpServletRequest.getParameter("SAMLResponse"));

        try {

            samlResponseService.validateSignature(response.getSignature(), authenticationService.getSsoX509Certificate());

            Assertion assertion = samlResponseService.getCheckedAssertion(response);
            String authenticationName = samlResponseService.getSubjectNameId(assertion);

            Optional<User> user = userService.findUser(authenticationName);
            if (user.isPresent()) {
                token = authenticationService.createToken(user.get(), "");
            }

        } catch (SAMLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        if (!StringUtils.isEmpty(token)) {
            Cookie cookie = authenticationService.createTokenCookie(token, "/");
            httpServletResponse.addCookie(cookie);
        }

        httpServletResponse.sendRedirect(URI.create(httpServletRequest.getParameter("RelayState")).toString());
    }
}
