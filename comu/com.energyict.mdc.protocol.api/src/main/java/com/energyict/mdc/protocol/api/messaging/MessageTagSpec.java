/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

import java.util.ArrayList;
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

    private String name;
    private List subElements;
    private List attributeSpecs;

    public MessageTagSpec(String name) {
        this.name = name;
        this.subElements = new ArrayList();
        this.attributeSpecs = new ArrayList();
    }

    public String getName() {
        return name;
    }

    public List getSubElements() {
        return subElements;
    }

    public boolean isTag() {
        return true;
    }

    public void add(MessageElementSpec element) {
        this.subElements.add(element);
    }

    public List getAttributeSpecs() {
        return attributeSpecs;
    }

    public void add(MessageAttributeSpec attributeSpec) {
        this.attributeSpecs.add(attributeSpec);
    }
}
