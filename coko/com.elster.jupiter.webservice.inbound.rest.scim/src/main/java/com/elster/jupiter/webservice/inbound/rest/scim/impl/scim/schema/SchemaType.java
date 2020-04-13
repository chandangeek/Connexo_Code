package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema;

public enum SchemaType {

    USER_SCHEMA("urn:ietf:params:scim:schemas:core:2.0:User"),
    GROUP_SCHEMA("urn:ietf:params:scim:schemas:core:2.0:Group"),
    SERVICE_PROVIDER_CONFIGURATION_SCHEMA("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"),
    RESOURCE_TYPE_SCHEMA("urn:ietf:params:scim:schemas:core:2.0:ResourceType");

    private String id;

    SchemaType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
