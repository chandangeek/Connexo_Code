package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.comserver.eventsimpl.EventPublisher;
import com.energyict.mdc.engine.events.EventRegistrationRequestInitiator;


/**
 * Represents a request from an interested party to
 * register an interest in events that occur
 * in the {@link com.energyict.mdc.engine.model.ComServer}.
 * Note that it is allowed to register multiple times and narrow down
 * the interests to limit the amount of events received.
 * Note also that the interest can be registered multiple
 * time so e.g. change the interest in events that
 * relate to one {@link com.energyict.mdc.protocol.api.device.BaseDevice device} to another device.
 * A request can include what type of events should be returned.
 * The interested party can choose between "text" or "binary" events.
 * The difference is explained below:
 * <ul>
 * <li>text: sends the String representation of the event to the interested party
 *           The interested party will need to implement the {@link org.eclipse.jetty.websocket.WebSocket.OnTextMessage} interface</li>
 * <li>binary: sends the serialized version of the event to the interested party
 *             The interested party will need to implement the {@link org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage} interface
 *             and deserialize the received bytes with an {@link java.io.ObjectInputStream}.</li>
 * </ul>
 * <p>
 * Making the initial request is done through the
 * {@link EventRegistrationRequestInitiator}
 * which will give you the URL on which you can post Requests
 * via WebSocket technology.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (17:34)
 */
public interface Request {

    /**
     * Marks the request to produce binary events
     * when publishing to the interested party
     * that posted this Request.
     *
     * @param flag The flag value
     */
    public void setBinaryEvents (boolean flag);

    /**
     * Tests if this Request was marked to produce
     * binary events when publishing to the interested
     * party that posted this Request.
     *
     * @return <code>true</code> iff this Request was marked to produce binary events
     */
    public boolean useBinaryEvents ();

    public void applyTo (EventPublisher eventPublisher);

}