/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

/**
 * This class represents a message value.
 * It contains the value for a message tag.
 *
 * @author gde
 */
public class MessageValue extends MessageElement {

    private String value;

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
