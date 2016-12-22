package com.energyict.mdc.upl.messages.legacy;

import java.util.ArrayList;
import java.util.Collections;
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

    private final String name;
    private final List<MessageSpec> messageSpecs;

    public MessageCategorySpec(String name) {
        this.name = name;
        messageSpecs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<MessageSpec> getMessageSpecs() {
        return Collections.unmodifiableList(messageSpecs);
    }

    public void addMessageSpec(MessageSpec spec) {
        messageSpecs.add(spec);
    }

    public boolean isAdvancedOnly() {
        return this.messageSpecs.stream().allMatch(MessageSpec::isAdvanced);
    }

}