/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a message. It is an instance of a message specification.
 * It contains a collection of elements that contains all the necessary data to be sent in the message.
 * Elements can be tags and values.
 *
 * @author gde
 */
public class Message {

    private MessageSpec spec;
    private List elements;

    public Message(MessageSpec spec) {
        this.spec = spec;
        this.elements = new ArrayList();
    }

    public MessageSpec getSpec() {
        return spec;
    }

    public List getElements() {
        return elements;
    }

    public void add(MessageElement elt) {
        elements.add(elt);
    }

    public String write(Messaging messaging) {
        StringBuilder builder = new StringBuilder();
        for (Iterator it = getElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(messaging.writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                builder.append(messaging.writeValue((MessageValue) elt));
            }
        }
        return builder.toString();
    }
}
