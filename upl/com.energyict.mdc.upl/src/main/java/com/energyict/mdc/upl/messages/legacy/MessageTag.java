package com.energyict.mdc.upl.messages.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a message tag.
 * A message tag has a name.
 * A message tag has a collection of attributes and can have other subElements (tags or values).
 * A message tag contains the data described by the message tag specification.
 *
 * @author gde
 */
public class MessageTag extends MessageElement {

    private String name;
    private List<MessageAttribute> attributes;
    private List<MessageElement> subElements;

    public MessageTag(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.subElements = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<MessageAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public void add(MessageAttribute attribute) {
        this.attributes.add(attribute);
    }

    public List<MessageElement> getSubElements() {
        return Collections.unmodifiableList(this.subElements);
    }

    public void add(MessageElement element) {
        this.subElements.add(element);
    }

    public String write(Messaging messaging) {
        return messaging.writeTag(this);
    }

    public boolean isTag() {
        return true;
    }

}