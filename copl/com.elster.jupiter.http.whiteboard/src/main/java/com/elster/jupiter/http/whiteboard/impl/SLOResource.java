package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.bouncycastle.util.encoders.Base64;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.impl.LogoutRequestUnmarshaller;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Path("saml/v2")
public class SLOResource {

    private static final Logger LOGGER = Logger.getLogger(SLOResource.class.getName());

    @Inject
    private HttpAuthenticationService authenticationService;

    @Inject
    private SamlResponseService samlResponseService;

    @POST
    @Path("logout")
    public void logout(@QueryParam("SAMLRequest") String base64EncodedAndDeflatedSLORequest) throws XMLParserException, IOException, UnmarshallingException, DataFormatException {
        final LogoutRequest logoutRequest = createLogoutRequest(base64EncodedAndDeflatedSLORequest);
    }

    private LogoutRequest createLogoutRequest(final String base64EncodedAndDeflatedSLORequest) throws UnmarshallingException, XMLParserException, IOException, DataFormatException {
        final Element documentElement = Objects.requireNonNull(XMLObjectProviderRegistrySupport.getParserPool())
                .parse(new ByteArrayInputStream(decodeBase64AndInflateSLORequest(base64EncodedAndDeflatedSLORequest)))
                .getDocumentElement();
        final LogoutRequestUnmarshaller logoutRequestUnmarshaller = new LogoutRequestUnmarshaller();
        return (LogoutRequest) logoutRequestUnmarshaller.unmarshall(documentElement);
    }

    private byte[] decodeBase64AndInflateSLORequest(final String base64EncodedAndDeflatedSLORequest) throws DataFormatException, IOException {
        return inflateByteArray(decodeBase64(base64EncodedAndDeflatedSLORequest));
    }

    private byte[] decodeBase64(final String base64EncodedString) {
        return Base64.decode(base64EncodedString);
    }

    private byte[] inflateByteArray(final byte[] deflatedByteArray) throws DataFormatException, IOException {
        final Inflater inflater = new Inflater(true);
        inflater.setInput(deflatedByteArray);

        final ByteArrayOutputStream inflatedOutputStream = new ByteArrayOutputStream(deflatedByteArray.length);
        final byte[] BUFFER = new byte[1024];
        while (!inflater.finished()) {
            final int count = inflater.inflate(BUFFER);
            inflatedOutputStream.write(BUFFER, 0, count);
        }
        inflatedOutputStream.close();
        inflater.end();

        return inflatedOutputStream.toByteArray();
    }


}
