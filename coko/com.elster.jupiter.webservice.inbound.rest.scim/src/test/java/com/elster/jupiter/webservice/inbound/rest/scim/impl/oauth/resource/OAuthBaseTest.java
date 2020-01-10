package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.users.UserService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.SCIMApplication;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.impl.DefaultJwtParser;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import java.util.Base64;

public class OAuthBaseTest extends JerseyTest {

    protected static final String TOKEN_RESOURCE_PATH = "/token";
    protected static final String CLIENT_CREDENTIALS = Base64.getEncoder().encodeToString("enexis.password".getBytes());
    protected static final Form TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS = new Form();
    protected static final Form TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_UNKNOWN = new Form();
    protected static final Form TOKEN_REQUEST_FORM_WITHOUT_GRANT_TYPE = new Form();

    static {
        TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS.param("grant_type", "client_credentials");
        TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_UNKNOWN.param("grant_type", "unknown");
    }

    @Mock
    protected UserService userService;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new SCIMApplication(userService);
    }

    protected Jwt<?, ?> parseJws(final String jws) {
        String[] splitedToken = jws.split("\\.");
        String unsignedToken = splitedToken[0] + "." + splitedToken[1] + ".";

        final DefaultJwtParser defaultJwtParser = new DefaultJwtParser();
        return defaultJwtParser.parse(unsignedToken);
    }
}
