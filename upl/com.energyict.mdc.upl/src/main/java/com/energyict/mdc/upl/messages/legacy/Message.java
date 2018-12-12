package com.energyict.mdc.upl.messages.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a message. It is an instance of a message specification.
 * It contains a collection of elements that contains all the necessary data to be sent in the message.
 * Elements can be tags and values.
 *
 * @author gde
 */
public class Message {

    private final MessageSpec spec;
    private final List<MessageElement> elements;

    public Message(MessageSpec spec) {
        this.spec = spec;
        this.elements = new ArrayList<>();
    }

    public MessageSpec getSpec() {
        return spec;
    }

    public List<MessageElement> getElements() {
        return Collections.unmodifiableList(this.elements);
    }

    public void add(MessageElement elt) {
        elements.add(elt);
    }

    public String write(Messaging messaging) {
        StringBuilder builder = new StringBuilder();
        this.elements.forEach(element -> this.appendTo(builder, messaging, element));
        return builder.toString();
    }

    private void appendTo(StringBuilder builder, Messaging messaging, MessageElement element) {
        if (element.isTag()) {
            builder.append(messaging.writeTag((MessageTag) element));
        } else if (element.isValue()) {
            builder.append(messaging.writeValue((MessageValue) element));
        }
    }

}
