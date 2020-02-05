package com.elster.jupiter.http.whiteboard.impl.saml.sso;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.http.whiteboard.MessageSeeds;
import com.elster.jupiter.http.whiteboard.SamlResponseService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.common.util.CollectionUtils;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.Assertion;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
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

    @Inject
    private Thesaurus thesaurus;

    @POST
    @Path("acs")
    public void handleSAMLResponse(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) throws IOException, ServletException {

        String token = null;
        String samlResponse = httpServletRequest.getParameter("SAMLResponse");
        String relayState = httpServletRequest.getParameter("RelayState");
        org.opensaml.saml.saml2.core.Response response = samlResponseService.createSamlResponse(samlResponse);

        try {

            samlResponseService.validateSignature(response.getSignature(), authenticationService.getSsoX509Certificate());

            Assertion assertion = samlResponseService.getCheckedAssertion(response);
            String authenticationName = samlResponseService.getSubjectNameId(assertion);

            Optional<User> user = userService.findUser(authenticationName);
            if (user.isPresent()) {
                if (checkFlowFactsPrivileges(relayState, user.get())) {
                    token = authenticationService.createToken(user.get(), "");
                } else {
                    HttpSession session = httpServletRequest.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN)
                            .entity(thesaurus.getFormat((TranslationKey) MessageSeeds.SSO_ACCESS_PERMISION_FORBIDDEN).format(relayState))
                            .build());
                }

            }
        } catch (SAMLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        if (!StringUtils.isEmpty(token)) {
            Cookie cookie = authenticationService.createTokenCookie(token, "/");
            httpServletResponse.addCookie(cookie);
        }

        httpServletResponse.sendRedirect(URI.create(relayState).toString());
    }

    private boolean checkFlowFactsPrivileges(String relayState, User user) {
        if (!relayState.contains("facts") && !relayState.contains("flow")) return true;
        if ((relayState.contains("facts") && containsFactsPrivileges(user.getPrivileges()))
                || relayState.contains("/facts/services/") || relayState.contains("/facts/JsAPI")) {
            return true;
        }
        if ((relayState.contains("flow") && containsFlowPrivileges(user.getPrivileges())) || relayState.contains("/flow/rest/")) {
            return true;
        }
        return false;
    }

    private boolean containsFactsPrivileges(Set<Privilege> userPrivileges) {
        if (CollectionUtils.isEmpty(userPrivileges)) return false;
        Optional<Privilege> factsPrivilegeOptional = userPrivileges.stream()
                .filter(privilege -> (privilege.getName().contains("privilege.administrate.reports") || privilege.getName().contains("privilege.design.reports"))).findAny();

        return factsPrivilegeOptional.isPresent();
    }

    private boolean containsFlowPrivileges(Set<Privilege> userPrivileges) {
        if (CollectionUtils.isEmpty(userPrivileges)) return false;
        Optional<Privilege> flowPrivilegeOptional = userPrivileges.stream()
                .filter(privilege -> (privilege.getName().contains("privilege.design.bpm"))).findAny();

        return flowPrivilegeOptional.isPresent();
    }
}
