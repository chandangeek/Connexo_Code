package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.ResourceTypeSchema;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaResourceTest extends SCIMBaseTest {

    @Test
    public void shouldReturnResourceTypeSchemes() {
        final Response response = target(RESOURCE_TYPE_RESOURCE_PATH)
                .request("application/scim+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JWS)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final List<ResourceTypeSchema> resourceTypeSchema = response.readEntity(new GenericType<List<ResourceTypeSchema>>() {
        });

        assertThat(resourceTypeSchema).isNotNull();
    }

}