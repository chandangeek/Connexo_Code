package com.elster.jupiter.messaging;


import com.elster.jupiter.util.HasName;

import java.sql.SQLException;

/**
 * Models a Subscriber on a Destination.
 */
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

    /**
     * Other threads may safely invoke this method to cancel a blocking receive().
     * @throws SQLException
     */
    void cancel() throws SQLException;
}
