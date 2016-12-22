package com.energyict.mdc.upl.messages.legacy;

/**
 * This class represents a message value.
 * It contains the value for a message tag.
 *
 * @author gde
 */
public class MessageValue extends MessageElement {

    private final String value;

    public MessageValue(String value) {
        this.value = value;
    }

    public String toString() {
        return null;
    }

    public String getValue() {
        return value;
    }

    public String write(Messaging messaging) {
        return null;
    }

    public boolean isValue() {
        return true;
    }

}