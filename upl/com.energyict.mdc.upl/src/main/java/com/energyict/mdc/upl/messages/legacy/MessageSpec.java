package com.energyict.mdc.upl.messages.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This class represents a message specification.
 * It contains a collection of elements specifications that describe all the necessary data to be sent in the message.
 *
 * @author gde
 */
public class MessageSpec {

    private String name;
    private boolean advanced;
    private List<MessageElementSpec> elements;

    public MessageSpec(String name) {
        this(name, false);
    }

    public MessageSpec(String name, boolean advanced) {
        this.name = name;
        this.advanced = advanced;
        this.elements = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<MessageElementSpec> getElements() {
        return Collections.unmodifiableList(this.elements);
    }

    public void add(MessageElementSpec elt) {
        elements.add(elt);
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public MessageValueSpec getValueSpec() {
        return this.elements.stream().filter(MessageElementSpec::isValue).findFirst().map(MessageValueSpec.class::cast).orElse(null);
    }

}
