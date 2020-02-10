package com.elster.jupiter.http.whiteboard.impl.saml.slo;

import com.elster.jupiter.http.whiteboard.SAMLSingleLogoutService;
import com.elster.jupiter.http.whiteboard.impl.saml.SAMLUtilities;
import com.google.common.collect.ImmutableMap;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

@Path("saml/v2")
public class SLOResource {

    private static final Logger LOGGER = Logger.getLogger(SLOResource.class.getName());

    private static SAMLUtilities samlUtilities = SAMLUtilities.getInstance();

    @Inject
    private SAMLSingleLogoutService samlSingleLogoutService;

    @POST
    @Path("logout")
    public void logout(@QueryParam("LogoutRequest") String base64EncodedAndDeflatedSLORequest,
                       @QueryParam("RelayState") String relayState,
                       @Context HttpServletResponse httpServletResponse) throws XMLParserException, UnmarshallingException, DataFormatException, IOException, MarshallingException, TransformerException {
        final LogoutRequest logoutRequest = SAMLUtilities.createLogoutRequest(base64EncodedAndDeflatedSLORequest);
        final LogoutResponse logoutResponse = samlSingleLogoutService.initializeSingleLogout(logoutRequest);
        final String serializedLogoutResponse = URLEncoder.encode(SAMLUtilities.marshallLogoutResponse(logoutResponse), "UTF-8");
        final ImmutableMap<String, String> queryParameters = ImmutableMap.of("LogoutResponse", serializedLogoutResponse);
        httpServletResponse.sendRedirect(URI.create(createRedirectUrl(relayState, queryParameters)).toString());
    }

    private String createRedirectUrl(final String relayState, final Map<String, String> queryParameters) {
        final StringBuffer stringBuffer = new StringBuffer(relayState).append("?");
        queryParameters.forEach((key, value) -> stringBuffer.append(key).append("=").append(value));
        return stringBuffer.toString();
    }

}
