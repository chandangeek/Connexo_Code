package com.elster.jupiter.systemadmin.rest.imp.response;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LicenseProperty {

    public String key;
    public String value;

    /**
     * Default constructor 4 JSON deserialization
     */
    public LicenseProperty() {
    }

    public LicenseProperty(String name, String value) {
        this.key = name;
        this.value = value;

    }
}