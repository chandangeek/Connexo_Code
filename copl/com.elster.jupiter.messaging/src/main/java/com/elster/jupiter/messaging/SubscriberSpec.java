/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;


import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.conditions.Condition;

import aQute.bnd.annotation.ProviderType;
import java.util.function.Predicate;

/**
 * Models a Subscriber on a Destination.
 */
@ProviderType
public interface SubscriberSpec extends HasName {

    String getDisplayName();

    /**
     * @return the Destination to which is being subscribed.
     */
    DestinationSpec getDestination();

    /**
     * This method blocks indefinitely until the next message on the Destination is available.
     *
     * @return the next message on the Destination
     */
    Message receive();

    /**
     * This method blocks indefinitely until the next valid message on the Destination is available.
     * @param validationFunction is a function to validate message
     * @return the next message on the Destination
     */
    Message receive(Predicate<Message> validationFunction);

    boolean isSystemManaged();

    /**
     * Other threads may safely invoke this method to cancel a blocking receive().
     */
    void cancel();

    Layer getNlsLayer();

    String getNlsComponent();

    Condition getFilterCondition();

    String getFilter();

}