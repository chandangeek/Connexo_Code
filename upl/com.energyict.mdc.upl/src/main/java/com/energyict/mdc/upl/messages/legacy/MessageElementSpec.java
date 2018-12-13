package com.energyict.mdc.upl.messages.legacy;

import java.util.List;

/**
 * This class represents a message element specification .
 * A message has a collection of element specifications. (tag or value specification)
 * A message element specification describes the data that will be contained in a message element.
 *
 * @author gde
 */
public abstract class MessageElementSpec {

    public boolean isValue() {
        return false;
    }

    public boolean isTag() {
        return false;
    }

    public abstract List<MessageElementSpec> getSubElements();

}