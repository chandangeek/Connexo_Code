package com.energyict.mdc.device.data;

import com.elster.jupiter.util.time.Interval;
import java.util.List;

/**
 * Models the communication topology for a {@link Device}.
 * A CommunicationTopologyEntry is a collection of Devices that are directly
 * referencing a Device for Communication. A device is directly referencing
 * another device when {@link Device#getCommunicationGateway()} returns that Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-02 (16:04)
 */
public interface CommunicationTopologyEntry {

    /**
     * Gets the {@link Interval} during which this CommunicationTopologyEntry was effective.
     *
     * @return The Interval
     */
    public Interval getInterval ();

    public List<Device> getDevices ();

}