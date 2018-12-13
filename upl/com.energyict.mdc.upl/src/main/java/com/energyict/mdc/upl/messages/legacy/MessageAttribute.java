package com.energyict.mdc.upl.messages.legacy;

/**
 * This class represents a message attribute.
 * Attributes are used to describe elements, or to provide additional information about elements.
 * The message attribute contains the value for the message attribute specification.
 *
 * @author gde
 */

public class MessageAttribute {

    private final MessageAttributeSpec spec;
    private final String value;

    public MessageAttribute(String name, String value) {
        this(name, value, false);
    }

    public MessageAttribute(String name, String value, boolean required) {
        this.spec = new MessageAttributeSpec(name, required);
        this.value = value;
    }

    public MessageAttributeSpec getSpec() {
        return spec;
    }

    @Override
    public String toString() {
        return null;
    }

    public String getValue() {
        return value;
    }

}