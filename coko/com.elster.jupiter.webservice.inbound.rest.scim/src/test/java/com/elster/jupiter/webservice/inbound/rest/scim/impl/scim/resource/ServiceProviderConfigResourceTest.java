package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.ServiceProviderConfigSchema;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceProviderConfigResourceTest extends SCIMBaseTest {

    @Test
    public void shouldReturnServiceProviderConfig() {
        final Response response = target(SERVICE_PROVDER_CONFIG_RESOURCE_PATH)
                .request("application/scim+json")
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final ServiceProviderConfigSchema serviceProviderConfigSchema = response.readEntity(ServiceProviderConfigSchema.class);

        assertThat(serviceProviderConfigSchema).isNotNull();
        assertThat(serviceProviderConfigSchema.getPatch().isSupported()).isFalse();
        assertThat(serviceProviderConfigSchema.getBulk().isSupported()).isFalse();
        assertThat(serviceProviderConfigSchema.getFilter().isSupported()).isFalse();
        assertThat(serviceProviderConfigSchema.getChangePassword().isSupported()).isFalse();
        assertThat(serviceProviderConfigSchema.getSort().isSupported()).isFalse();
        assertThat(serviceProviderConfigSchema.getEtag().isSupported()).isFalse();
        assertThat(serviceProviderConfigSchema.getAuthenticationSchemes()).isNotEmpty();
    }

}