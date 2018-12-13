/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pubsub;

/**
 * A Subscriber service handles events synchronously.
 */
public interface Subscriber {

    /**
     * The implementer is given the opportunity to respond to the given notification by calling this method.
     *
     * @param notification notification that occurred, will be an instance of a Class that is returned by getClasses().
     * @param notificationDetails optionally a notification may have more objects that are part of the notification
     */
	void handle(Object notification, Object... notificationDetails);

    /**
     * The implementer will return the types it is interested in of being notified.
     * Every call will return an array, each time containing the same classes.
     *
     * @return not null, possibly empty
     */
	Class<?>[] getClasses();
}
