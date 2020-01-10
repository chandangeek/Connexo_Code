package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GroupSchema extends BaseSchemaWithCommonAttributes {

    private String displayName;

    private String[] members;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String[] getMembers() {
        return members;
    }

    public void setMembers(String[] members) {
        this.members = members;
    }
}
