/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.nls.MessageSeed;

/**
 * @author Stijn Vanhoorelbeke
 * @since 29.09.17 - 17:22
 */
public class ConnectionCommunicationException extends CommunicationException {

    public enum Type {
        /* A special case applicable for physical slaves that have the same gateway (and thus connection task)
        * It is a common timeout (we did not receive the response of the slave device in time), but the connection is still intact. Other physical slaves can still use it. */

        /**
         * Use in case a connection related exception occurred, but for which the connection remains valid and still can be used for communication afterwards.
         * Usage for this kind of exception is mainly when reading out physical slaves (who have the same gateway and thus also connection task).<p>
         * For example:<br/>
         * During readout of a physical slave a common timeout is reached (or in other words: we did not receive the response of the slave device).<br/>
         * This implies that:
         * <ul>
         * <li>we cannot communicate any more to that physical slave</li>
         * <li>we still can communicate to all other physical slaves</li>
         * </ul>
         */
        CONNECTION_STILL_INTACT,

        /**
         * Use in case the connection is broken/closed and can no longer be used for any communication at all.
         * This implies that:
         * <ul>
         * <li>all other physical slaves <b>cannot</b> be read out</li>
         * <li>the next comtasks for this connection wil be set to 'not executed'</li>
         * </ul>
         */
        NON_RECOVERABLE;
    }

    public ConnectionCommunicationException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public ConnectionCommunicationException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

    /**
     * Getter for the type of the exception
     */
    public Type getExceptionType() {
        return Type.NON_RECOVERABLE; // By default all exceptions are non recoverable, subclasses can override this
    }
}