package com.energyict.mdc.upl.messages.legacy;

/**
 * This class represents a message attribute specification.
 * Attributes are used to describe elements, or to provide additional information about elements.
 * Here the name of the attribute is specified and whether is is required or not.
 * The value is optional and can be used to specify values that are valid.
 *
 * @author gde
 */
public class MessageAttributeSpec {

    private final String name;
    private final boolean required;
    private String value;

    public MessageAttributeSpec(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRequired() {
        return required;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}