package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;

@XmlRootElement
public class UserSchema extends BaseSchemaWithCommonAttributes {

    private String userName;

    private String displayName;

    private String locale;

    private boolean active;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "UserSchema{" +
                "userName='" + userName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", locale='" + locale + '\'' +
                ", active=" + active +
                ", schemas=" + Arrays.toString(schemas) +
                ", id='" + id + '\'' +
                ", externalId='" + externalId + '\'' +
                ", meta=" + meta +
                '}';
    }
}
