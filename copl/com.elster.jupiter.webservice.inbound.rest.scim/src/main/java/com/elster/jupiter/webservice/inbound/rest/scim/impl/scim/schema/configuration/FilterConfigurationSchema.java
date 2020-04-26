package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FilterConfigurationSchema extends BasicConfigurationSchema {

    private Integer maxResults;

    public FilterConfigurationSchema() {
    }

    public FilterConfigurationSchema(boolean supported, Integer maxResults) {
        super(supported);
        this.maxResults = maxResults;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }
}
