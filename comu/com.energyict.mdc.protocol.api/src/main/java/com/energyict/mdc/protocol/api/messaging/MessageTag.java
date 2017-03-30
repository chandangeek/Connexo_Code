/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

import java.util.ArrayList;
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
    private List attributes;
    private List subElements;

    public MessageTag(String name) {
        this.name = name;
        this.attributes = new ArrayList();
        this.subElements = new ArrayList();
    }

    public String getName() {
        return name;
    }

    public List getAttributes() {
        return attributes;
    }

    public void add(MessageAttribute attribute) {
        this.attributes.add(attribute);
    }

    public List getSubElements() {
        return subElements;
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
