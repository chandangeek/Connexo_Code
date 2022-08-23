package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.SCIMApplication;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource.OAuthBaseTest;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.GroupSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.SchemaType;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;

import org.glassfish.jersey.test.TestProperties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public abstract class SCIMBaseTest extends OAuthBaseTest {

    protected static final String USER_RESOURCE_PATH = "/Users";
    protected static final String GROUP_RESOURCE_PATH = "/Groups";
    protected static final String SERVICE_PROVDER_CONFIG_RESOURCE_PATH = "/ServiceProviderConfig";
    protected static final String RESOURCE_TYPE_RESOURCE_PATH = "/ResourceTypes";

    protected static final String[] LIST_OF_FIELDS_FOR_USER_COMPARISON = new String[]{
            "schemas",
            "externalId",
            "userName",
            "displayName",
            "locale",
            "active"
    };

    protected static final String[] LIST_OF_FILEDS_FOR_GROUP_COMPARISON = new String[]{
            "schemas",
            "externalId",
            "displayName",
            "members",
    };

    @Mock
    protected User user;

    @Mock
    protected Group group;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        configureBehaviorOfUserServiceMock();
        configureBehaviorOfUserMock();
        configureBehaviorOfGroupMock();

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new SCIMApplication(userService, tokenService);
    }

    protected void configureBehaviorOfUserServiceMock() {
        when(userService.createSCIMUser(any(String.class), any(String.class), any(String.class))).thenReturn(user);
        when(userService.createSCIMGroup(any(String.class), any(String.class), any(String.class))).thenReturn(group);
        when(userService.findUserByExternalId(any(String.class))).thenReturn(Optional.empty());
        when(userService.findUserByExternalId(Matchers.eq("1"))).thenReturn(Optional.of(user));
        when(userService.findGroupByExternalId(any(String.class))).thenReturn(Optional.empty());
        when(userService.findGroupByExternalId(Matchers.eq("1"))).thenReturn(Optional.of(group));
        when(userService.getGroupMembers(any(String.class))).thenReturn(new ArrayList<>());
    }

    protected void configureBehaviorOfUserMock() {
        when(user.getId()).thenReturn(1L);
        when(user.getExternalId()).thenReturn("1");
        when(user.getName()).thenReturn("TEST_USERNAME");
        when(user.getCreationDate()).thenReturn(Instant.now());
        when(user.getModifiedDate()).thenReturn(Instant.now());
        when(user.getVersion()).thenReturn(1L);
        when(user.getLocale()).thenReturn(Optional.of(Locale.US));
        when(user.getLanguage()).thenReturn(Locale.US.toLanguageTag());
        when(user.getStatus()).thenReturn(true);
    }

    private void configureBehaviorOfGroupMock() {
        when(group.getId()).thenReturn(1L);
        when(group.getExternalId()).thenReturn("1");
        when(group.getName()).thenReturn("TEST_GROUP_DISPLAY_NAME");
        when(group.getCreationDate()).thenReturn(Instant.now());
        when(group.getModifiedDate()).thenReturn(Instant.now());
        when(group.getVersion()).thenReturn(1L);
    }

    protected UserSchema createUserWithinConnexoWithAttributeValues() {
        return target(USER_RESOURCE_PATH)
                .request("application/scim+json")
                .buildPost(Entity.entity(createUserSchemaWithRandomAttributeValues(), "application/scim+json"))
                .invoke()
                .readEntity(UserSchema.class);
    }

    protected GroupSchema createGroupWithinConnexoWithAttributeValues() {
        return target(GROUP_RESOURCE_PATH)
                .request("application/scim+json")
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
        userSchema.setExternalId("1");
        userSchema.setId(null);
        userSchema.setMeta(null);
        userSchema.setUserName("Trump");
        userSchema.setDisplayName("The President");
        userSchema.setLocale("en-US");
        userSchema.setActive(true);
        return userSchema;
    }

    protected GroupSchema createGroupSchemaForGroupMock() {
        final GroupSchema groupSchema = new GroupSchema();
        groupSchema.setSchemas(new String[]{SchemaType.GROUP_SCHEMA.getId()});
        groupSchema.setExternalId("1");
        groupSchema.setId("1");
        groupSchema.setMeta(null);
        groupSchema.setDisplayName("TEST_GROUP_DISPLAY_NAME");
        groupSchema.setMembers(new String[]{UUID.randomUUID().toString()});
        return groupSchema;
    }

    protected GroupSchema createGroupSchemaWithRandomAttributeValues() {
        final GroupSchema groupSchema = new GroupSchema();
        groupSchema.setSchemas(new String[]{SchemaType.GROUP_SCHEMA.getId()});
        groupSchema.setExternalId("1");
        groupSchema.setId(null);
        groupSchema.setMeta(null);
        groupSchema.setDisplayName("TEST_GROUP_DISPLAY_NAME");
        groupSchema.setMembers(new String[]{});
        return groupSchema;
    }
}
