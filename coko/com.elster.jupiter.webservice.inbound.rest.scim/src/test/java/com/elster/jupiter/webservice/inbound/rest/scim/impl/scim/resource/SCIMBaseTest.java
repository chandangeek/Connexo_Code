package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.users.User;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.SCIMApplication;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource.OAuthBaseTest;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.GroupSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.SchemaType;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class SCIMBaseTest extends OAuthBaseTest {

    protected static final String USER_RESOURCE_PATH = "/Users";
    protected static final String GROUP_RESOURCE_PATH = "/Groups";
    protected static final String SERVICE_PROVDER_CONFIG_RESOURCE_PATH = "/ServiceProviderConfig";
    protected static final String RESOURCE_TYPE_RESOURCE_PATH = "/ResourceTypes";

    protected static final String[] LIST_OF_FIELDS_FOR_USER_COMPARISON = new String[]{
            "schemas",
            "id",
            "externalId",
            "userName",
            "displayName",
            "locale",
            "active"
    };

    protected String JWS;

    @Mock
    protected User user;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        final Response httpResponse = target(TOKEN_RESOURCE_PATH)
                .request(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + CLIENT_CREDENTIALS)
                .buildPost(Entity.form(TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS))
                .invoke();

        JWS = httpResponse.readEntity(TokenResponse.class).getAccessToken();
    }

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        configureBehaviorOfUserServiceMock();
        configureBehaviorOfUserMock();

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new SCIMApplication(userService);
    }

    private void configureBehaviorOfUserServiceMock() {
        when(userService.createUser(any(String.class), any(String.class))).thenReturn(user);
    }

    private void configureBehaviorOfUserMock() {
        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("TEST_USERNAME");
        when(user.getCreationDate()).thenReturn(Instant.now());
        when(user.getModifiedDate()).thenReturn(Instant.now());
        when(user.getVersion()).thenReturn(1L);
        when(user.getLocale()).thenReturn(Optional.of(Locale.US));
        when(user.getLanguage()).thenReturn(Locale.US.toLanguageTag());
        when(user.getStatus()).thenReturn(true);
    }

    protected UserSchema createUserWithinConnexoWithAttributeValues() {
        return target(USER_RESOURCE_PATH)
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildPost(Entity.entity(createUserSchemaWithRandomAttributeValues(), "application/scim+json"))
                .invoke()
                .readEntity(UserSchema.class);
    }

    protected GroupSchema createGroupWithinConnexoWithAttributeValues() {
        return target(GROUP_RESOURCE_PATH)
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildPost(Entity.entity(createGroupSchemaWithRandomAttributeValues(), "application/scim+json"))
                .invoke()
                .readEntity(GroupSchema.class);
    }

    protected UserSchema createUserSchemaForUserMock() {
        final UserSchema userSchema = new UserSchema();
        userSchema.setSchemas(new String[]{SchemaType.USER_SCHEMA.getId()});
        userSchema.setExternalId("1");
        userSchema.setId("1");
        userSchema.setMeta(null);
        userSchema.setUserName("TEST_USERNAME");
        userSchema.setDisplayName("TEST_USERNAME");
        userSchema.setLocale(Locale.US.toLanguageTag());
        userSchema.setActive(true);
        return userSchema;
    }

    protected UserSchema createUserSchemaWithRandomAttributeValues() {
        final UserSchema userSchema = new UserSchema();
        userSchema.setSchemas(new String[]{SchemaType.USER_SCHEMA.getId()});
        userSchema.setExternalId(UUID.randomUUID().toString());
        userSchema.setId(null);
        userSchema.setMeta(null);
        userSchema.setUserName("Trump");
        userSchema.setDisplayName("The President");
        userSchema.setLocale("en-US");
        userSchema.setActive(true);
        return userSchema;
    }

    protected GroupSchema createGroupSchemaWithRandomAttributeValues() {
        final GroupSchema groupSchema = new GroupSchema();
        groupSchema.setSchemas(new String[]{SchemaType.USER_SCHEMA.getId()});
        groupSchema.setExternalId(UUID.randomUUID().toString());
        groupSchema.setId(null);
        groupSchema.setMeta(null);
        groupSchema.setDisplayName("TEST_GROUP_DISPLAY_NAME");
        groupSchema.setMembers(new String[]{UUID.randomUUID().toString()});
        return groupSchema;
    }
}
