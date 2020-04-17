package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.attribute.Meta;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BaseSchema {

    protected String[] schemas;

    protected String id;

    protected String externalId;

    protected Meta meta;

    public BaseSchema() {
    }

    public BaseSchema(String id) {
        this.id = id;
    }

    public String[] getSchemas() {
        return schemas;
    }

    public void setSchemas(String[] schemas) {
        this.schemas = schemas;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
