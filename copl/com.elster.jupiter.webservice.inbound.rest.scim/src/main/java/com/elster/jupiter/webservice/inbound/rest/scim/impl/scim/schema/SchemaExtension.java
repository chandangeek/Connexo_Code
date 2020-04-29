package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SchemaExtension {

    private String schema;

    private boolean required;

    public SchemaExtension() {
    }

    public SchemaExtension(String schema, boolean required) {
        this.schema = schema;
        this.required = required;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
