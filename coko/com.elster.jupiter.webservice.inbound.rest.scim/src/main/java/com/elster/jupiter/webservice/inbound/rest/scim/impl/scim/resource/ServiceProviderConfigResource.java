package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.SchemaType;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.ServiceProviderConfigSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.attribute.Meta;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration.AuthenticationScheme;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration.BasicConfigurationSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration.BulkConfigurationSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration.FilterConfigurationSchema;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.time.Instant;

@Path("ServiceProviderConfig")
public class ServiceProviderConfigResource {

    private static final ServiceProviderConfigSchema serviceProviderConfigSchema = new ServiceProviderConfigSchema();

    static {
        final Meta meta = new Meta();
        meta.setCreated(Instant.now().toString());
        meta.setLastModified(Instant.now().toString());
        meta.setLocation("/ServiceProviderConfig");
        meta.setResourceType("ServiceProviderConfig");
        meta.setVersion("1.0");
        serviceProviderConfigSchema.setMeta(meta);
        serviceProviderConfigSchema.setSchemas(new String[]{SchemaType.SERVICE_PROVIDER_CONFIGURATION_SCHEMA.getId()});

        serviceProviderConfigSchema.setPatch(new BasicConfigurationSchema(false));
        serviceProviderConfigSchema.setBulk(new BulkConfigurationSchema(false, 0, 0));
        serviceProviderConfigSchema.setFilter(new FilterConfigurationSchema(false, 0));
        serviceProviderConfigSchema.setChangePassword(new BasicConfigurationSchema(false));
        serviceProviderConfigSchema.setSort(new BasicConfigurationSchema(false));
        serviceProviderConfigSchema.setEtag(new BasicConfigurationSchema(false));
        serviceProviderConfigSchema.setAuthenticationSchemes(new AuthenticationScheme[]{new AuthenticationScheme("oauth2", "OAuth 2.0", "The OAuth 2.0 authorization framework")});
    }

    @GET
    @Produces("application/scim+json")
    public ServiceProviderConfigSchema getServiceProviderConfig() {
        return serviceProviderConfigSchema;
    }

}
