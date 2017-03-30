/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

import java.util.ArrayList;
import java.util.Iterator;
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
    private List elements;

    public MessageSpec(String name) {
        this(name, false);
    }

    public MessageSpec(String name, boolean advanced) {
        this.name = name;
        this.advanced = advanced;
        this.elements = new ArrayList();
    }

    public String getName() {
        return name;
    }

    public List getElements() {
        return elements;
    }

    public void add(MessageElementSpec elt) {
        elements.add(elt);
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public MessageValueSpec getValueSpec() {
        for (Iterator it = getElements().iterator(); it.hasNext(); ) {
            MessageElementSpec eltSpec = (MessageElementSpec) it.next();
            if (eltSpec.isValue()) {
                return (MessageValueSpec) eltSpec;
            }
        }
        return null;
    }
}
