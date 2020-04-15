package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BasicConfigurationSchema {

    protected boolean supported;

    public BasicConfigurationSchema() {
    }

    public BasicConfigurationSchema(boolean supported) {
        this.supported = supported;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }
}
