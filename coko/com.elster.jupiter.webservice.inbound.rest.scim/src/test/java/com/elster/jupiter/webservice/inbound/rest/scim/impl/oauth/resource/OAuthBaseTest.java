package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.SCIMApplication;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import javax.ws.rs.core.Application;

public class OAuthBaseTest extends JerseyTest {

    protected static final String TOKEN_RESOURCE_PATH = "/token";

    @Override
    protected Application configure() {

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new SCIMApplication();
    }
}
