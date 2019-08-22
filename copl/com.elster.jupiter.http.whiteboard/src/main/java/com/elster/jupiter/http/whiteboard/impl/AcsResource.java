package com.elster.jupiter.http.whiteboard.impl;

import aQute.bnd.build.Run;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.ecp.RelayState;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("security")
public class AcsResource {

    private static final Logger LOGGER = Logger.getLogger(AcsResource.class.getName());

    private volatile SamlResponseService samlResponseService;

    @POST
    @Path("acs")
    public Response handleSAMLResponse(@QueryParam("SAMLResponse") String samlResponse, @QueryParam(RelayState.DEFAULT_ELEMENT_LOCAL_NAME) String relayState) {

        /*
            TODO: handle SAML Response
                1. Create SAML Assertion object
                2. Validate SAML Assertion
                3. Create a JWT token based on SAML Assertion (including roles mapping from IDP spec. to SP spec.)
                4. Set JWT token to header
                5. Redirect to RelayState
         */

        org.opensaml.saml.saml2.core.Response response = samlResponseService.createSamlResponse(samlResponse);

        try {
            samlResponseService.validateSignature(response.getSignature(),SamlUtils.X509_CERTIFICATE);

            Assertion assertion = samlResponseService.getCheckedAssertion(response);
            String email = samlResponseService.getSubjectNameId(assertion);
            Map<String, List<String>> attributeValueMap = samlResponseService.getAttributeValues(assertion);

            String firstName = getSingleValue("User.FirstName", attributeValueMap);
            String lastName = getSingleValue("User.LastName", attributeValueMap);

        } catch (SAMLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return Response
                .seeOther(URI.create(relayState))
                .header(HttpHeaders.AUTHORIZATION, "token")
                .build();
    }

    @Reference
    public void setSamlResponseService(SamlResponseService samlResponseService) {
        this.samlResponseService = samlResponseService;
    }

    private String getSingleValue(String attributeName, Map<String, List<String>> attributeValues) {
        return attributeValues.getOrDefault(attributeName, Collections.emptyList()).stream().findFirst().orElse(StringUtils.EMPTY);
    }
}
