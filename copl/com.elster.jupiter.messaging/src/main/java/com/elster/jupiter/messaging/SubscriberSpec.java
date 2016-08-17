package com.elster.jupiter.messaging;


import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a Subscriber on a Destination.
 */
@ProviderType
public interface SubscriberSpec extends HasName {

    /**
     * @return the Destination to which is being subscribed.
     */
    DestinationSpec getDestination();

    Receiver newReceiver();

    boolean isSystemManaged();

    interface Receiver {

        /**
         * This method blocks indefinitely until the next message on the Destination is available.
         * @return the next message on the Destination
         */
        Message receive();

        /**
         * Other threads may safely invoke this method to cancel a blocking receive().
         */
        void cancel();

    }
}
