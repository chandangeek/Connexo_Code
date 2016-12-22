package com.energyict.mdc.upl.messages.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a message tag specification.
 * A message tag specification has a name.
 * A message tag specification has a collection of attributes specifications
 * and can have other subElements specifications(tags specifications or value specifications).
 * A message tag specification describes the data that will be contained in a message tag.
 *
 * @author gde
 */
public class MessageTagSpec extends MessageElementSpec {

    private final String name;
    private final List<MessageElementSpec> subElements;
    private final List<MessageAttributeSpec> attributeSpecs;

    public MessageTagSpec(String name) {
        this.name = name;
        this.subElements = new ArrayList<>();
        this.attributeSpecs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<MessageElementSpec> getSubElements() {
        return subElements;
    }

    public boolean isTag() {
        return true;
    }

    public void add(MessageElementSpec element) {
        this.subElements.add(element);
    }

    public List<MessageAttributeSpec> getAttributeSpecs() {
        return Collections.unmodifiableList(attributeSpecs);
    }

    public void add(MessageAttributeSpec attributeSpec) {
        this.attributeSpecs.add(attributeSpec);
    }
}
