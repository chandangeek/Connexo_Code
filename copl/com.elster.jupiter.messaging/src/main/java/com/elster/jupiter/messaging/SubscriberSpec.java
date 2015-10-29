package com.elster.jupiter.messaging;


import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasName;

/**
 * Models a Subscriber on a Destination.
 */
@ProviderType
public interface SubscriberSpec extends HasName {

    /**
     * @return the Destination to which is being subscribed.
     */
    DestinationSpec getDestination();

    /**
     * This method blocks indefinitely until the next message on the Destination is available.
     * @return the next message on the Destination
     */
    Message receive();

    boolean isSystemManaged();

    /**
     * Other threads may safely invoke this method to cancel a blocking receive().
     */
    void cancel();
}
