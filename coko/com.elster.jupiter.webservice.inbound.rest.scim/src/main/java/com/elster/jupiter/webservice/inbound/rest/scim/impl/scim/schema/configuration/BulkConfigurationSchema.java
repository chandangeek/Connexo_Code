package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BulkConfigurationSchema extends BasicConfigurationSchema {

    private Integer maxOperations;

    private Integer maxPayloadSize;

    public BulkConfigurationSchema() {
    }

    public BulkConfigurationSchema(boolean supported, Integer maxOperations, Integer maxPayloadSize) {
        super(supported);
        this.maxOperations = maxOperations;
        this.maxPayloadSize = maxPayloadSize;
    }

    public Integer getMaxOperations() {
        return maxOperations;
    }

    public void setMaxOperations(Integer maxOperations) {
        this.maxOperations = maxOperations;
    }

    public Integer getMaxPayloadSize() {
        return maxPayloadSize;
    }

    public void setMaxPayloadSize(Integer maxPayloadSize) {
        this.maxPayloadSize = maxPayloadSize;
    }
}
