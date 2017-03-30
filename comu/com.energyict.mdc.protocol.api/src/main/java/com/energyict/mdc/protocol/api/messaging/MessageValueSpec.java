/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a message value specification.
 * A message value specification describes the data that will be contained in a message value element.
 *
 * @author gde
 */
public class MessageValueSpec extends MessageElementSpec {

    private String value;

    public boolean isValue() {
        return true;
    }

    public List getSubElements() {
        return new ArrayList();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
