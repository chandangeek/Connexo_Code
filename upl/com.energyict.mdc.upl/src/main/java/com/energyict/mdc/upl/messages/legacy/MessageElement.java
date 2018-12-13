package com.energyict.mdc.upl.messages.legacy;

/**
 * This class represents a message element.
 * A message has a collection of elements.
 * Elements can be tags or values.
 * A message element contains the data described by the message element specification.
 *
 * @author gde
 */
public abstract class MessageElement {

    public boolean isValue() {
        return false;
    }

    public boolean isTag() {
        return false;
    }

}