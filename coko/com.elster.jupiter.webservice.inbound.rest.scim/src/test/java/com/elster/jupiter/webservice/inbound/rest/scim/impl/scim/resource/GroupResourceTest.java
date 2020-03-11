package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.GroupSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class GroupResourceTest extends SCIMBaseTest {

    @Test
    public void shouldCreateGroup() {
        final GroupSchema groupSchema = createGroupSchemaWithRandomAttributeValues();

        final Response response = target(GROUP_RESOURCE_PATH)
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildPost(Entity.entity(groupSchema, "application/scim+json"))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final GroupSchema createdGroup = response.readEntity(GroupSchema.class);

        assertThat(createdGroup).isEqualToComparingOnlyGivenFields(groupSchema, LIST_OF_FILEDS_FOR_GROUP_COMPARISON);
    }

    @Test
    public void shouldReturnNoGroup() {
        final Response response = target(GROUP_RESOURCE_PATH + "/" + UUID.randomUUID())
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void shouldUpdateGroup() {
        when(userService.findGroupByExternalId(any())).thenReturn(Optional.of(group));

        final GroupSchema existingGroup = createGroupWithinConnexoWithAttributeValues();
        existingGroup.setMembers(new String[]{});

        final Response response = target(GROUP_RESOURCE_PATH + "/" + existingGroup.getExternalId())
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildPut(Entity.entity(existingGroup, "application/scim+json"))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final GroupSchema updatedGroup = response.readEntity(GroupSchema.class);

        assertThat(existingGroup).isEqualToComparingOnlyGivenFields(updatedGroup, LIST_OF_FILEDS_FOR_GROUP_COMPARISON);
    }

    @Test
    public void shouldDeleteGroup() {
        when(userService.findGroupByExternalId(anyString())).thenReturn(Optional.of(group));

        final GroupSchema existingGroup = createGroupWithinConnexoWithAttributeValues();

        final Response response = target(GROUP_RESOURCE_PATH + "/" + existingGroup.getExternalId())
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildDelete()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        when(userService.findGroupByExternalId(anyString())).thenReturn(Optional.empty());

        final Response getDeletedUserResponse = target(GROUP_RESOURCE_PATH + "/" + existingGroup.getExternalId())
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildGet()
                .invoke();

        assertThat(getDeletedUserResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

}