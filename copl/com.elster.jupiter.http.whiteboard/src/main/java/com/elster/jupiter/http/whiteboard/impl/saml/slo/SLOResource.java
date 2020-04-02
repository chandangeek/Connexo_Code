package com.elster.jupiter.http.whiteboard.impl.saml.slo;

import com.elster.jupiter.http.whiteboard.SAMLSingleLogoutService;
import com.elster.jupiter.http.whiteboard.impl.saml.SAMLUtilities;
import com.elster.jupiter.rest.util.Transactional;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

@Path("saml/v2")
public class SLOResource {

    private static final Logger LOGGER = Logger.getLogger(SLOResource.class.getName());

    private static SAMLUtilities samlUtilities = SAMLUtilities.getInstance();

    @Inject
    private SAMLSingleLogoutService samlSingleLogoutService;

    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_FORM_URLENCODED})
    @Path("logout")
    @Transactional
    public Response logout(@FormParam("SAMLRequest") String base64EncodedAndDeflatedSLORequest,
                           @FormParam("RelayState") String relayState) throws XMLParserException, UnmarshallingException, DataFormatException, TransformerException, MarshallingException {
        final LogoutRequest logoutRequest = samlUtilities.createLogoutRequest(base64EncodedAndDeflatedSLORequest);
        final LogoutResponse logoutResponse = samlSingleLogoutService.initializeSingleLogout(logoutRequest);
        return Response.ok()
                .entity(buildSLOResponseForm(logoutResponse, relayState))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .build();
    }

    private Form buildSLOResponseForm(final LogoutResponse logoutResponse, final String relayState) throws TransformerException, MarshallingException {
        final Form form = new Form();
        form.param("SAMLResponse", samlUtilities.marshallLogoutResponse(logoutResponse));
        form.param("RelayState", relayState);
        return form;
    }

}
