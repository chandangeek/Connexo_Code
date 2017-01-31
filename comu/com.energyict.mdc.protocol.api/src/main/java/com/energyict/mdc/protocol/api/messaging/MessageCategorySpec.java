/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class represents a message category specification.
 * In a message category specification, similar message specifications are grouped.
 * Example:
 * Category spec; General, Messages specs:Connect, Disconnect
 *
 * @author gde
 */
public class MessageCategorySpec {

    private String name;
    private List messageSpecs;

    public MessageCategorySpec(String name) {
        this.name = name;
        messageSpecs = new ArrayList();
    }

    public String getName() {
        return name;
    }

    public List getMessageSpecs() {
        return messageSpecs;
    }

    public void addMessageSpec(MessageSpec spec) {
        messageSpecs.add(spec);
    }

    public boolean isAdvancedOnly() {
        boolean advancedOnly = true;
        for (Iterator it = getMessageSpecs().iterator(); it.hasNext(); ) {
            if (!((MessageSpec) it.next()).isAdvanced()) {
                advancedOnly = false;
                break;
            }
        }
        return advancedOnly;
    }
}
