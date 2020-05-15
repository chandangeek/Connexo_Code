package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.ResourceTypeSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.SchemaExtension;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.SchemaType;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.attribute.Meta;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("ResourceTypes")
public class ResourceTypeResource {

    private static final ResourceTypeSchema[] resourceTypeSchemes;

    static {
        final Meta userResourceTypeMeta = new Meta();
        userResourceTypeMeta.setLocation("/ResourceTypes/User");
        userResourceTypeMeta.setResourceType("ResourceType");

        ResourceTypeSchema userTypeSchema = new ResourceTypeSchema("User", "User", "User Account", "/Users", SchemaType.USER_SCHEMA.getId(), null);
        userTypeSchema.setMeta(userResourceTypeMeta);
        userTypeSchema.setSchemas(new String[]{SchemaType.RESOURCE_TYPE_SCHEMA.getId()});

        final Meta groupResourceTypeMeta = new Meta();
        groupResourceTypeMeta.setLocation("/ResourceTypes/Group");
        groupResourceTypeMeta.setResourceType("ResourceType");

        ResourceTypeSchema groupTypeSchema = new ResourceTypeSchema("Group", "Group", "Group Representation", "/Groups", SchemaType.GROUP_SCHEMA.getId(), null);
        groupTypeSchema.setMeta(groupResourceTypeMeta);
        groupTypeSchema.setSchemas(new String[]{SchemaType.RESOURCE_TYPE_SCHEMA.getId()});

        resourceTypeSchemes = new ResourceTypeSchema[]{
                userTypeSchema,
                groupTypeSchema
        };
    }

    @GET
    @Produces("application/scim+json")
    public ResourceTypeSchema[] getResourceTypeSchemes() {
        return resourceTypeSchemes;
    }

}
