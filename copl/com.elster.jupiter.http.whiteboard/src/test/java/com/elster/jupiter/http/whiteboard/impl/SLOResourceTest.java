package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.impl.saml.SAMLUtilities;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.junit.Test;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.LogoutResponse;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.zip.DataFormatException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SLOResourceTest extends SLOBaseTest {

    @Test
    public void shouldInvalidateUserSessionAndReturnStatusSuccess() throws UnmarshallingException, DataFormatException, XMLParserException {
        when(userService.findUserByExternalId(any())).thenReturn(Optional.of(user));

        final Response response = target(SLO_ENDPOINT_PATH)
                .request()
                .buildPost(Entity.form(createFormForSAMLSingleLogoutRequest()))
                .invoke();

        assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response.getStatus());

        final Form responseForm = response.readEntity(Form.class);
        final String samlRespose = responseForm.asMap().getFirst("SAMLResponse");
        final String relayState = responseForm.asMap().getFirst("RelayState");

        assertThat(samlRespose).isNotEmpty();
        assertThat(relayState).isEqualTo(SLO_VALUE_RELAY_STATE);

        final LogoutResponse logoutResponse = SAMLUtilities.unmarshallLogoutResponse(samlRespose);

        assertThat(logoutResponse.getStatus().getStatusCode().getValue()).isEqualTo("urn:oasis:names:tc:SAML:2.0:status:Success");

        verify(userService).findUserByExternalId(any());
    }

    @Test
    public void shouldNotInvalidateUserSessionAndReturnStatusFailed() throws XMLParserException, UnmarshallingException, DataFormatException {
        when(userService.findUserByExternalId(any())).thenReturn(Optional.empty());

        final Response response = target(SLO_ENDPOINT_PATH)
                .request()
                .buildPost(Entity.form(createFormForSAMLSingleLogoutRequest()))
                .invoke();

        assertThat(Response.Status.OK.getStatusCode()).isEqualTo(response.getStatus());

        final Form responseForm = response.readEntity(Form.class);
        final String samlRespose = responseForm.asMap().getFirst("SAMLResponse");
        final String relayState = responseForm.asMap().getFirst("RelayState");

        assertThat(samlRespose).isNotEmpty();
        assertThat(relayState).isEqualTo(SLO_VALUE_RELAY_STATE);

        final LogoutResponse logoutResponse = SAMLUtilities.unmarshallLogoutResponse(samlRespose);

        assertThat(logoutResponse.getStatus().getStatusCode().getValue()).isEqualTo("urn:oasis:names:tc:SAML:2.0:status:RequestDenied");

        verify(userService).findUserByExternalId(any());
    }

    private Form createFormForSAMLSingleLogoutRequest() {
        return new Form()
                .param(SLO_NAME_LOGOUT_REQUEST, SLO_VALUE_LOGOUT_REQUEST)
                .param(SLO_NAME_RELAY_STATE, SLO_VALUE_RELAY_STATE);
    }

}