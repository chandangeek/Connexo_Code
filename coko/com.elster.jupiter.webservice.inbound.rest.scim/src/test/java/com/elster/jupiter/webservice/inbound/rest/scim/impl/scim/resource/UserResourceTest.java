package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class UserResourceTest extends SCIMBaseTest {

    @Test
    public void shouldCreateUser() {
        final UserSchema userWithRandomData = createUserSchemaForUserMock();

        final Response response = target(USER_RESOURCE_PATH)
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildPost(Entity.entity(userWithRandomData, "application/scim+json"))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final UserSchema createdUser = response.readEntity(UserSchema.class);

        assertThat(createdUser).isEqualToComparingOnlyGivenFields(userWithRandomData, LIST_OF_FIELDS_FOR_USER_COMPARISON);
    }

    @Test
    public void shouldReturnNoUser() {
        final Response response = target(USER_RESOURCE_PATH + "/" + UUID.randomUUID())
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void shouldUpdateUser() {
        final UserSchema existingUser = createUserWithinConnexoWithAttributeValues();
        existingUser.setDisplayName("TEST_DISPLAYNAME");
        existingUser.setUserName("TEST_USERNAME");

        final Response response = target(USER_RESOURCE_PATH + "/" + UUID.randomUUID())
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildPut(Entity.entity(existingUser, "application/scim+json"))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final UserSchema updatedUser = response.readEntity(UserSchema.class);

        assertThat(existingUser).isEqualToComparingFieldByField(updatedUser);
    }

    @Test
    public void shouldDeleteUser() {
        final UserSchema existingUser = createUserWithinConnexoWithAttributeValues();

        final Response response = target(USER_RESOURCE_PATH + "/" + existingUser.getId())
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildDelete()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final Response getDeletedUserResponse = target(USER_RESOURCE_PATH + "/" + existingUser.getId())
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildGet()
                .invoke();

        assertThat(getDeletedUserResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

}