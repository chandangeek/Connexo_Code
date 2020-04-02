package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthenticationScheme {

    private String type;

    private String name;

    private String description;

    public AuthenticationScheme() {
    }

    public AuthenticationScheme(String type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
