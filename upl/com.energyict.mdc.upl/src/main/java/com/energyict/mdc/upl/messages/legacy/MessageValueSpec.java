package com.energyict.mdc.upl.messages.legacy;

import java.util.Collections;
import java.util.List;

/**
 * This class represents a message value specification.
 * A message value specification describes the data that will be contained in a message value element.
 *
 * @author gde
 */
public class MessageValueSpec extends MessageElementSpec {

    private final String value;

    public MessageValueSpec() {
        this(null);
    }

    public MessageValueSpec(String value) {
        super();
        this.value = value;
    }

    public boolean isValue() {
        return true;
    }

    public List<MessageElementSpec> getSubElements() {
        return Collections.emptyList();
    }

    public String getValue() {
        return value;
    }

}