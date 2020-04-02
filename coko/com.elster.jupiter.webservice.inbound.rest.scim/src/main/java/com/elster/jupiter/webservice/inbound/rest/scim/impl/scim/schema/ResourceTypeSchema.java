package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceTypeSchema extends BaseSchema {

    private String name;

    private String description;

    private String endpoint;

    private String schema;

    private SchemaExtension[] schemaExtensions;

    public ResourceTypeSchema() {
    }

    public ResourceTypeSchema(String id, String name, String description, String endpoint, String schema, SchemaExtension[] schemaExtensions) {
        super(id);
        this.name = name;
        this.description = description;
        this.endpoint = endpoint;
        this.schema = schema;
        this.schemaExtensions = schemaExtensions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public SchemaExtension[] getSchemaExtensions() {
        return schemaExtensions;
    }

    public void setSchemaExtensions(SchemaExtension[] schemaExtensions) {
        this.schemaExtensions = schemaExtensions;
    }
}
